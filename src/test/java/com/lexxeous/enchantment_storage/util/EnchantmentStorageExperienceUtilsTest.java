package com.lexxeous.enchantment_storage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EnchantmentStorageExperienceUtilsTest {
	// region Confidence
	@Test
	void canAffordExperience_treatsNullPlayerAsAffordable() {
		assertTrue(EnchantmentStorageExperienceUtils.canAffordExperience(null, 10));
	}
	// endregion

	// region Regression
	@Test
	void storeExperienceCost_returnsMinusOneWhenInputIsNull() {
		assertEquals(-1, EnchantmentStorageExperienceUtils.getStoreExperienceCost(null));
	}
	// endregion
}