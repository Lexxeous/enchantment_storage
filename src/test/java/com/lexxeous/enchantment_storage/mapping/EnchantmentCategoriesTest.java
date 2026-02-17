package com.lexxeous.enchantment_storage.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

class EnchantmentCategoriesTest {
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
	void incrementDecrementAndSetCount_keepTotalConsistentAndCleanupEmptyCategories() {
		EnchantmentCategories categories = new EnchantmentCategories();
		categories.increment(SHARPNESS, 0, 3);
		categories.increment(SMITE, 1, 2);
		assertEquals(5, categories.getTotalCount());
		assertEquals(3, categories.getTotalForEnchantment(SHARPNESS));
		assertEquals(2, categories.getTotalForEnchantment(SMITE));

		categories.decrement(SHARPNESS, 0, 3);
		assertEquals(2, categories.getTotalCount());
		assertEquals(0, categories.getTotalForEnchantment(SHARPNESS));
		assertEquals(List.of(SMITE), categories.getSortedEnchantments());
	}

	@Test
	void getSortedEnchantments_withResolverSortsByResolvedName() {
		EnchantmentCategories categories = new EnchantmentCategories();
		categories.increment(SHARPNESS, 0, 1);
		categories.increment(SMITE, 0, 1);

		List<Identifier> sorted = categories.getSortedEnchantments(id -> {
			if (id.equals(SHARPNESS)) {
				return "Zulu";
			}
			return "alpha";
		});

		assertEquals(List.of(SMITE, SHARPNESS), sorted);
	}
	// endregion

	// region Modded
	@Test
	void incrementDecrementAndSetCount_supportsModdedEnchantments() {
		EnchantmentCategories categories = new EnchantmentCategories();
		categories.increment(ARIALS_BANE, 0, 3);
		categories.increment(WITHERING, 1, 2);
		categories.increment(DECAY, 2, 1);

		assertEquals(6, categories.getTotalCount());
		assertEquals(3, categories.getTotalForEnchantment(ARIALS_BANE));
		assertEquals(2, categories.getTotalForEnchantment(WITHERING));
		assertEquals(1, categories.getTotalForEnchantment(DECAY));
	}

	@Test
	void getSortedEnchantments_withResolverSortsModdedByDisplayName() {
		EnchantmentCategories categories = new EnchantmentCategories();
		categories.increment(WITHERING, 0, 1);
		categories.increment(ARIALS_BANE, 0, 1);
		categories.increment(DECAY, 0, 1);

		List<Identifier> sorted = categories.getSortedEnchantments(id -> {
			if (id.equals(ARIALS_BANE)) {
				return "Arial's Bane";
			}
			if (id.equals(WITHERING)) {
				return "Withering";
			}
			return "Decay";
		});

		assertEquals(List.of(ARIALS_BANE, DECAY, WITHERING), sorted);
	}
	// endregion
	// endregion

	// region Regression
	// region Vanilla
	@Test
	void nbtRoundTrip_preservesCountsAndTotals() {
		EnchantmentCategories original = new EnchantmentCategories();
		original.increment(SHARPNESS, 0, 2);
		original.increment(SHARPNESS, 2, 1);
		original.increment(SMITE, 1, 4);

		NbtCompound nbt = original.toNbt();
		EnchantmentCategories restored = new EnchantmentCategories();
		restored.readNbt(nbt);

		assertEquals(original.getTotalCount(), restored.getTotalCount());
		assertEquals(3, restored.getTotalForEnchantment(SHARPNESS));
		assertEquals(4, restored.getTotalForEnchantment(SMITE));
	}

	@Test
	void readNbt_ignoresInvalidVanillaEntriesAndRetainsValidOnes() {
		NbtCompound root = new NbtCompound();
		NbtList entries = new NbtList();
		entries.add(buildEntry("minecraft:sharpness", new byte[] {2, 0, 0, 0, 0}));
		entries.add(buildEntry("", new byte[] {1, 0, 0, 0, 0}));
		entries.add(buildEntry("invalid id", new byte[] {1, 0, 0, 0, 0}));
		entries.add(buildEntry("minecraft:smite", new byte[0]));
		root.put("categories", entries);

		EnchantmentCategories categories = new EnchantmentCategories();
		categories.readNbt(root);

		assertEquals(2, categories.getTotalCount());
		assertEquals(2, categories.getTotalForEnchantment(SHARPNESS));
		assertEquals(0, categories.getTotalForEnchantment(SMITE));
	}

