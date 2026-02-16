package com.lexxeous.enchantment_storage.provider;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

public class EnchantmentExtractorEnglishLangProvider extends FabricLanguageProvider {
	// region Constructors
	public EnchantmentExtractorEnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
		super(dataOutput, "en_us", registryLookup);
	}
	// endregion

	// region Overrides
	@Override
	public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
		translationBuilder.add("text.enchantment_extractor.use", "Using EE Block");
	}
	// endregion
}