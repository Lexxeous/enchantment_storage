package com.lexxeous.enchantment_storage.mapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class EnchantmentCategoriesHarness {
    private EnchantmentCategoriesHarness() {}

    public static EnchantmentCategories buildDemoCategories(Registry<Enchantment> registry) {
        EnchantmentCategories categories = new EnchantmentCategories();
        List<Identifier> order = getRegistryOrder(registry);
        for (int i = 0; i < order.size() && i < 4; i++) {
            Identifier id = order.get(i);
            categories.increment(id, Math.min(4, i), 1 + i);
            categories.increment(id, 0, 1);
        }
        return categories;
    }

    public static List<Identifier> getRegistryOrder(Registry<Enchantment> registry) {
        if (registry == null) {
            return List.of();
        }
        List<Identifier> ids = new ArrayList<>();
        ids.addAll(registry.getIds());
        ids.sort(Comparator.comparing(Identifier::toString));
        return ids;
    }
}
