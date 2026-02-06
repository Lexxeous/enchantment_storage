package com.lexxeous.enchantment_storage;

import com.lexxeous.enchantment_storage.prototype.EnchantmentCategoriesHarness;
import com.lexxeous.enchantment_storage.registry.ModBlocks;
import com.lexxeous.enchantment_storage.registry.ModBlockEntities;
import com.lexxeous.enchantment_storage.registry.ModScreenHandlers;
import com.lexxeous.enchantment_storage.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantmentStorage implements ModInitializer {
	public static final String MOD_ID = "enchantment_storage";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.

		// region Registration & Initialization
		ServerLifecycleEvents.SERVER_STARTED.register(server ->
			LOGGER.info("Server started. Lexxeous' Enchantment Storage mod loaded.")
		);

		ModBlocks.register();
		ModItems.register();
		LOGGER.info("Registration complete for {}", MOD_ID);

		ModBlockEntities.init();
		ModScreenHandlers.init();
		LOGGER.info("Initialization complete for {}", MOD_ID);
		// endregion

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			registerDebugCommands();
		}

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
			.register(entries ->
				entries.add(ModItems.ENCHANTMENT_EXTRACTOR_ITEM)
			);
	}

	private void registerDebugCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			dispatcher.register(CommandManager.literal("es_debug")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("demo").executes(ctx -> {
					EnchantmentCategoriesHarness.runDemo();
					ctx.getSource().sendFeedback(() -> Text.literal("Debug demo ran."), false);
					return 1;
				}))
				.then(CommandManager.literal("other").executes(ctx -> {
					// method call placeholder
					ctx.getSource().sendFeedback(() -> Text.literal("Debug method call placeholder ran."), false);
					return 1;
				}))
			)
		);
	}
}

// region Notes

// Store
// Bottom Input enchanted item or book
// Top input optional lapis
// Output regular (unenchanted) item or book with all same properties

// Extract
// Bottom Input only book
// Top input optional lapis
// Output enchanted book

// endregion

// region TODOs

// TODO: Create Regions

// endregion
