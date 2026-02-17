package com.lexxeous.enchantment_storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.fabricmc.loader.api.FabricLoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class EnchantmentStorageTest {
	// region Smoke
	@Test
	void modId_isStable() {
		assertEquals("enchantment_storage", EnchantmentStorage.MOD_ID);
	}
	// endregion

	// region Stability
	@Test
	void logDebugDev_isSafeOutsideDevelopmentEnvironment() {
		try (MockedStatic<FabricLoader> fabricLoaderStatic = Mockito.mockStatic(FabricLoader.class)) {
			FabricLoader loader = Mockito.mock(FabricLoader.class);
			fabricLoaderStatic.when(FabricLoader::getInstance).thenReturn(loader);
			Mockito.when(loader.isDevelopmentEnvironment()).thenReturn(false);

			assertDoesNotThrow(() -> EnchantmentStorage.logDebugDev("test {}", 1));
			Mockito.verify(loader).isDevelopmentEnvironment();
		}
	}
	// endregion
}
