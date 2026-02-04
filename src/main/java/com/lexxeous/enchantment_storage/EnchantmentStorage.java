package com.lexxeous.enchantment_storage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantmentStorage implements ModInitializer {
	public static final String MOD_ID = "enchantment_storage";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.

		ServerLifecycleEvents.SERVER_STARTED.register(server ->
			LOGGER.info("Server started. Enchantment storage mod loaded.")
		);

		ModBlocks.register();
		ModItems.register();
		LOGGER.info("Registration complete for {}", MOD_ID);

		ModBlocks.init();
		ModItems.init();
		ModBlockEntities.init();
		ModScreenHandlers.init();
		LOGGER.info("Initialization complete for {}", MOD_ID);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
			.register(entries ->
				entries.add(ModItems.ENCHANTMENT_EXTRACTOR_ITEM)
			);

	}
}