package com.technicjelle.bluemapsignextractor;

import com.technicjelle.UpdateChecker;
import com.technicjelle.bluemapsignextractor.common.Core;
import de.bluecolored.bluemap.api.BlueMapAPI;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

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
		Bukkit.getScheduler().runTaskAsynchronously(this, () -> Core.loadMarkers(getLogger(), api));
	};

	@Override
	public void onDisable() {
		getLogger().info("BlueMapSignExtractor disabled");
		BlueMapAPI.unregisterListener(onEnableListener);
	}
}
