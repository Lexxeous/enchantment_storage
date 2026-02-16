package com.lexxeous.enchantment_storage.logic;

public final class EnchantmentStorageUiLogic {
	// region Constructors
	private EnchantmentStorageUiLogic() {}
	// endregion

	// region Helpers
	public static float calculateEnchantmentNameScale(int nameLength, float defaultScale) {
		float nameScale = defaultScale;
		if (nameLength > 15) {
			nameScale = 0.6f;
		}
		if (nameLength > 17) {
			nameScale = 0.53f;
		}
		return nameScale;
	}

	// region Compute
	public static int computeVisibleEntryCount(int listHeight, int listItemHeight) {
		if (listItemHeight <= 0) {
			return 0;
		}
		return listHeight / listItemHeight;
	}

	public static int computeMaxScrollOffset(int entryCount, int visibleEntries) {
		return Math.max(0, entryCount - Math.max(0, visibleEntries));
	}

	public static int clampScrollOffset(int scrollOffset, int entryCount, int visibleEntries) {
		int maxOffset = computeMaxScrollOffset(entryCount, visibleEntries);
		return Math.max(0, Math.min(maxOffset, scrollOffset));
	}

	public static int computeScrollThumbHeight(int barHeight, int visibleEntries, int entryCount, int minThumbHeight) {
		int safeBarHeight = Math.max(0, barHeight);
		int safeVisible = Math.max(0, visibleEntries);
		int safeEntries = Math.max(1, entryCount);
		return Math.max(minThumbHeight, (safeBarHeight * safeVisible) / safeEntries);
	}

	public static int computeScrollThumbY(int barY, int barHeight, int thumbHeight, int scrollOffset, int maxOffset) {
		if (maxOffset <= 0) {
			return barY;
		}
		return barY + ((barHeight - thumbHeight) * scrollOffset / maxOffset);
	}

	public static int computeScrollOffsetFromMouse(double mouseY, int barY, int barHeight, int thumbHeight, int maxOffset) {
		if (maxOffset <= 0) {
			return 0;
		}

		double track = barHeight - thumbHeight;
		if (track <= 0.0) {
			return 0;
		}

		double normalized = (mouseY - barY - (thumbHeight / 2.0)) / track;
		int next = (int) Math.round(normalized * maxOffset);
		return Math.max(0, Math.min(maxOffset, next));
	}

	public static int computeInfoFingerprint(
		String totalText,
		String levelsText,
		String experienceCostText,
		int experienceCostColor,
		boolean drawExperienceShadow
	) {
		int hash = 17;
		hash = (31 * hash) + totalText.hashCode();
		hash = (31 * hash) + levelsText.hashCode();
		hash = (31 * hash) + experienceCostText.hashCode();
		hash = (31 * hash) + experienceCostColor;
		hash = (31 * hash) + (drawExperienceShadow ? 1 : 0);
		return hash;
	}

	public static String computeExperienceCostLine(String experienceCostText) {
		return "Experience Cost: " + experienceCostText;
	}

	public static String computeTotalRemainingLine(String totalText) {
		return "Total Remaining: " + totalText;
	}

	public static String computeLevelsRemainingLine(String levelsText) {
		return "Levels Remaining: " + levelsText;
	}
	// endregion
	// endregion
}