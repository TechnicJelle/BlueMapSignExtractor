package com.technicjelle.BlueMapSignExtractor;

import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
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
import java.util.Timer;
import java.util.TimerTask;

import static com.technicjelle.BlueMapSignExtractor.BlueMapSignExtractor.logger;

/**
 * Watches a world for changes and updates the markers accordingly
 * Closely matches BlueMap's own MapUpdateService class
 *
 * @see de.bluecolored.bluemap.common.plugin.MapUpdateService
 */
public class WorldWatcher extends Thread {
	private final BlueMapWorld apiWorld;
	private final World world;
	private final WatchService<Vector2i> watchService;

	private volatile boolean closed;

	private Timer delayTimer;

	private final Map<Vector2i, TimerTask> scheduledUpdates;

	private final MarkerSet markerSet;

	private final Config config;

	public WorldWatcher(BlueMapWorld apiWorld, Config config) throws IOException {
		this.apiWorld = apiWorld;
		this.world = ((BlueMapWorldImpl) apiWorld).world();
		this.closed = false;
		this.scheduledUpdates = new HashMap<>();
		this.watchService = world.createRegionWatchService();

		this.config = config;

		this.markerSet = MarkerSet.builder()
				.label(config.getMarkerSetName())
				.toggleable(config.getToggleable())
				.defaultHidden(config.getDefaultHidden())
				.build();

		//All maps of this world share the same marker set
		apiWorld.getMaps().forEach(map -> map.getMarkerSets().put("signs", markerSet));
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
			//when you close the filewatcher while another thread is "take()"-ing
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

		// we only want to start the extraction when there were no changes on a file for 5 seconds
		TimerTask task = scheduledUpdates.remove(regionPos);
		if (task != null) task.cancel();

		task = new TimerTask() {
			@Override
			public void run() {
				synchronized (WorldWatcher.this) {
					int x = regionPos.getX();
					int z = regionPos.getY();
					String regionPrefix = "sign#" + x + "|" + z + "@";

					// First, remove all markers from this region
					markerSet.getMarkers().keySet().removeIf(key -> key.startsWith(regionPrefix));

					// Then, add them back
					Region<Chunk> region = world.getRegion(x, z);
					try {
						region.iterateAllChunks(new ChunkConsumer<>() {
							@Override
							public void accept(int chunkX, int chunkZ, Chunk chunk) {
								//The signs need to know the data version, so they can use the correct String parsing method
								final int dataVersion = chunk instanceof MCAChunk mcaChunk ? mcaChunk.getDataVersion() : -1;
								chunk.iterateBlockEntities(blockEntity -> {
									if (blockEntity instanceof SignBlockEntity signBlockEntity) {
										Sign sign = new Sign(signBlockEntity, dataVersion);
										// If the config is set to ignore blank signs, skip them
										if (config.getIgnoreBlankSigns() && sign.isBlank()) return;
										Marker marker = sign.createMarker(config);
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

						// Force save the markers to the storage
						// This is especially important for the CLI, which otherwise would not save the markers for a long time
						saveMarkers(); //TODO: Should be done less (only when all regions are done)
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
