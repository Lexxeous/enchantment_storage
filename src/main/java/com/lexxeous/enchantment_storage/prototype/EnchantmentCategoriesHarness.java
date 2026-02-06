package com.lexxeous.enchantment_storage.prototype;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public final class EnchantmentCategoriesHarness {
    private EnchantmentCategoriesHarness() {}

    public static void runDemo() {
        EnchantmentCategories categories = new EnchantmentCategories();
        categories.increment(Identifier.of("minecraft", "efficiency"), 4, 10);
        categories.increment(Identifier.of("minecraft", "unbreaking"), 2, 3);
        categories.increment(Identifier.of("examplemod", "withering"), 0, 1);

        NbtCompound tag = categories.toNbt();
        EnchantmentCategories reloaded = new EnchantmentCategories();
        reloaded.readNbt(tag);

        EnchantmentCategories.NameResolver resolver = id -> id.getPath();
        for (Identifier id : reloaded.getSortedEnchantments(resolver)) {
            int total = reloaded.getTotalForEnchantment(id);
            System.out.println(id + " => " + total);
        }
    }
}
