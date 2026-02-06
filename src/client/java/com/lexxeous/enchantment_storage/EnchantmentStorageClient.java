package com.lexxeous.enchantment_storage;

import com.lexxeous.enchantment_storage.registry.ModScreenHandlers;
import com.lexxeous.enchantment_storage.screen.EnchantmentStorageClientScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class EnchantmentStorageClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HandledScreens.register(ModScreenHandlers.ENCHANTMENT_EXTRACTOR, EnchantmentStorageClientScreen::new);
	}
}