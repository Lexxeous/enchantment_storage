package com.lexxeous.enchantment_storage.util;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class EnchantmentStorageUtils {
    private EnchantmentStorageUtils() {}

    public static String formatFallbackName(String path) {
        if (path == null || path.isEmpty()) return "";
        String[] parts = path.split("_");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            if (i > 0) builder.append(' ');
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) builder.append(part.substring(1));
        }
        return builder.toString();
    }

    public static boolean hasEnchantment(ItemStack stack, Identifier enchantmentId) {
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

    public static int getEnchantmentLevelTotal(ItemStack stack) {
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
}
