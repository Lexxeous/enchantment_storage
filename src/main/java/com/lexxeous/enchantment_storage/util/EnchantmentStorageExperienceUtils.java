package com.lexxeous.enchantment_storage.util;

import com.lexxeous.enchantment_storage.logic.EnchantmentStorageActionLogic;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class EnchantmentStorageExperienceUtils {
	// region Constructors
	private EnchantmentStorageExperienceUtils() {}
	// endregion

	// region Helpers
	public static int getDiscountedCost(int baseCost, ItemStack lapisStack) {
		return EnchantmentStorageActionLogic.getDiscountedCost(baseCost, getLapisCount(lapisStack));
	}

	public static int getLapisDiscountUsed(int baseCost, ItemStack lapisStack) {
		if (baseCost <= 0) {
			return 0;
		}
		return Math.min(baseCost, getLapisCount(lapisStack));
	}

	public static boolean canAffordExperience(@Nullable PlayerEntity player, int experienceCost) {
		if (player == null) {
			return true;
		}
		return EnchantmentStorageActionLogic.canAffordExperience(
			player.isCreative(),
			player.experienceLevel,
			experienceCost
		);
	}

	public static int getStoreExperienceCost(@Nullable ItemStack input) {
		int total = EnchantmentStorageUtils.getEnchantmentLevelTotal(input);
		return total > 0 ? total : -1;
	}

	private static int getLapisCount(@Nullable ItemStack lapisStack) {
		if (lapisStack == null || lapisStack.isEmpty()) {
			return 0;
		}
		return lapisStack.getCount();
	}
	// endregion
}
