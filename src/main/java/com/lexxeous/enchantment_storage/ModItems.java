package com.lexxeous.enchantment_storage;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class ModItems {
    private ModItems() {}

    public static void init() {
        // Call this from your mod initializer to force class load, if you like.
    }

    public static final RegistryKey<Item> ENCHANTMENT_EXTRACTOR_ITEM_KEY =
            RegistryKey.of(
                    RegistryKeys.ITEM,
                    ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK_ID
            );

    public static final Item ENCHANTMENT_EXTRACTOR_ITEM =
            new BlockItem(
                    ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK,
                    new Item.Settings().registryKey(ENCHANTMENT_EXTRACTOR_ITEM_KEY)
            );

    public static void register() {
        Registry.register(
                Registries.ITEM,
                ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK_ID,
                ENCHANTMENT_EXTRACTOR_ITEM
        );
    }
}
