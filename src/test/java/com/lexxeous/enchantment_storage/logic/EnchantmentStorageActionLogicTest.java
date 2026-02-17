package com.lexxeous.enchantment_storage.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EnchantmentStorageActionLogicTest {
	// region Confidence
	@Test
	void getDiscountedCost_clampsToZeroAndHandlesNegativeLapis() {
		assertEquals(0, EnchantmentStorageActionLogic.getDiscountedCost(0, 5));
		assertEquals(4, EnchantmentStorageActionLogic.getDiscountedCost(4, -1));
		assertEquals(0, EnchantmentStorageActionLogic.getDiscountedCost(2, 8));
	}

	@Test
	void canAffordExperience_respectsCreativeAndLevels() {
		assertTrue(EnchantmentStorageActionLogic.canAffordExperience(false, 0, 0));
		assertTrue(EnchantmentStorageActionLogic.canAffordExperience(true, 0, 999));
		assertTrue(EnchantmentStorageActionLogic.canAffordExperience(false, 7, 7));
		assertFalse(EnchantmentStorageActionLogic.canAffordExperience(false, 6, 7));
	}
	// endregion

	// region Regression
	@Test
	void storeAndExtractButtonActiveness_followBusinessRules() {
		assertFalse(EnchantmentStorageActionLogic.isStoreButtonActive(false, 9, 0, false, 30));
		assertTrue(EnchantmentStorageActionLogic.isStoreButtonActive(true, 9, 2, false, 7));
		assertFalse(EnchantmentStorageActionLogic.isStoreButtonActive(true, 9, 2, false, 6));

		assertFalse(EnchantmentStorageActionLogic.isExtractButtonActive(true, true, 3, 0, false, 10));
		assertFalse(EnchantmentStorageActionLogic.isExtractButtonActive(false, false, 3, 0, false, 10));
		assertTrue(EnchantmentStorageActionLogic.isExtractButtonActive(true, false, 3, 1, false, 2));
	}

	@Test
	void storeAndExtractButtonActiveness_treatNonPositiveCostsAsAffordable() {
		assertTrue(EnchantmentStorageActionLogic.isStoreButtonActive(true, 0, 0, false, 0));
		assertTrue(EnchantmentStorageActionLogic.isStoreButtonActive(true, -10, 0, false, 0));

		assertTrue(EnchantmentStorageActionLogic.isExtractButtonActive(true, false, 0, 0, false, 0));
		assertTrue(EnchantmentStorageActionLogic.isExtractButtonActive(true, false, -3, 0, false, 0));
	}
	// endregion

	// region Smoke
	@Test
	void storeAndExtractEligibility_guardsInvalidInputs() {
		assertFalse(EnchantmentStorageActionLogic.canStore(true, true, true));
		assertFalse(EnchantmentStorageActionLogic.canStore(false, false, false));
		assertTrue(EnchantmentStorageActionLogic.canStore(false, true, false));
		assertTrue(EnchantmentStorageActionLogic.canStore(false, false, true));

		assertFalse(EnchantmentStorageActionLogic.canExtractSelection(false, 0, 1));
		assertFalse(EnchantmentStorageActionLogic.canExtractSelection(true, -1, 1));
		assertFalse(EnchantmentStorageActionLogic.canExtractSelection(true, 0, 0));
		assertTrue(EnchantmentStorageActionLogic.canExtractSelection(true, 0, 1));

		assertFalse(EnchantmentStorageActionLogic.canExtractInput(true, true, false, false));
		assertFalse(EnchantmentStorageActionLogic.canExtractInput(false, false, false, false));
		assertFalse(EnchantmentStorageActionLogic.canExtractInput(false, true, false, true));
		assertTrue(EnchantmentStorageActionLogic.canExtractInput(false, true, false, false));
	}
	// endregion
}
