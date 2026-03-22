package com.technicjelle.BlueMapSignExtractor;

import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.markers.POIMarker;
import de.bluecolored.bluemap.core.world.mca.blockentity.SignBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sign {
	private static final Pattern GROUP_PATTERN = Pattern.compile("^\\[([a-zA-Z0-9-]+)]$");

	private final @NotNull SignSide front;
	private final @Nullable SignSide back;
	private final Vector3d position;

	public Sign(SignBlockEntity signBlockEntity, int dataVersion) {
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
		this.position = new Vector3d(signBlockEntity.getX() + 0.5, signBlockEntity.getY() + 0.5, signBlockEntity.getZ() + 0.5);
	}

	/**
	 * Parses a group name from a line of text.
	 * The text must match [groupName] where groupName is alphanumeric plus hyphens.
	 * Returns the group name lowercased, or empty if not a valid group tag.
	 */
	static Optional<String> parseGroupName(String text) {
		if (text == null) return Optional.empty();
		Matcher matcher = GROUP_PATTERN.matcher(text.trim());
		if (matcher.matches()) {
			return Optional.of(matcher.group(1).toLowerCase());
		}
		return Optional.empty();
	}

	/**
	 * Extracts the group name from the first line of the front side.
	 */
	public Optional<String> getGroupName() {
		List<SignLine> messages = front.getMessages();
		if (messages.isEmpty()) return Optional.empty();

		SignLine firstLine = messages.get(0);
		if (firstLine == null) return Optional.empty();

		return parseGroupName(firstLine.getPlainText());
	}

	/**
	 * Returns the POI label: the first non-blank line after the [group] tag (lines 2-4).
	 * Falls back to the group name if all remaining lines are blank.
	 */
	public String getPoiLabel() {
		List<SignLine> messages = front.getMessages();
		// Lines 2-4 (index 1-3)
		for (int i = 1; i < messages.size(); i++) {
			SignLine line = messages.get(i);
			if (line != null && !line.getPlainText().isBlank()) {
				return line.getPlainText().trim();
			}
		}
		// Fall back to group name
		return getGroupName().orElse("Sign at " + position);
	}

	/**
	 * Returns HTML detail content from front side lines 2-4.
	 * TODO: Consider including back-side text in the future
	 */
	public String getDetailHtml() {
		List<SignLine> messages = front.getMessages();
		StringBuilder sb = new StringBuilder();
		// Lines 2-4 (index 1-3)
		for (int i = 1; i < messages.size(); i++) {
			SignLine line = messages.get(i);
			if (line != null && !line.getPlainText().isBlank()) {
				sb.append(escapeHtml(line.getPlainText().trim()));
			}
			if (i < messages.size() - 1) {
				sb.append("<br>");
			}
		}
		return sb.toString();
	}

	/**
	 * Creates a POI marker for this group sign.
	 */
	public POIMarker createGroupMarker(Config config) {
		POIMarker.Builder builder = POIMarker.builder()
				.label(getPoiLabel())
				.position(position)
				.detail(getDetailHtml());

		if (config.getMaxDistance() > 0) {
			builder.maxDistance(config.getMaxDistance());
		}

		return builder.build();
	}

	public String createKey(String prefix) {
		return prefix + position;
	}

	private static String escapeHtml(String text) {
		return text.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
}
