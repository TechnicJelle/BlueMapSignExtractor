package com.technicjelle.bluemapsignextractor.impl.paper;

import com.technicjelle.MCUtils;
import com.technicjelle.bluemapsignextractor.common.Config;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class PaperConfig implements Config {
	private String markerSetName;
	private boolean toggleable;
	private boolean defaultHidden;
	private double maxDistance;
	private boolean suppressWarnings;

	public PaperConfig(JavaPlugin plugin) {
		loadFromPlugin(plugin);
	}

	public void loadFromPlugin(JavaPlugin plugin) {
		try {
			MCUtils.copyPluginResourceToConfigDir(plugin, "config.yml", "config.yml", false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		//Load config from disk
		plugin.reloadConfig();

		//Load config values into variables
		markerSetName = plugin.getConfig().getString("MarkerSetName", "Signs");
		toggleable = plugin.getConfig().getBoolean("Toggleable", true);
		defaultHidden = plugin.getConfig().getBoolean("DefaultHidden", false);
		maxDistance = plugin.getConfig().getDouble("MaxDistance", 0.0);
		suppressWarnings = plugin.getConfig().getBoolean("SuppressWarnings", false);
	}

	@Override
	public String getMarkerSetName() {
		return markerSetName;
	}

	@Override
	public boolean isToggleable() {
		return toggleable;
	}

	@Override
	public boolean isDefaultHidden() {
		return defaultHidden;
	}

	@Override
	public double getMaxDistance() {
		return maxDistance;
	}

	@Override
	public boolean areWarningsAllowed() {
		return !suppressWarnings;
	}
}
