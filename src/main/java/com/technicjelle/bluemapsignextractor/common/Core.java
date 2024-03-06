package com.technicjelle.bluemapsignextractor.common;

import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.HtmlMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Core {
	/**
	 * Adds markers to all of a BlueMapWorld's maps.
	 */
	public static void addMarkersToBlueMapWorld(Logger logger, Config config, BlueMapWorld world, Path regionFolder) {
		Collection<BlueMapMap> maps = world.getMaps();
		if (maps.isEmpty()) return;

		MarkerSet markerSet = loadMarkerSetFromWorld(logger, config, regionFolder);
		maps.forEach(map -> map.getMarkerSets().put(Config.MARKER_SET_ID, markerSet));
	}

	public static MarkerSet loadMarkerSetFromWorld(Logger logger, Config config, Path regionFolder) {
		final String startRegex = "^\\.[/\\\\]";
		final String endRegex = "[/\\\\]?region[/\\\\]?$";
		final String worldPath = regionFolder.toString().replaceAll(startRegex, "").replaceAll(endRegex, "");
		final String currentWorldPrefix = "World \"" + worldPath + "\": ";
		logger.info(currentWorldPrefix + "Extracting signs into markers...");

		final MarkerSet markerSet = MarkerSet.builder()
				.label(config.getMarkerSetName())
				.toggleable(config.isToggleable())
				.defaultHidden(config.isDefaultHidden())
				.build();

		try (final Stream<Path> stream = Files.list(regionFolder)) {
			stream.filter(path -> path.toString().endsWith(MCARegion.FILE_SUFFIX)).forEach(path -> fillMarkerSetFromRegionFile(logger, config, markerSet, path));
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error reading region folder", e);
		}

		logger.info(currentWorldPrefix + "Finished extracting signs into markers!");

		return markerSet;
	}

	private static void fillMarkerSetFromRegionFile(Logger logger, Config config, MarkerSet markerSet, Path regionFile) {
		logger.fine("Processing region " + regionFile.getFileName().toString());

		final MCARegion mcaRegion = new MCARegion(logger, regionFile);
		try {
			for (BlockEntity blockEntity : mcaRegion.getBlockEntities()) {
				if (blockEntity.isInvalidSign()) continue;

				final HtmlMarker htmlMarker = HtmlMarker.builder()
						.label(blockEntity.getLabel())
						.position(blockEntity.getPosition())
						.html(blockEntity.getFormattedHTML())
						.styleClasses("sign")
						.maxDistance(config.getMaxDistance())
						.build();

				markerSet.put(blockEntity.createKey(), htmlMarker);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error reading region file", e);
		}
	}
}
