package com.lexxeous.enchantment_storage.registry;

import com.lexxeous.enchantment_storage.EnchantmentStorage;
import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public final class ModScreenHandlers {
	// region Constants
	public static final Identifier ENCHANTMENT_EXTRACTOR_SCREEN_HANDLER_ID =
		Identifier.of(EnchantmentStorage.MOD_ID, "enchantment_extractor");
	// endregion

	// region Class Variables
	public static final ScreenHandlerType<EnchantmentExtractorScreenHandler> ENCHANTMENT_EXTRACTOR =
		Registry.register(
			Registries.SCREEN_HANDLER,
			ENCHANTMENT_EXTRACTOR_SCREEN_HANDLER_ID,
			new ScreenHandlerType<>(EnchantmentExtractorScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
		);
	// endregion

	// region Constructors
	private ModScreenHandlers() {}
	// endregion

	// region Registration & Initialization
	public static void register() {
		// Static holder.
	}
	// endregion
}