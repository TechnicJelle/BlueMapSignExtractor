package com.technicjelle.BlueMapSignExtractor;


import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.markers.HtmlMarker;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.mca.blockentity.SignBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Sign {
	private enum SignType { STANDING, WALL, HANGING, WALL_HANGING }

	private final @NotNull SignSide front;
	private final @Nullable SignSide back;
	private final int blockX, blockY, blockZ;
	private final Vector3d position;
	private final SignType signType;
	private final double yawDegrees;

	public Sign(SignBlockEntity signBlockEntity, int dataVersion, BlockState blockState) {
		@Nullable SignBlockEntity.TextData frontText = signBlockEntity.getFrontText();
		if (frontText == null) {
			this.front = new SignSide();
		} else {
			this.front = new SignSide(frontText, dataVersion);
		}
		@Nullable SignBlockEntity.TextData backText = signBlockEntity.getBackText();
		if (backText == null) {
			this.back = null;
		} else {
			this.back = new SignSide(backText, dataVersion);
		}

		this.blockX = signBlockEntity.getX();
		this.blockY = signBlockEntity.getY();
		this.blockZ = signBlockEntity.getZ();

		String blockId = blockState.getId().getValue();
		Map<String, String> props = blockState.getProperties();

		if (blockId.contains("wall")) {
			double facingY = switch (props.getOrDefault("facing", "south")) {
				case "west"  -> 90.0;
				case "north" -> 180.0;
				case "east"  -> 270.0;
				default      -> 0.0; // south
			};
			this.yawDegrees = 360 - facingY;
		} else {
			int rotation = Integer.parseInt(props.getOrDefault("rotation", "0"));
			this.yawDegrees = 360 - rotation * 22.5;
		}

		if (blockId.endsWith("_wall_hanging_sign")) {
			this.signType = SignType.WALL_HANGING;
			this.position = new Vector3d(blockX + 0.5, blockY + 5.0 / 16, blockZ + 0.5);
		} else if (blockId.endsWith("_wall_sign")) {
			this.signType = SignType.WALL;
			this.position = switch (props.getOrDefault("facing", "south")) {
				case "north" -> new Vector3d(blockX + 0.5,    blockY + 8.333 / 16, blockZ + 0.9375);
				case "west"  -> new Vector3d(blockX + 0.9375, blockY + 8.333 / 16, blockZ + 0.5);
				case "east"  -> new Vector3d(blockX + 0.0625, blockY + 8.333 / 16, blockZ + 0.5);
				default      -> new Vector3d(blockX + 0.5,    blockY + 8.333 / 16, blockZ + 0.0625); // south
			};
		} else if (blockId.endsWith("_hanging_sign")) {
			this.signType = SignType.HANGING;
			this.position = new Vector3d(blockX + 0.5, blockY + 5.0 / 16, blockZ + 0.5);
		} else { // blockId.endsWith("_sign")
			this.signType = SignType.STANDING;
			this.position = new Vector3d(blockX + 0.5, blockY + 13.333 / 16, blockZ + 0.5);
		}
	}

	public boolean needsFrontMarker(Config config) {
		return (!config.getIgnoreBlankSigns() && !needsBackMarker(config)) || front.isWrittenOn();
	}

	public boolean needsBackMarker(Config config) {
		return config.getUse3dMarkers() && back != null && back.isWrittenOn();
	}

	public Marker createMarker(Config config) {
		if (!config.getUse3dMarkers()) return createLegacy2dMarker(config);
		boolean isHanging = signType == SignType.HANGING || signType == SignType.WALL_HANGING;
		return buildMarker(
			getLabel(),
			front.isWrittenOn() ? front.getFormattedHTML() : "",
			buildClasses(isHanging, yawDegrees),
			offsetToSurface(position, yawDegrees, isHanging),
			config
		);
	}

	public Marker createBackMarker(Config config) {
		boolean isHanging = signType == SignType.HANGING || signType == SignType.WALL_HANGING;
		double backYaw = (yawDegrees + 180) % 360;
		return buildMarker(
			back.getLabel() != null ? back.getLabel() : getLabel(),
			back.getFormattedHTML(),
			buildClasses(isHanging, backYaw),
			offsetToSurface(position, backYaw, isHanging),
			config
		);
	}

	private static Vector3d offsetToSurface(Vector3d pos, double yawDegrees, boolean isHanging) {
		double rad = Math.toRadians(yawDegrees);
		double offset = isHanging ? 1.0 / 16.0 : 0.75 / 16.0;
		return new Vector3d(
			pos.getX() + Math.sin(rad) * offset,
			pos.getY(),
			pos.getZ() + Math.cos(rad) * offset
		);
	}

	private Marker createLegacy2dMarker(Config config) {
		final StringBuilder sb = new StringBuilder();
		if (front.isWrittenOn() || !config.getIgnoreBlankSigns()) sb.append(front.getFormattedHTML());
		if (back != null && back.isWrittenOn()) sb.append(back.getFormattedHTML());
		Vector3d center = new Vector3d(blockX + 0.5, blockY + 0.5, blockZ + 0.5);
		return buildMarker(getLabel(), sb.toString().stripTrailing(), List.of("sign", "sign-2d"), center, config);
	}

	public String createKey(String prefix) {
		return prefix + blockX + "," + blockY + "," + blockZ;
	}

	public String createBackKey(String prefix) {
		return prefix + blockX + "," + blockY + "," + blockZ + "_b";
	}

	private List<String> buildClasses(boolean isHanging, double yaw) {
		List<String> classes = new ArrayList<>();
		classes.add("sign");
		classes.add("html3d");
		classes.add("html3d-density-288");
		classes.add(isHanging ? "html3d-width-0_875" : "html3d-width-1");
		classes.add(isHanging ? "html3d-height-0_625" : "html3d-height-0_5");
		if (isHanging) classes.add("hanging");
		classes.add("html3d-ry-" + formatDegrees(yaw));
		return classes;
	}

	private HtmlMarker buildMarker(String label, String html, List<String> classes, Vector3d pos, Config config) {
		final HtmlMarker htmlMarker = HtmlMarker.builder()
				.label(label)
				.position(pos)
				.html(html)
				.styleClasses(classes.toArray(new String[0]))
				.build();
		if (config.getMaxDistance() > 0) htmlMarker.setMaxDistance(config.getMaxDistance());
		return htmlMarker;
	}

	private @NotNull String getLabel() {
		if (front.isWrittenOn()) {
			final @Nullable String frontLabel = front.getLabel();
			if (frontLabel != null) return frontLabel;
		}
		if (back != null && back.isWrittenOn()) {
			final @Nullable String backLabel = back.getLabel();
			if (backLabel != null) return backLabel;
		}
		return "Blank sign at " + blockX + ", " + blockY + ", " + blockZ;
	}

	private static String formatDegrees(double degrees) {
		if (degrees == Math.floor(degrees)) return String.valueOf((int) degrees);
		return String.valueOf(degrees).replace('.', '_');
	}
}
