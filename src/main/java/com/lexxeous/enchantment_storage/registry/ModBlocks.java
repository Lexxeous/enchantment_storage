package com.lexxeous.enchantment_storage.registry;

import com.lexxeous.enchantment_storage.EnchantmentStorage;
import com.lexxeous.enchantment_storage.block.EnchantmentExtractorBlock;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModBlocks implements ModInitializer {
    private ModBlocks() {}

    public static void init() {
        // Call this from your mod initializer to force class load, if you like.
    }

    @Override
    public void onInitialize() {
        ModBlocks.init();
    }

    public static final Identifier ENCHANTMENT_EXTRACTOR_BLOCK_ID =
            Identifier.of(EnchantmentStorage.MOD_ID, "enchantment_extractor");

    public static final RegistryKey<Block> ENCHANTMENT_EXTRACTOR_BLOCK_KEY =
            RegistryKey.of(RegistryKeys.BLOCK, ENCHANTMENT_EXTRACTOR_BLOCK_ID);

    public static final Block ENCHANTMENT_EXTRACTOR_BLOCK =
            new EnchantmentExtractorBlock(AbstractBlock.Settings.create()
                    .registryKey(ENCHANTMENT_EXTRACTOR_BLOCK_KEY)
                    .strength(5.0f, 6.0f)
                    .requiresTool()
            );

    public static void register() {
        Registry.register(
                Registries.BLOCK,
                ENCHANTMENT_EXTRACTOR_BLOCK_ID,
                ENCHANTMENT_EXTRACTOR_BLOCK
        );
    }

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(EnchantmentStorage.MOD_ID, name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(EnchantmentStorage.MOD_ID, name));
    }
}
