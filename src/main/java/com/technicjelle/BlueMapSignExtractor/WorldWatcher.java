package com.technicjelle.BlueMapSignExtractor;

import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.common.api.BlueMapMapImpl;
import de.bluecolored.bluemap.common.api.BlueMapWorldImpl;
import de.bluecolored.bluemap.core.util.WatchService;
import de.bluecolored.bluemap.core.world.Chunk;
import de.bluecolored.bluemap.core.world.Region;
import de.bluecolored.bluemap.core.world.World;
import de.bluecolored.bluemap.core.world.block.entity.SignBlockEntity;

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

		logger.logInfo("Started watching world '" + world.getId() + "' for sign updates...");

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
			logger.logInfo("Stopped watching world " + world.getId() + " for sign updates.");
			if (!closed) {
				logger.logWarning("Region-file watch-service for world " + world.getId() +
								  " stopped unexpectedly! (The signs might not update automatically from now on)");
			}
		}
	}

	private synchronized void updateRegion(Vector2i regionPos) {
		// we only want to start the extraction when there were no changes on a file for 5 seconds
		TimerTask task = scheduledUpdates.remove(regionPos);
		if (task != null) task.cancel();

		task = new TimerTask() {
			@Override
			public void run() {
				synchronized (WorldWatcher.this) {
					try {
						String regionPrefix = "sign#" + regionPos.getX() + "|" + regionPos.getY() + "@";

						// First, remove all markers from this region
						Map<String, Marker> markers = markerSet.getMarkers();
						markers.keySet().removeIf(key -> key.startsWith(regionPrefix));

						// Then, add them back
						//TODO: this cast should be able to be removed in BlueMap 5.6
						// https://github.com/BlueMap-Minecraft/BlueMap/commit/93d2dc54ba13673123c26c66f8807291dc7aa6ae
						@SuppressWarnings("unchecked")
						Region<Chunk> region = (Region<Chunk>) world.getRegion(regionPos.getX(), regionPos.getY());
						region.iterateAllChunks((int chunkX, int chunkZ, Chunk chunk) -> {
							chunk.iterateBlockEntities(blockEntity -> {
								if (blockEntity instanceof SignBlockEntity signBlockEntity) {
									Sign sign = new Sign(signBlockEntity);
									// If the config is set to ignore blank signs, skip them
									if (config.getIgnoreBlankSigns() && sign.isBlank()) return;
									Marker marker = sign.createMarker(config);
									markerSet.put(sign.createKey(regionPrefix), marker);
								}
							});
						});

						// Force save the markers to the storage
						// This is especially important for the CLI, which otherwise would not save the markers for a long time
						saveMarkers(); //TODO: Should be done less (only when all regions are done)
					} catch (IOException e) {
						logger.logError("Failed to get region" + regionPos, e);
					}
				}
			}
		};
		scheduledUpdates.put(regionPos, task);
		delayTimer.schedule(task, 5000);
	}

	public void close() {
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
