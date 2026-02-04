package com.lexxeous.enchantment_storage;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    private ModBlockEntities() {}

    public static void init() {
        // Call this from your mod initializer to force class load, if you like.
    }

    public static final Identifier ENCHANTMENT_EXTRACTOR_BLOCK_ENTITY_ID =
            Identifier.of(EnchantmentStorage.MOD_ID, "enchantment_extractor");

    public static final BlockEntityType<EnchantmentExtractorBlockEntity> ENCHANTMENT_EXTRACTOR =
        Registry.register(
              Registries.BLOCK_ENTITY_TYPE,
                    ENCHANTMENT_EXTRACTOR_BLOCK_ENTITY_ID,
                    FabricBlockEntityTypeBuilder.create(EnchantmentExtractorBlockEntity::new,
                            ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK).build()
        );
}
