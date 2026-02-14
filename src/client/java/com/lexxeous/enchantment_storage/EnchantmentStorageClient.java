package com.lexxeous.enchantment_storage;

import com.lexxeous.enchantment_storage.registry.ModBlockEntities;
import com.lexxeous.enchantment_storage.registry.ModScreenHandlers;
import com.lexxeous.enchantment_storage.render.block.entity.EnchantmentExtractorEyeRenderer;
import com.lexxeous.enchantment_storage.screen.EnchantmentStorageClientScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public final class EnchantmentStorageClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(
            ModScreenHandlers.ENCHANTMENT_EXTRACTOR,
            EnchantmentStorageClientScreen::new
        );

        BlockEntityRendererRegistry.register(
            ModBlockEntities.ENCHANTMENT_EXTRACTOR,
            EnchantmentExtractorEyeRenderer::new
        );
    }
}
