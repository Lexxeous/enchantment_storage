package com.lexxeous.enchantment_storage;

import com.lexxeous.enchantment_storage.registry.ModBlockEntities;
import com.lexxeous.enchantment_storage.registry.ModBlocks;
import com.lexxeous.enchantment_storage.registry.ModItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.registry.Registries;
import net.minecraft.test.TestContext;

public final class EnchantmentStorageRegistryGameTest {
	// region Integration
	@GameTest(structure = "fabric-gametest-api-v1:empty")
	public void enchantmentExtractorRegistrations_exist(TestContext context) {
		if (ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK == null) {
			throw new IllegalStateException("Expected ENCHANTMENT_EXTRACTOR_BLOCK to be initialized.");
		}
		if (ModItems.ENCHANTMENT_EXTRACTOR_ITEM == null) {
			throw new IllegalStateException("Expected ENCHANTMENT_EXTRACTOR_ITEM to be initialized.");
		}
		if (ModBlockEntities.ENCHANTMENT_EXTRACTOR == null) {
			throw new IllegalStateException("Expected ENCHANTMENT_EXTRACTOR block entity type to be initialized.");
		}

		if (Registries.BLOCK.get(ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK_ID) != ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK) {
			throw new IllegalStateException("Expected block registry to contain the mod block instance.");
		}
		if (Registries.ITEM.get(ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK_ID) != ModItems.ENCHANTMENT_EXTRACTOR_ITEM) {
			throw new IllegalStateException("Expected item registry to contain the mod item instance.");
		}

		context.complete();
	}
	// endregion
}