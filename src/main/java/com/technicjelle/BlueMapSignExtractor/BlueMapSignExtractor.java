package com.technicjelle.BlueMapSignExtractor;

import com.technicjelle.BMUtils.BMCopy;
import com.technicjelle.BMUtils.BMNative.BMNLogger;
import com.technicjelle.BMUtils.BMNative.BMNMetadata;
import com.technicjelle.UpdateChecker;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapWorld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlueMapSignExtractor implements Runnable {
	public static BMNLogger logger;
	private UpdateChecker updateChecker;

	List<WorldWatcher> worldWatchers = new ArrayList<>();

	@Override
	public void run() {
		String addonID;
		String addonVersion;
		try {
			addonID = BMNMetadata.getAddonID(this.getClass().getClassLoader());
			addonVersion = BMNMetadata.getKey(this.getClass().getClassLoader(), "version");
			logger = new BMNLogger(this.getClass().getClassLoader());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		logger.logInfo("Starting " + addonID + " " + addonVersion);
		updateChecker = new UpdateChecker("TechnicJelle", addonID, addonVersion);
		updateChecker.checkAsync();
		BlueMapAPI.onEnable(onEnableListener);
		BlueMapAPI.onDisable(onDisableListener);
	}

	final private Consumer<BlueMapAPI> onEnableListener = api -> {
		updateChecker.getUpdateMessage().ifPresent(logger::logWarning);

		try {
			BMCopy.jarResourceToWebApp(api, this.getClass().getClassLoader(), "style.css", "bmse.css", false);
			BMCopy.jarResourceToWebApp(api, this.getClass().getClassLoader(), "obfuscated_text.js", "bmse_obfuscated_text.js", false);
			BMCopy.jarResourceToWebApp(api, this.getClass().getClassLoader(), "Minecraft.otf", "Minecraft.otf", false);
			BMCopy.jarResourceToWebApp(api, this.getClass().getClassLoader(), "Minecraft-Bold.otf", "Minecraft-Bold.otf", false);
			BMCopy.jarResourceToWebApp(api, this.getClass().getClassLoader(), "sign_oak.png", "sign_oak.png", false);
		} catch (IOException e) {
			logger.logError("Failed to copy resources to BlueMap webapp!", e);
		}

		final Config config;
		try {
			config = Config.load(api);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		for (BlueMapWorld apiWorld : api.getWorlds()) {
			// no need to watch worlds that aren't rendered by bluemap anyway
			if (apiWorld.getMaps().isEmpty()) continue;

			try {
				WorldWatcher watcher = new WorldWatcher(apiWorld, config);
				watcher.start();
				worldWatchers.add(watcher);
			} catch (IOException ex) {
				logger.logError("Failed to create update-watcher for world: " + apiWorld.getId() +
								" (This means the signs might not automatically update)", ex);
			} catch (UnsupportedOperationException ex) {
				logger.logError("Update-watcher for world '" + apiWorld.getId() + "' is not supported for the world-type." +
								" (This means the signs might not automatically update)", ex);
			}
		}
	};

	final private Consumer<BlueMapAPI> onDisableListener = api -> {
		worldWatchers.forEach(WorldWatcher::close);
		worldWatchers.clear();
	};
}
