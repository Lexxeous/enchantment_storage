package com.lexxeous.enchantment_storage;

import com.lexxeous.enchantment_storage.item.EnchantmentExtractorBlockItem;
import com.lexxeous.enchantment_storage.registry.ModBlockEntities;
import com.lexxeous.enchantment_storage.registry.ModItems;
import com.lexxeous.enchantment_storage.registry.ModScreenHandlers;
import com.lexxeous.enchantment_storage.render.block.entity.EnchantmentExtractorEyeRenderer;
import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorClientScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public final class EnchantmentStorageClient implements ClientModInitializer {
	// region Overrides
	@Override
	public void onInitializeClient() {
		HandledScreens.register(
			ModScreenHandlers.ENCHANTMENT_EXTRACTOR,
			EnchantmentExtractorClientScreen::new
		);

		BlockEntityRendererFactories.register(
			ModBlockEntities.ENCHANTMENT_EXTRACTOR,
			EnchantmentExtractorEyeRenderer::new
		);

		ItemTooltipCallback.EVENT.register((stack, context, tooltipType, lines) -> {
			if (!stack.isOf(ModItems.ENCHANTMENT_EXTRACTOR_ITEM)) {
				return;
			}
			EnchantmentExtractorBlockItem.appendStoredEnchantmentsTooltip(stack, lines);
		});
	}
	// endregion
}