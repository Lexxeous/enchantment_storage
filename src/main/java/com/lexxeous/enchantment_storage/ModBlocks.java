package com.lexxeous.enchantment_storage;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModBlocks {
    private ModBlocks() {}

    public static void init() {
        // Call this from your mod initializer to force class load, if you like.
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
}
