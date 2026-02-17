package com.lexxeous.enchantment_storage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

class EnchantmentStorageTextUtilsTest {
	// region Constants
	// region Vanilla
	private static final Identifier SHARPNESS = Identifier.of("minecraft", "sharpness");
	// endregion

	// region Modded
	private static final Identifier ARIALS_BANE = Identifier.of("other_mod_namespace1", "arials_bane");
	private static final Identifier WITHERING = Identifier.of("other_mod_namespace2", "withering");
	private static final Identifier DECAY = Identifier.of("other_mod_namespace3", "decay");
	// endregion
	// endregion

	// region Confidence
	// region Vanilla
	@Test
	void formatFallbackName_handlesNullEmptyAndDelimitedNames() {
		assertEquals("", EnchantmentStorageTextUtils.formatFallbackName(null));
		assertEquals("", EnchantmentStorageTextUtils.formatFallbackName(""));
		assertEquals("Fire Aspect", EnchantmentStorageTextUtils.formatFallbackName("fire_aspect"));
	}

	@Test
	void joinWithCommasAnd_formatsByItemCount() {
		assertEquals("", EnchantmentStorageTextUtils.joinWithCommasAnd(null));
		assertEquals("", EnchantmentStorageTextUtils.joinWithCommasAnd(List.of()));
		assertEquals("Sharpness", EnchantmentStorageTextUtils.joinWithCommasAnd(List.of("Sharpness")));
		assertEquals("Sharpness & Smite", EnchantmentStorageTextUtils.joinWithCommasAnd(List.of("Sharpness", "Smite")));
		assertEquals(
			"Sharpness, Smite, & Looting",
			EnchantmentStorageTextUtils.joinWithCommasAnd(List.of("Sharpness", "Smite", "Looting"))
		);
	}
	// endregion

	// region Modded
	@Test
	void joinWithCommasAnd_formatsCustomModdedNames() {
		assertEquals(
			"Arial's Bane, Withering, & Decay",
			EnchantmentStorageTextUtils.joinWithCommasAnd(List.of("Arial's Bane", "Withering", "Decay"))
		);
	}
	// endregion
	// endregion

	// region Regression
	// region Vanilla
	@Test
	void displayEnchantmentName_handlesVanillaEnchantments() {
		String display = EnchantmentStorageTextUtils.displayEnchantmentName(SHARPNESS);
		assertFalse(display.isEmpty());
		assertEquals("Sharpness", display);
	}
	// endregion

	// region Modded
	@Test
	void displayEnchantmentName_usesFallbackForUnknownTranslation() {
		String display = EnchantmentStorageTextUtils.displayEnchantmentName(ARIALS_BANE);
		assertFalse(display.isEmpty());
		assertEquals("Arials Bane", display);
	}

	@Test
	void displayEnchantmentName_handlesOtherCustomNames() {
		assertEquals("Withering", EnchantmentStorageTextUtils.displayEnchantmentName(WITHERING));
		assertEquals("Decay", EnchantmentStorageTextUtils.displayEnchantmentName(DECAY));
	}
	// endregion
	// endregion
}
