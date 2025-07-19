package com.technicjelle.BlueMapSignExtractor;


import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.markers.HtmlMarker;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.core.world.mca.blockentity.SignBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Sign {
	private final @Nullable SignSide front;
	private final @Nullable SignSide back;
	private final Vector3d position;

	public Sign(SignBlockEntity signBlockEntity, int dataVersion) {
		@Nullable SignBlockEntity.TextData frontText = signBlockEntity.getFrontText();
		if (frontText == null) {
			this.front = null;
		} else {
			this.front = new SignSide(frontText, dataVersion);
		}
		@Nullable SignBlockEntity.TextData backText = signBlockEntity.getBackText();
		if (backText == null) {
			this.back = null;
		} else {
			this.back = new SignSide(backText, dataVersion);
		}
		this.position = new Vector3d(signBlockEntity.getX() + 0.5, signBlockEntity.getY() + 0.5, signBlockEntity.getZ() + 0.5); // centre of the block
	}

	public boolean isBlank() {
		//if the front is null or has not been written on AND if the back is null or has not been written on
		return (front == null || !front.isWrittenOn()) && (back == null || !back.isWrittenOn());
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
		if (front != null && front.isWrittenOn()) {
			sb.append(front.getFormattedHTML());
		}
		if (back != null && back.isWrittenOn()) {
			sb.append(back.getFormattedHTML());
		}
		return sb.toString().stripTrailing();
	}

	private @NotNull String getLabel() {
		if (front != null && front.isWrittenOn()) {
			final @Nullable String frontLabel = front.getLabel();
			if (frontLabel != null) return frontLabel;
		}
		if (back != null && back.isWrittenOn()) {
			final @Nullable String backLabel = back.getLabel();
			if (backLabel != null) return backLabel;
		}
		return "Blank sign at " + position;
	}
}
