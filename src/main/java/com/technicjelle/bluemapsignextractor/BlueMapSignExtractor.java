package com.technicjelle.bluemapsignextractor;

import com.technicjelle.BMCopy;
import com.technicjelle.UpdateChecker;
import com.technicjelle.bluemapsignextractor.common.Core;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapWorld;
import org.bstats.bukkit.Metrics;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
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

	final Consumer<BlueMapAPI> onEnableListener = api -> {
		updateChecker.logUpdateMessage(getLogger());

		try {
			BMCopy.jarResourceToWebApp(api, getClassLoader(), "style.css", "bmse.css", false);
			BMCopy.jarResourceToWebApp(api, getClassLoader(), "Minecraft.otf", "Minecraft.otf", false);
			BMCopy.jarResourceToWebApp(api, getClassLoader(), "sign_oak.png", "sign_oak.png", false);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Failed to copy resources to BlueMap webapp!", e);
		}

		getServer().getScheduler().runTaskAsynchronously(this, () -> loadMarkersFromWorlds(api));
	};

	private void loadMarkersFromWorlds(BlueMapAPI api) {
		for (World world : getServer().getWorlds()) {
			Path saveFolder = world.getWorldFolder().toPath();
			Optional<Path> regionFolder = findRegionFolder(saveFolder);
			if (regionFolder.isEmpty()) {
				getLogger().info("No region folder found for world " + world.getName());
				continue;
			}

			BlueMapWorld blueMapWorld = api.getWorld(world).orElse(null);
			if (blueMapWorld == null) {
				getLogger().info("No BlueMap world found for world " + world.getName());
				continue;
			}
			Core.addMarkersToBlueMapWorld(getLogger(), blueMapWorld, regionFolder.get());
		}
	}

	@Override
	public void onDisable() {
		getLogger().info("BlueMapSignExtractor disabled");
		BlueMapAPI.unregisterListener(onEnableListener);
	}

	public static Optional<Path> findRegionFolder(Path saveFolder) {
		//Breath-first search to find the region folder
		Queue<Path> queue = new LinkedList<>();
		queue.add(saveFolder);
		while (!queue.isEmpty()) {
			Path current = queue.poll();
			try (Stream<Path> allFilesStream = Files.list(current)) {
				Collection<Path> dirs = allFilesStream.filter(Files::isDirectory).collect(Collectors.toList());
				for (Path path : dirs) {
					if (path.endsWith("region")) return Optional.of(path);
					queue.add(path);
				}
			} catch (IOException e) {
				return Optional.empty();
			}
		}
		return Optional.empty();
	}
}
