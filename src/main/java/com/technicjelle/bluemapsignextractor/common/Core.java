package com.technicjelle.bluemapsignextractor.common;

import de.bluecolored.bluemap.api.BlueMapMap;
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
	public static void loadMarkersFromWorld(Logger logger, Path regionFolder, Collection<BlueMapMap> maps) {
		for (BlueMapMap map : maps) {
			final String currentMapPrefix = "Map \"" + map.getId() + "\" (" + regionFolder + "): ";
			logger.info(currentMapPrefix + "Loading signs into markers...");

			try (final Stream<Path> stream = Files.list(regionFolder)) {
				stream.filter(path -> path.toString().endsWith(".mca")).forEach(path -> processMCA(logger, map, path));
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Error reading region folder", e);
			}
			logger.info(currentMapPrefix + "Finished loading signs into markers!");
		}
	}

	private static void processMCA(Logger logger, BlueMapMap map, Path regionFile) {
		logger.fine("Processing region " + regionFile.getFileName().toString());

		final MCA mca = new MCA(regionFile);
		try {
			for (BlockEntity blockEntity : mca.getBlockEntities()) {
				if (blockEntity.isInvalidSign()) continue;

				final HtmlMarker htmlMarker = HtmlMarker.builder()
						.label(blockEntity.getLabel())
						.position(blockEntity.getPosition())
						.html(blockEntity.getFormattedHTML())
						.styleClasses("sign")
						.maxDistance(16)
						.build();

				final MarkerSet markerSet = map.getMarkerSets().computeIfAbsent("signs", id -> MarkerSet.builder().label("Signs").toggleable(true).defaultHidden(false).build());

				markerSet.put(blockEntity.getKey(), htmlMarker);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error reading region file", e);
		}
	}
}
