package com.lexxeous.enchantment_storage.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EnchantmentStorageCategoryLogicTest {
	// region Constants
	// region Vanilla
	private static final Identifier SHARPNESS = Identifier.of("minecraft", "sharpness");
	private static final Identifier SMITE = Identifier.of("minecraft", "smite");
	// endregion

	// region Modded
	private static final Identifier ARIALS_BANE = Identifier.of("other_mod_namespace1", "arials_bane");
	private static final Identifier WITHERING = Identifier.of("other_mod_namespace2", "withering");
	private static final Identifier DECAY = Identifier.of("other_mod_namespace3", "decay");
	// endregion
	// endregion

	// region Confidence
	// region Vanilla
	@Test
	void sortById_ordersIdentifiersLexicographically() {
		List<Identifier> ids = new ArrayList<>();
		ids.add(SMITE);
		ids.add(SHARPNESS);

		EnchantmentStorageCategoryLogic.sortById(ids);

		assertEquals(List.of(
			SHARPNESS,
			SMITE
		), ids);
	}
	// endregion

	// region Modded
	@Test
	void sortById_ordersModdedEnchantmentsAcrossNamespaces() {
		List<Identifier> ids = new ArrayList<>();
		ids.add(WITHERING);
		ids.add(ARIALS_BANE);
		ids.add(DECAY);

		EnchantmentStorageCategoryLogic.sortById(ids);

		assertEquals(List.of(ARIALS_BANE, WITHERING, DECAY), ids);
	}
	// endregion
	// endregion

	// region Regression
	// region Vanilla
	@Test
	void sortByResolvedName_usesCaseInsensitiveNamesThenIdTiebreaker() {
		List<Identifier> ids = new ArrayList<>();
		ids.add(SMITE);
		ids.add(SHARPNESS);

		@SuppressWarnings("unchecked")
		Function<Identifier, String> resolver = Mockito.mock(Function.class);
		when(resolver.apply(SMITE)).thenReturn("alpha");
		when(resolver.apply(SHARPNESS)).thenReturn("Beta");

		EnchantmentStorageCategoryLogic.sortByResolvedName(ids, resolver);

		assertEquals(List.of(SMITE, SHARPNESS), ids);
		verify(resolver, atLeastOnce()).apply(SMITE);
		verify(resolver, atLeastOnce()).apply(SHARPNESS);
	}

	@Test
	void sortByResolvedName_handlesNullVanillaResolvedNames() {
		List<Identifier> ids = new ArrayList<>();
		ids.add(SMITE);
		ids.add(SHARPNESS);

		@SuppressWarnings("unchecked")
		Function<Identifier, String> resolver = Mockito.mock(Function.class);
		when(resolver.apply(SMITE)).thenReturn(null);
		when(resolver.apply(SHARPNESS)).thenReturn("Sharpness");

		EnchantmentStorageCategoryLogic.sortByResolvedName(ids, resolver);

		assertEquals(List.of(SMITE, SHARPNESS), ids);
	}
	// endregion

	// region Modded
	@Test
	void sortByResolvedName_ordersCustomModdedEnchantmentNames() {
		List<Identifier> ids = new ArrayList<>();
		ids.add(WITHERING);
		ids.add(ARIALS_BANE);
		ids.add(DECAY);

		@SuppressWarnings("unchecked")
		Function<Identifier, String> resolver = Mockito.mock(Function.class);
		when(resolver.apply(ARIALS_BANE)).thenReturn("Arial's Bane");
		when(resolver.apply(WITHERING)).thenReturn("Withering");
		when(resolver.apply(DECAY)).thenReturn("Decay");

		EnchantmentStorageCategoryLogic.sortByResolvedName(ids, resolver);

		assertEquals(List.of(ARIALS_BANE, DECAY, WITHERING), ids);
		verify(resolver, atLeastOnce()).apply(ARIALS_BANE);
		verify(resolver, atLeastOnce()).apply(WITHERING);
		verify(resolver, atLeastOnce()).apply(DECAY);
	}

	@Test
	void sortByResolvedName_handlesNullModdedResolvedNamesWithIdFallback() {
		List<Identifier> ids = new ArrayList<>();
		ids.add(DECAY);
		ids.add(ARIALS_BANE);

		@SuppressWarnings("unchecked")
		Function<Identifier, String> resolver = Mockito.mock(Function.class);
		when(resolver.apply(DECAY)).thenReturn(null);
		when(resolver.apply(ARIALS_BANE)).thenReturn(null);

		EnchantmentStorageCategoryLogic.sortByResolvedName(ids, resolver);

		assertEquals(List.of(ARIALS_BANE, DECAY), ids);
	}
	// endregion
	// endregion
}
