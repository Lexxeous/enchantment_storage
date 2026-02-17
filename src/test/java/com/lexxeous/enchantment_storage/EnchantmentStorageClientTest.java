package com.lexxeous.enchantment_storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import net.fabricmc.api.ClientModInitializer;
import org.junit.jupiter.api.Test;

class EnchantmentStorageClientTest {
	// region Smoke
	@Test
	void clientInitializer_isFinalAndImplementsClientModInitializer() {
		assertTrue(Modifier.isFinal(EnchantmentStorageClient.class.getModifiers()));
		assertInstanceOf(ClientModInitializer.class, new EnchantmentStorageClient());
	}
	// endregion

	// region Stability
	@Test
	void className_staysStableForEntrypointConfiguration() {
		assertEquals(
			"com.lexxeous.enchantment_storage.EnchantmentStorageClient",
			EnchantmentStorageClient.class.getName()
		);
	}
	// endregion
}
