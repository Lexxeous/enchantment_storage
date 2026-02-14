package com.lexxeous.enchantment_storage.registry;

import com.lexxeous.enchantment_storage.EnchantmentStorage;
import com.lexxeous.enchantment_storage.item.EnchantmentExtractorBlockItem;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems implements ModInitializer {
    private ModItems() {}

    public static void init() {
        // Call this from your mod initializer to force class load, if you like.
    }

    @Override
    public void onInitialize() {
        ModItems.init();
    }

    public static final RegistryKey<Item> ENCHANTMENT_EXTRACTOR_ITEM_KEY =
            RegistryKey.of(
                    RegistryKeys.ITEM,
                    ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK_ID
            );

    public static final Item ENCHANTMENT_EXTRACTOR_ITEM =
            new EnchantmentExtractorBlockItem(
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

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(EnchantmentStorage.MOD_ID, name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(EnchantmentStorage.MOD_ID, name));
    }
}
