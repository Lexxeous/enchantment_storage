package com.lexxeous.enchantment_storage.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class EnchantmentStorageUiLogicTest {
	// region Confidence
	@Test
	void computeVisibleEntryCount_returnsZeroWhenItemHeightIsNonPositive() {
		assertEquals(0, EnchantmentStorageUiLogic.computeVisibleEntryCount(60, 0));
		assertEquals(0, EnchantmentStorageUiLogic.computeVisibleEntryCount(60, -4));
	}

	@Test
	void clampScrollOffset_clampsToExpectedRange() {
		assertEquals(0, EnchantmentStorageUiLogic.clampScrollOffset(-1, 10, 4));
		assertEquals(6, EnchantmentStorageUiLogic.clampScrollOffset(10, 10, 4));
		assertEquals(3, EnchantmentStorageUiLogic.clampScrollOffset(3, 10, 4));
	}

	@Test
	void computeScrollThumbHeight_respectsSafetyBounds() {
		assertEquals(4, EnchantmentStorageUiLogic.computeScrollThumbHeight(-10, 3, 10, 4));
		assertEquals(5, EnchantmentStorageUiLogic.computeScrollThumbHeight(40, 1, 100, 5));
		assertEquals(20, EnchantmentStorageUiLogic.computeScrollThumbHeight(40, 5, 10, 4));
	}

	@Test
	void computeScrollOffsetFromMouse_clampsWithinBounds() {
		assertEquals(0, EnchantmentStorageUiLogic.computeScrollOffsetFromMouse(-999, 10, 80, 20, 10));
		assertEquals(10, EnchantmentStorageUiLogic.computeScrollOffsetFromMouse(999, 10, 80, 20, 10));
		assertEquals(0, EnchantmentStorageUiLogic.computeScrollOffsetFromMouse(25, 10, 20, 20, 10));
	}
	// endregion

	// region Regression
	@Test
	void computeExperienceCostLine_hasExpectedPrefix() {
		assertEquals("Experience Cost: 15", EnchantmentStorageUiLogic.computeExperienceCostLine("15"));
	}

	@Test
	void computeInfoFingerprint_changesWhenAnyInputChanges() {
		int base = EnchantmentStorageUiLogic.computeInfoFingerprint("T", "L", "C", 1, false);
		assertNotEquals(base, EnchantmentStorageUiLogic.computeInfoFingerprint("T2", "L", "C", 1, false));
		assertNotEquals(base, EnchantmentStorageUiLogic.computeInfoFingerprint("T", "L2", "C", 1, false));
		assertNotEquals(base, EnchantmentStorageUiLogic.computeInfoFingerprint("T", "L", "C2", 1, false));
		assertNotEquals(base, EnchantmentStorageUiLogic.computeInfoFingerprint("T", "L", "C", 2, false));
		assertNotEquals(base, EnchantmentStorageUiLogic.computeInfoFingerprint("T", "L", "C", 1, true));
	}
	// endregion
}
