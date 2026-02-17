package com.lexxeous.enchantment_storage;

import com.lexxeous.enchantment_storage.mapping.EnchantmentCategories;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

public final class EnchantmentStorageCategoriesGameTest {
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

	// region Integration
	// region Vanilla
	@GameTest(structure = "fabric-gametest-api-v1:empty")
	public void categoriesIgnoreMalformedEntriesAtRuntime(TestContext context) {
		NbtCompound root = new NbtCompound();
		NbtList entries = new NbtList();
		entries.add(buildEntry(SHARPNESS.toString(), new byte[] {2, 0, 0, 0, 0}));
		entries.add(buildEntry("invalid id", new byte[] {4, 0, 0, 0, 0}));
		entries.add(buildEntry("minecraft:sharpness", new byte[0]));
		root.put("categories", entries);

		EnchantmentCategories categories = new EnchantmentCategories();
		categories.readNbt(root);

		if (categories.getTotalForEnchantment(SHARPNESS) != 2) {
			throw new IllegalStateException("Expected sharpness total of 2 after malformed entry filtering.");
		}

		context.complete();
	}
	// endregion

	// region Modded
	@GameTest(structure = "fabric-gametest-api-v1:empty")
	public void categoriesPreserveModdedNamespacesAtRuntime(TestContext context) {
		EnchantmentCategories categories = new EnchantmentCategories();
		categories.increment(ARIALS_BANE, 0, 1);
		categories.increment(WITHERING, 1, 2);
		categories.increment(DECAY, 2, 3);

		NbtCompound nbt = categories.toNbt();
		EnchantmentCategories restored = new EnchantmentCategories();
		restored.readNbt(nbt);

		if (restored.getTotalForEnchantment(ARIALS_BANE) != 1) {
			throw new IllegalStateException("Expected Arial's Bane total of 1.");
		}
		if (restored.getTotalForEnchantment(WITHERING) != 2) {
			throw new IllegalStateException("Expected Withering total of 2.");
		}
		if (restored.getTotalForEnchantment(DECAY) != 3) {
			throw new IllegalStateException("Expected Decay total of 3.");
		}

		context.complete();
	}
	// endregion
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