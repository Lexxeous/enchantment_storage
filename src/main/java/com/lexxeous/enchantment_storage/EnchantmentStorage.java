package com.lexxeous.enchantment_storage;

import com.lexxeous.enchantment_storage.debug.DebugCommands;
import com.lexxeous.enchantment_storage.registry.ModBlockEntities;
import com.lexxeous.enchantment_storage.registry.ModBlocks;
import com.lexxeous.enchantment_storage.registry.ModItems;
import com.lexxeous.enchantment_storage.registry.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantmentStorage implements ModInitializer {
	// region Constants
	public static final String MOD_ID = "enchantment_storage";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	// endregion

	// region Logging
	public static void logDebugDev(String message, Object... args) {
		if (FabricLoader.getInstance().isDevelopmentEnvironment() && LOGGER.isDebugEnabled()) {
			LOGGER.debug(message, args);
		}
	}
	// endregion

	// region Overrides
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.

		// region Registration & Initialization
		ServerLifecycleEvents.SERVER_STARTED.register(server ->
			LOGGER.info("Server started. Lexxeous' Enchantment Storage mod loaded.")
		);

		ModBlocks.register();
		ModItems.register();
		ModBlockEntities.register();
		ModScreenHandlers.register();
		LOGGER.info("Registration complete for '{}'.", MOD_ID);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
			.register(entries ->
				entries.add(ModItems.ENCHANTMENT_EXTRACTOR_ITEM)
			);
		// endregion

		// region Debug
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			registerDebugCommands();
		}
		// endregion
	}
	// endregion

	// region Helpers
	private void registerDebugCommands() {
		DebugCommands.register();
	}
	// endregion
}
