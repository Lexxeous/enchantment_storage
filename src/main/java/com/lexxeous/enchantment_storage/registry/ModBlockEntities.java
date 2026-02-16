package com.lexxeous.enchantment_storage.registry;

import com.lexxeous.enchantment_storage.EnchantmentStorage;
import com.lexxeous.enchantment_storage.blockentity.EnchantmentExtractorBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
	// region Constants
	public static final Identifier ENCHANTMENT_EXTRACTOR_BLOCK_ENTITY_ID =
		Identifier.of(EnchantmentStorage.MOD_ID, "enchantment_extractor");
	// endregion

	// region Class Variables
	public static final BlockEntityType<EnchantmentExtractorBlockEntity> ENCHANTMENT_EXTRACTOR =
		Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			ENCHANTMENT_EXTRACTOR_BLOCK_ENTITY_ID,
			FabricBlockEntityTypeBuilder
				.create(EnchantmentExtractorBlockEntity::new, ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK)
				.build()
		);
	// endregion

	// region Constructors
	private ModBlockEntities() {}
	// endregion

	// region Registration & Initialization
	public static void register() {
		// Static holder.
	}
	// endregion
}