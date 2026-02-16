package com.lexxeous.enchantment_storage.util;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class EnchantmentStorageUtils {
	// region Constructors
	private EnchantmentStorageUtils() {}
	// endregion

	// region Helpers
	public static boolean hasEnchantment(@Nullable ItemStack stack, @Nullable Identifier enchantmentId) {
		if (stack == null || stack.isEmpty() || enchantmentId == null) {
			return false;
		}

		ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
		if (enchantments.getEnchantments().isEmpty()) {
			return false;
		}

		for (var entry : enchantments.getEnchantmentEntries()) {
			var id = entry.getKey().getKey()
				.map(net.minecraft.registry.RegistryKey::getValue)
				.orElse(null);
			if (enchantmentId.equals(id)) {
				return true;
			}
		}
		return false;
	}

	public static int getEnchantmentLevelTotal(@Nullable ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return 0;
		}

		ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
		if (enchantments.getEnchantments().isEmpty()) {
			return 0;
		}

		int total = 0;
		for (var entry : enchantments.getEnchantmentEntries()) {
			total += entry.getIntValue();
		}
		return total;
	}

	// endregion
}
