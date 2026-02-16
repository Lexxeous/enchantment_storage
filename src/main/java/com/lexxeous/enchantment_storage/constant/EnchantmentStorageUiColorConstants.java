package com.lexxeous.enchantment_storage.constant;

public final class EnchantmentStorageUiColorConstants {
	// region Constants
	public static final int INFO_TEXT = 0x404040; // dark gray
	public static final int EXPERIENCE_AFFORDABLE = 0x80FF20; // bright lime green
	public static final int EXPERIENCE_UNAFFORDABLE = 0xFF6060; // light red

	public static final int LIST_ROW_SELECTED_BG = 0xBBBBBB; // medium light gray
	public static final int LIST_ROW_BG = 0xDDDDDD; // light gray
	public static final int TEXT_PRIMARY = 0x000000; // black

	public static final int RANK_OUTER_ACTIVE = 0x6F6F6F; // medium gray
	public static final int RANK_OUTER_INACTIVE = 0x2F2F2F; // dark charcoal gray
	public static final int RANK_TEXT_ACTIVE = 0xFFFFFF; // white
	public static final int RANK_TEXT_INACTIVE = 0xA0A0A0; // neutral gray

	public static final int RANK_BORDER_DEFAULT = 0x000000; // black
	public static final int RANK_BORDER_HOVER = 0xFFFFFF; // white
	public static final int RANK_BORDER_SELECTED = 0x3F76E4; // medium cornflower blue

	public static final int SCROLL_BG = 0xCCCCCC; // light gray
	public static final int SCROLL_BG_DISABLED = 0x999999; // medium gray
	public static final int SCROLL_THUMB = 0x888888; // darker medium gray
	// endregion

	// region Helpers
	public static int withArgb(int rgb) {
		return 0xFF000000 | (rgb & 0x00FFFFFF);
	}
	// endregion

	// region Constructors
	private EnchantmentStorageUiColorConstants() {}
	// endregion
}