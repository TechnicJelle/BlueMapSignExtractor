package com.technicjelle.bluemapsignextractor;

import com.technicjelle.UpdateChecker;
import com.technicjelle.bluemapsignextractor.models.BlockEntity;
import com.technicjelle.bluemapsignextractor.models.Chunk;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTReader;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class BlueMapSignExtractor extends JavaPlugin {
	private UpdateChecker updateChecker;

	@Override
	public void onEnable() {
		getLogger().info("BlueMapSignExtractor enabled");

		new Metrics(this, 19320);

		updateChecker = new UpdateChecker("TechnicJelle", "BlueMapSignExtractor", getDescription().getVersion());
		updateChecker.checkAsync();

		BlueMapAPI.onEnable(onEnableListener);
	}

	Consumer<BlueMapAPI> onEnableListener = api -> {
		updateChecker.logUpdateMessage(getLogger());
		Bukkit.getScheduler().runTaskAsynchronously(this, () -> loadMarkers(api));
	};

	private void loadMarkers(BlueMapAPI api) {
		for (BlueMapMap map : api.getMaps()) {
			Path saveFolder = map.getWorld().getSaveFolder();
			getLogger().info("Map: " + map.getId() + " " + saveFolder);
			Path regionFolder = saveFolder.resolve("region");

			try (Stream<Path> s = Files.list(regionFolder)) {
				s.filter(p -> p.toString().endsWith(".mca")).forEach(p -> processMCA(map, p));
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Error reading region folder", e);
			}
		}

		getLogger().info("Finished loading markers");
	}

	@Override
	public void onDisable() {
		getLogger().info("BlueMapSignExtractor disabled");
		BlueMapAPI.unregisterListener(onEnableListener);
	}

	private void processMCA(BlueMapMap map, Path mcaFile) {
		getLogger().info("Processing region " + mcaFile.getFileName().toString());
		BlueNBT nbt = new BlueNBT();
		Random random = new Random();
		for (int z = 0; z < 32; z++) {
			for (int x = 0; x < 32; x++) {
				try (InputStream in = MCA.loadChunk(mcaFile, x, z)) {
					if (in == null) continue;

					NBTReader reader = new NBTReader(in);
					Chunk chunk = nbt.read(reader, Chunk.class);

					for (BlockEntity be : chunk.blockEntities) {
						if (be.id.contains("sign") && be.getText1() != null) {
							POIMarker marker = POIMarker.builder()
									.label(be.getText1())
									.detail("<p style=\"white-space: nowrap;text-align: center;\">" + be.getText1() + "<br>" + be.getText2() + "<br>" + be.getText3() + "<br>" + be.getText4() + "</p>")
									.position(be.x + 0.5, be.y + 0.5, be.z + 0.5)
									.build();

							MarkerSet markerSet = map.getMarkerSets().computeIfAbsent("signs", id -> MarkerSet.builder()
									.label("Signs")
									.toggleable(true)
									.defaultHidden(false)
									.build());

							//nice and random key... probably be good enough to prevent duplicates
							String key = be.getText1() + be.getText2() + be.getText3() + be.getText4() + random.nextInt();
							markerSet.put(key, marker);
						}
					}
				} catch (IOException e) {
					getLogger().log(Level.SEVERE, "Error reading mca file", e);
				}
			}
		}
	}
}
