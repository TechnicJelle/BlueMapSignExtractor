package com.technicjelle.BlueMapSignExtractor;

import com.technicjelle.BMUtils.BMNative.BMNConfigDirectory;
import de.bluecolored.bluemap.api.BlueMapAPI;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.io.IOException;
import java.nio.file.Path;

@ConfigSerializable
public class Config {
	private static final String fileName = "settings.conf";

	private @Nullable String markerSetName;

	private @Nullable Boolean toggleable;

	private @Nullable Boolean defaultHidden;

	private @Nullable Double maxDistance;

	private @Nullable Boolean ignoreBlankSigns;

	public static Config load(BlueMapAPI api) throws IOException {
		BMNConfigDirectory.BMNCopy.fromJarResource(api, Config.class.getClassLoader(), fileName, fileName, false);
		Path configDirectory = BMNConfigDirectory.getAllocatedDirectory(api, Config.class.getClassLoader());
		Path configFile = configDirectory.resolve(fileName);

		HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
				.defaultOptions(options -> options.implicitInitialization(false))
				.path(configFile).build();

		Config config = loader.load().get(Config.class);
		if (config == null) {
			throw new IOException("Failed to load config");
		}

		return config;
	}

	public String getMarkerSetName() {
		return markerSetName != null ? markerSetName : "Signs";
	}

	public boolean getToggleable() {
		return toggleable != null ? toggleable : true;
	}

	public boolean getDefaultHidden() {
		return defaultHidden != null ? defaultHidden : false;
	}

	public double getMaxDistance() {
		return maxDistance != null ? maxDistance : 32;
	}

	public boolean getIgnoreBlankSigns() {
		return ignoreBlankSigns != null ? ignoreBlankSigns : true;
	}
}
