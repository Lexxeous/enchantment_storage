package com.lexxeous.enchantment_storage.mapping;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

class EnchantmentCategoryTest {
	// region Constants
	// region Vanilla
	private static final Identifier SHARPNESS = Identifier.of("minecraft", "sharpness");
	// endregion

	// region Modded
	private static final Identifier WITHERING = Identifier.of("other_mod_namespace2", "withering");
	// endregion
	// endregion

	// region Confidence
	// region Vanilla
	@Test
	void constructorWithLevels_clampsToRangeAndMaxLevels() {
		byte[] raw = new byte[] {(byte) 250, 1, 2, 3, 4, 5, 6};
		EnchantmentCategory category = new EnchantmentCategory(SHARPNESS, raw);

		assertArrayEquals(new byte[] {(byte) 99, 1, 2, 3, 4}, category.getLevelsCopy());
		assertEquals(99, category.getCount(0));
		assertEquals(1, category.getCount(1));
		assertEquals(2, category.getCount(2));
		assertEquals(3, category.getCount(3));
		assertEquals(4, category.getCount(4));
		assertEquals(99 + 1 + 2 + 3 + 4, category.getTotalCount());
	}
	// endregion

	// region Modded
	@Test
	void constructorWithLevels_supportsModdedEnchantmentIdentifiers() {
		EnchantmentCategory category = new EnchantmentCategory(WITHERING, new byte[] {2, 0, 1, 0, 0});
		assertEquals(WITHERING, category.getEnchantmentId());
		assertEquals(3, category.getTotalCount());
	}
	// endregion

	@Test
	void incrementAndSetCount_clampValuesAndReturnDelta() {
		EnchantmentCategory category = new EnchantmentCategory(SHARPNESS);
		assertEquals(50, category.increment(0, 50));
		assertEquals(50, category.getCount(0));

		assertEquals(49, category.increment(0, 100));
		assertEquals(99, category.getCount(0));

		category.setCount(0, -5);
		assertEquals(0, category.getCount(0));
	}
	// endregion

	// region Regression
	@Test
	void levelIndexValidation_throwsForOutOfRangeIndexes() {
		EnchantmentCategory category = new EnchantmentCategory(SHARPNESS);
		assertThrows(IllegalArgumentException.class, () -> category.getCount(-1));
		assertThrows(IllegalArgumentException.class, () -> category.setCount(EnchantmentCategory.MAX_LEVELS, 1));
	}

	@Test
	void emptyState_reflectsCurrentLevelValues() {
		EnchantmentCategory category = new EnchantmentCategory(SHARPNESS);
		assertTrue(category.isEmpty());

		category.setCount(1, 4);
		assertFalse(category.isEmpty());

		category.setCount(1, 0);
		assertTrue(category.isEmpty());
	}
	// endregion
}
