package com.lexxeous.enchantment_storage.registry;

import com.lexxeous.enchantment_storage.EnchantmentStorage;
import com.lexxeous.enchantment_storage.block.EnchantmentExtractorBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModBlocks {
	// region Constants
	public static final Identifier ENCHANTMENT_EXTRACTOR_BLOCK_ID =
		Identifier.of(EnchantmentStorage.MOD_ID, "enchantment_extractor");
	public static final RegistryKey<Block> ENCHANTMENT_EXTRACTOR_BLOCK_KEY =
		RegistryKey.of(RegistryKeys.BLOCK, ENCHANTMENT_EXTRACTOR_BLOCK_ID);
	// endregion

	// region Class Variables
	// Non-opaque to match the block shape and keep the eye render clean.
	public static final Block ENCHANTMENT_EXTRACTOR_BLOCK =
		new EnchantmentExtractorBlock(AbstractBlock.Settings.create()
			.registryKey(ENCHANTMENT_EXTRACTOR_BLOCK_KEY)
			.strength(5.0f, 1200.0f)
			.luminance(state -> 7)
			.pistonBehavior(PistonBehavior.BLOCK)
			.nonOpaque());
	// endregion

	// region Constructors
	private ModBlocks() {}
	// endregion

	// region Registration & Initialization
	public static void register() {
		Registry.register(Registries.BLOCK, ENCHANTMENT_EXTRACTOR_BLOCK_ID, ENCHANTMENT_EXTRACTOR_BLOCK);
	}
	// endregion
}