package com.technicjelle.BlueMapSignExtractor;

import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import de.bluecolored.bluemap.common.api.BlueMapMapImpl;
import de.bluecolored.bluemap.common.api.BlueMapWorldImpl;
import de.bluecolored.bluemap.core.util.WatchService;
import de.bluecolored.bluemap.core.world.Chunk;
import de.bluecolored.bluemap.core.world.ChunkConsumer;
import de.bluecolored.bluemap.core.world.Region;
import de.bluecolored.bluemap.core.world.World;
import de.bluecolored.bluemap.core.world.mca.blockentity.SignBlockEntity;
import de.bluecolored.bluemap.core.world.mca.chunk.MCAChunk;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import static com.technicjelle.BlueMapSignExtractor.BlueMapSignExtractor.logger;

/**
 * Watches a world for changes and updates the markers accordingly.
 * Creates separate MarkerSets for each sign group (e.g., [home], [station]).
 *
 * @see de.bluecolored.bluemap.common.plugin.MapUpdateService
 */
public class WorldWatcher extends Thread {
	private static final String MARKER_SET_PREFIX = "bmse-";

	private final BlueMapWorld apiWorld;
	private final World world;
	private final WatchService<Vector2i> watchService;

	private volatile boolean closed;

	private Timer delayTimer;

	private final Map<Vector2i, TimerTask> scheduledUpdates;

	private final Map<String, MarkerSet> markerSets = new ConcurrentHashMap<>();

	private final Config config;

	public WorldWatcher(BlueMapWorld apiWorld, Config config) throws IOException {
		this.apiWorld = apiWorld;
		this.world = ((BlueMapWorldImpl) apiWorld).world();
		this.closed = false;
		this.scheduledUpdates = new HashMap<>();
		this.watchService = world.createRegionWatchService();
		this.config = config;
	}

	private MarkerSet getOrCreateMarkerSet(String groupName) {
		return markerSets.computeIfAbsent(groupName, name -> {
			MarkerSet markerSet = MarkerSet.builder()
					.label(titleCase(name))
					.toggleable(config.getToggleable())
					.defaultHidden(config.getDefaultHidden())
					.build();

			String key = MARKER_SET_PREFIX + name;
			apiWorld.getMaps().forEach(map -> map.getMarkerSets().put(key, markerSet));

			return markerSet;
		});
	}

	private static String titleCase(String input) {
		if (input.isEmpty()) return input;
		// "train-station" -> "Train Station"
		String[] parts = input.split("-");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i > 0) sb.append(" ");
			if (!parts[i].isEmpty()) {
				sb.append(Character.toUpperCase(parts[i].charAt(0)));
				if (parts[i].length() > 1) sb.append(parts[i].substring(1));
			}
		}
		return sb.toString();
	}

	@Override
	public void run() {
		if (delayTimer == null) delayTimer = new Timer("BMSE-WorldWatcher-DelayTimer", true);

		logger.logDebug("Started watching world '" + world.getId() + "' for sign updates...");

		//Initial update
		world.listRegions().forEach(this::updateRegion);

		try {
			while (!closed)
				this.watchService.take().forEach(this::updateRegion);
		} catch (WatchService.ClosedException ignore) {
		} catch (IOException e) {
			logger.logError("Exception trying to watch world " + world.getId() + " for sign updates.", e);
		} catch (InterruptedException iex) {
			Thread.currentThread().interrupt();
		} finally {
			logger.logDebug("Stopped watching world " + world.getId() + " for sign updates.");
			if (!closed) {
				logger.logWarning("Region-file watch-service for world " + world.getId() +
								  " stopped unexpectedly! (The signs might not update automatically from now on)");
			}
		}
	}

	private synchronized void updateRegion(Vector2i regionPos) {
		if (closed) return;

		TimerTask task = scheduledUpdates.remove(regionPos);
		if (task != null) task.cancel();

		task = new TimerTask() {
			@Override
			public void run() {
				synchronized (WorldWatcher.this) {
					int x = regionPos.getX();
					int z = regionPos.getY();
					String regionPrefix = "sign#" + x + "|" + z + "@";

					// Remove markers with this region prefix from ALL marker sets
					for (MarkerSet markerSet : markerSets.values()) {
						markerSet.getMarkers().keySet().removeIf(key -> key.startsWith(regionPrefix));
					}

					// Re-scan the region and add group markers
					Region<Chunk> region = world.getRegion(x, z);
					try {
						region.iterateAllChunks(new ChunkConsumer<>() {
							@Override
							public void accept(int chunkX, int chunkZ, Chunk chunk) {
								final int dataVersion = chunk instanceof MCAChunk mcaChunk ? mcaChunk.getDataVersion() : -1;
								chunk.iterateBlockEntities(blockEntity -> {
									if (blockEntity instanceof SignBlockEntity signBlockEntity) {
										Sign sign = new Sign(signBlockEntity, dataVersion);
										Optional<String> groupName = sign.getGroupName();
										if (groupName.isEmpty()) return; // Not a group sign, skip

										MarkerSet markerSet = getOrCreateMarkerSet(groupName.get());
										POIMarker marker = sign.createGroupMarker(config);
										markerSet.put(sign.createKey(regionPrefix), marker);
									}
								});
							}

							@Override
							public void fail(int chunkX, int chunkZ, IOException ex) {
								if (ex.getCause() instanceof SignException signEx) {
									throw signEx;
								}
								logger.logDebug("Failed to load chunk (%d, %d) from region (x:%d, z:%d):\n%s".formatted(chunkX, chunkZ, x, z, ex));
							}
						});

						saveMarkers();
					} catch (IOException | SignException e) {
						logger.logError("Failed to get region (x:%d, z:%d)".formatted(x, z), e);
					}
				}
			}
		};
		scheduledUpdates.put(regionPos, task);
		delayTimer.schedule(task, 5000);
	}

	public synchronized void close() {
		this.closed = true;
		this.interrupt();

		if (this.delayTimer != null) this.delayTimer.cancel();

		try {
			this.watchService.close();
		} catch (Exception e) {
			logger.logError("Failed to close watch service", e);
		}
	}

	private void saveMarkers() {
		apiWorld.getMaps().forEach(apiMap -> ((BlueMapMapImpl) apiMap).map().saveMarkerState());
	}
}
