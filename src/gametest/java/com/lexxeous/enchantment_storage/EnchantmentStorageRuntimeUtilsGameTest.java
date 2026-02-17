package com.lexxeous.enchantment_storage;

import com.lexxeous.enchantment_storage.util.EnchantmentStorageCacheUtils;
import com.lexxeous.enchantment_storage.util.EnchantmentStorageExperienceUtils;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.TestContext;

public final class EnchantmentStorageRuntimeUtilsGameTest {
	// region Integration
	@GameTest(structure = "fabric-gametest-api-v1:empty")
	public void experienceUtils_handlesDiscountAndLapisUsage(TestContext context) {
		ItemStack lapis = new ItemStack(Items.LAPIS_LAZULI, 3);

		if (EnchantmentStorageExperienceUtils.getDiscountedCost(10, lapis) != 7) {
			throw new IllegalStateException("Expected discounted cost of 7.");
		}
		if (EnchantmentStorageExperienceUtils.getLapisDiscountUsed(10, lapis) != 3) {
			throw new IllegalStateException("Expected lapis usage of 3.");
		}

		context.complete();
	}

	@GameTest(structure = "fabric-gametest-api-v1:empty")
	public void storeCostCache_handlesNullAndEmptyInput(TestContext context) {
		EnchantmentStorageCacheUtils.StoreCostCache cache = new EnchantmentStorageCacheUtils.StoreCostCache();
		if (cache.get(null) != -1) {
			throw new IllegalStateException("Expected null input to return -1.");
		}
		if (cache.get(ItemStack.EMPTY) != -1) {
			throw new IllegalStateException("Expected empty input to return -1.");
		}

		context.complete();
	}
	// endregion
}