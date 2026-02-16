package com.lexxeous.enchantment_storage.util;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class EnchantmentStorageCacheUtils {
	// region Constructors
	private EnchantmentStorageCacheUtils() {}
	// endregion

	public static final class StoreCostCache {
		// region Class Variables
		private ItemStack cachedInput = ItemStack.EMPTY;
		private int cachedStoreCost = -1;
		// endregion

		// region Getters & Setters
		public int get(@Nullable ItemStack input) {
			if (input == null || input.isEmpty()) {
				cachedInput = ItemStack.EMPTY;
				cachedStoreCost = -1;
				return -1;
			}

			if (!cachedInput.isEmpty() && ItemStack.areItemsAndComponentsEqual(cachedInput, input)) {
				return cachedStoreCost;
			}

			cachedInput = input.copyWithCount(1);
			cachedStoreCost = EnchantmentStorageExperienceUtils.getStoreExperienceCost(input);
			return cachedStoreCost;
		}
		// endregion
	}
}
