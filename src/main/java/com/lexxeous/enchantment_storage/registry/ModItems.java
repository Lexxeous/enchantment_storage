package com.lexxeous.enchantment_storage.registry;

import com.lexxeous.enchantment_storage.item.EnchantmentExtractorBlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class ModItems {
	// region Constants
	public static final RegistryKey<Item> ENCHANTMENT_EXTRACTOR_ITEM_KEY =
		RegistryKey.of(RegistryKeys.ITEM, ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK_ID);
	// endregion

	// region Class Variables
	public static final Item ENCHANTMENT_EXTRACTOR_ITEM =
		new EnchantmentExtractorBlockItem(
			ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK,
			new Item.Settings().registryKey(ENCHANTMENT_EXTRACTOR_ITEM_KEY)
		);
	// endregion

	// region Constructors
	private ModItems() {}
	// endregion

	// region Registration & Initialization
	public static void register() {
		Registry.register(Registries.ITEM, ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK_ID, ENCHANTMENT_EXTRACTOR_ITEM);
	}
	// endregion
}