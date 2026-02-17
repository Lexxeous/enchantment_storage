package com.lexxeous.enchantment_storage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.junit.jupiter.api.Test;

class EnchantmentStorageCategoryUtilsTest {
	// region Constants
	// region Vanilla
	private static final String SHARPNESS_ID = "minecraft:sharpness";
	// endregion

	// region Modded
	private static final String ARIALS_BANE_ID = "other_mod_namespace1:arials_bane";
	private static final String WITHERING_ID = "other_mod_namespace2:withering";
	private static final String DECAY_ID = "other_mod_namespace3:decay";
	// endregion
	// endregion

	// region Confidence
	// region Vanilla
	@Test
	void getStoredCategoryLines_handlesNullAndParsesValidEntries() {
		assertTrue(EnchantmentStorageCategoryUtils.getStoredCategoryLines(null).isEmpty());

		NbtCompound root = new NbtCompound();
		NbtList categories = new NbtList();
		NbtCompound entry = new NbtCompound();
		entry.putString("id", SHARPNESS_ID);
		entry.put("levels", new NbtByteArray(new byte[] {1, 2, 0, 0, 0}));
		categories.add(entry);
		root.put("categories", categories);

		List<EnchantmentStorageCategoryUtils.StoredCategoryLine> lines =
			EnchantmentStorageCategoryUtils.getStoredCategoryLines(root);

		assertEquals(1, lines.size());
		assertEquals(3, lines.getFirst().count());
		assertFalse(lines.getFirst().name().isEmpty());
	}
	// endregion

	// region Modded
	@Test
	void getStoredCategoryLines_parsesCustomModdedEnchantments() {
		NbtCompound root = new NbtCompound();
		NbtList categories = new NbtList();
		categories.add(buildEntry(ARIALS_BANE_ID, new byte[] {1, 1, 0, 0, 0}));
		categories.add(buildEntry(WITHERING_ID, new byte[] {0, 0, 3, 0, 0}));
		categories.add(buildEntry(DECAY_ID, new byte[] {0, 2, 0, 0, 0}));
		root.put("categories", categories);

		List<EnchantmentStorageCategoryUtils.StoredCategoryLine> lines =
			EnchantmentStorageCategoryUtils.getStoredCategoryLines(root);

		assertEquals(3, lines.size());
		assertEquals(7, EnchantmentStorageCategoryUtils.getStoredCategoryTotal(lines));
	}
	// endregion
	// endregion

	// region Regression
	@Test
	void getStoredCategoryTotal_sumsOnlyNonNegativeCounts() {
		List<EnchantmentStorageCategoryUtils.StoredCategoryLine> lines = List.of(
			new EnchantmentStorageCategoryUtils.StoredCategoryLine("A", 2),
			new EnchantmentStorageCategoryUtils.StoredCategoryLine("B", -10),
			new EnchantmentStorageCategoryUtils.StoredCategoryLine("C", 5)
		);

		assertEquals(7, EnchantmentStorageCategoryUtils.getStoredCategoryTotal(lines));
		assertEquals(0, EnchantmentStorageCategoryUtils.getStoredCategoryTotal(null));
		assertEquals(0, EnchantmentStorageCategoryUtils.getStoredCategoryTotal(List.of()));
	}
	// endregion

	// region Helpers
	private static NbtCompound buildEntry(String id, byte[] levels) {
		NbtCompound entry = new NbtCompound();
		entry.putString("id", id);
		entry.put("levels", new NbtByteArray(levels));
		return entry;
	}
	// endregion
}
