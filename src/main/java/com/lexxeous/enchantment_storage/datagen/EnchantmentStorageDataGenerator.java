package com.lexxeous.enchantment_storage.datagen;

import com.lexxeous.enchantment_storage.providers.EnchantmentExtractorEnglishLangProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class EnchantmentStorageDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(EnchantmentExtractorEnglishLangProvider::new);
//		pack.addProvider(EnchantmentExtractorModelProvider::new);
	}
}
