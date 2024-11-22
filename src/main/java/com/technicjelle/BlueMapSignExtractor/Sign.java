package com.technicjelle.BlueMapSignExtractor;


import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.markers.HtmlMarker;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.core.world.block.entity.SignBlockEntity;
import org.jetbrains.annotations.NotNull;

public class Sign {
	private final SignSide front;
	private final SignSide back;
	private final Vector3d position;

	public Sign(SignBlockEntity signBlockEntity) {
		this.front = new SignSide(signBlockEntity.getFrontText());
		this.back = new SignSide(signBlockEntity.getBackText());
		this.position = new Vector3d(signBlockEntity.getX() + 0.5, signBlockEntity.getY() + 0.5, signBlockEntity.getZ() + 0.5); // centre of the block
	}

	public boolean isBlank() {
		return !front.isWrittenOn() && !back.isWrittenOn();
	}

	public Marker createMarker(Config config) {
		final HtmlMarker htmlMarker = HtmlMarker.builder()
				.label(getLabel())
				.position(position)
				.html(getFormattedHTML())
				.styleClasses("sign")
				.build();

		if (config.getMaxDistance() > 0) {
			htmlMarker.setMaxDistance(config.getMaxDistance());
		}

		return htmlMarker;
	}

	public String createKey(String prefix) {
		return prefix + position;
	}

	private String getFormattedHTML() {
		final StringBuilder sb = new StringBuilder();
		if (front.isWrittenOn()) {
			sb.append(front.getFormattedHTML());
		}
		if (back.isWrittenOn()) {
			sb.append(back.getFormattedHTML());
		}
		return sb.toString().stripTrailing();
	}

	private @NotNull String getLabel() {
		if (front.isWrittenOn()) {
			final String frontLabel = front.getLabel();
			if (frontLabel != null) return frontLabel;
		}
		if (back.isWrittenOn()) {
			final String backLabel = back.getLabel();
			if (backLabel != null) return backLabel;
		}
		return "Blank sign at " + position;
	}
}
