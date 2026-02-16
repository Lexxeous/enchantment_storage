package com.lexxeous.enchantment_storage.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EnchantmentStorageUiLogicTest {
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
	void computeExperienceCostLine_hasExpectedPrefix() {
		assertEquals("Experience Cost: 15", EnchantmentStorageUiLogic.computeExperienceCostLine("15"));
	}
}