	@Test
	void getSortedEnchantments_returnsUnmodifiableVanillaList() {
		EnchantmentCategories categories = new EnchantmentCategories();
		categories.increment(SHARPNESS, 0, 1);
		List<Identifier> sorted = categories.getSortedEnchantments();

		assertThrows(UnsupportedOperationException.class, () -> sorted.add(SMITE));
	}
	// endregion

	// region Modded
	@Test
	void nbtRoundTrip_preservesModdedEnchantmentCountsAndTotals() {
		EnchantmentCategories original = new EnchantmentCategories();
		original.increment(ARIALS_BANE, 0, 2);
		original.increment(WITHERING, 2, 1);
		original.increment(DECAY, 1, 4);

		NbtCompound nbt = original.toNbt();
		EnchantmentCategories restored = new EnchantmentCategories();
		restored.readNbt(nbt);

		assertEquals(original.getTotalCount(), restored.getTotalCount());
		assertEquals(2, restored.getTotalForEnchantment(ARIALS_BANE));
		assertEquals(1, restored.getTotalForEnchantment(WITHERING));
		assertEquals(4, restored.getTotalForEnchantment(DECAY));
	}

	@Test
	void readNbt_ignoresInvalidModdedEntriesAndRetainsValidOnes() {
		NbtCompound root = new NbtCompound();
		NbtList entries = new NbtList();
		entries.add(buildEntry(ARIALS_BANE.toString(), new byte[] {1, 1, 0, 0, 0}));
		entries.add(buildEntry("other_mod_namespace2:withering", new byte[0]));
		entries.add(buildEntry("other_mod_namespace3:decay", new byte[] {0, 2, 0, 0, 0}));
		root.put("categories", entries);

		EnchantmentCategories categories = new EnchantmentCategories();
		categories.readNbt(root);

		assertEquals(4, categories.getTotalCount());
		assertEquals(2, categories.getTotalForEnchantment(ARIALS_BANE));
		assertEquals(0, categories.getTotalForEnchantment(WITHERING));
		assertEquals(2, categories.getTotalForEnchantment(DECAY));
	}

	@Test
	void sortedByNameCache_invalidatesAfterModdedMutation() {
		EnchantmentCategories categories = new EnchantmentCategories();
		categories.increment(WITHERING, 0, 1);
		categories.increment(ARIALS_BANE, 0, 1);

		EnchantmentCategories.NameResolver resolver = id -> {
			if (id.equals(ARIALS_BANE)) {
				return "Arial's Bane";
			}
			if (id.equals(WITHERING)) {
				return "Withering";
			}
			return "Decay";
		};

		List<Identifier> before = categories.getSortedEnchantments(resolver);
		assertEquals(List.of(ARIALS_BANE, WITHERING), before);

		categories.increment(DECAY, 0, 1);
		List<Identifier> after = categories.getSortedEnchantments(resolver);
		assertEquals(List.of(ARIALS_BANE, DECAY, WITHERING), after);
	}
	// endregion

	@Test
	void clear_resetsAllRuntimeState() {
		EnchantmentCategories categories = new EnchantmentCategories();
		categories.increment(SHARPNESS, 0, 1);
		categories.increment(SMITE, 0, 1);
		categories.clear();

		assertEquals(0, categories.getTotalCount());
		assertTrue(categories.getSortedEnchantments().isEmpty());
	}
	// endregion

	// region Helpers
	private static NbtCompound buildEntry(String id, byte[] levels) {
		NbtCompound entry = new NbtCompound();
		entry.putString("id", id);
		entry.put("levels", new NbtByteArray(levels));
		return entry;
	}
	// endregion
}
