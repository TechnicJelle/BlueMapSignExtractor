package com.technicjelle.bluemapsignextractor.common;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Core {
	public static void loadMarkers(Logger logger, BlueMapAPI api) {
		for (BlueMapMap map : api.getMaps()) {
			Path saveFolder = map.getWorld().getSaveFolder();
			logger.info("Map: " + map.getId() + " " + saveFolder);
			Path regionFolder = saveFolder.resolve("region");

			try (Stream<Path> stream = Files.list(regionFolder)) {
				stream.filter(path -> path.toString().endsWith(".mca")).forEach(path -> processMCA(logger, map, path));
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Error reading region folder", e);
			}
		}

		logger.info("Finished loading signs into markers");
	}

	private static void processMCA(Logger logger, BlueMapMap map, Path regionFile) {
		logger.info("Processing region " + regionFile.getFileName().toString());

		Random random = new Random();

		MCA mca = new MCA(regionFile);
		try {
			for (BlockEntity blockEntity : mca.getBlockEntities()) {
				if (blockEntity.isInvalidSign()) continue;

				String allMessages = blockEntity.getAllSignMessages();
				logger.info("Sign:\n" + allMessages);
				POIMarker marker = POIMarker.builder().label(allMessages.split("\n")[0]).detail("<p style=\"white-space: nowrap;text-align: center;\">" + allMessages.replace("\n", "<br>") + "</p>").position(blockEntity.getPosition()).build();

				MarkerSet markerSet = map.getMarkerSets().computeIfAbsent("signs", id -> MarkerSet.builder().label("Signs").toggleable(true).defaultHidden(false).build());

				//nice and random key... probably good enough to prevent duplicates
				String key = allMessages.replace("\n", "") + random.nextInt();
				markerSet.put(key, marker);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error reading region file", e);
		}
	}
}
