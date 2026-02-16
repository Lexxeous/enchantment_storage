package com.lexxeous.enchantment_storage.mapping;

import com.lexxeous.enchantment_storage.logic.EnchantmentStorageCategoryLogic;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class EnchantmentCategoriesHarness {
	// region Constructors
	private EnchantmentCategoriesHarness() {}
	// endregion

	// region Helpers
	public static EnchantmentCategories buildSeedCategories(
		@Nullable Registry<Enchantment> registry,
		int rows,
		int baseCount
	) {
		EnchantmentCategories categories = new EnchantmentCategories();
		if (registry == null || rows <= 0 || baseCount <= 0) {
			return categories;
		}

		List<Identifier> order = computeRegistryOrder(registry);
		int limit = Math.min(order.size(), rows);
		for (int i = 0; i < limit; i++) {
			Identifier id = order.get(i);
			categories.increment(id, Math.min(EnchantmentCategory.MAX_LEVELS - 1, i), baseCount + i);
			categories.increment(id, 0, 1);
		}
		return categories;
	}

	public static List<Identifier> computeRegistryOrder(@Nullable Registry<Enchantment> registry) {
		if (registry == null) {
			return List.of();
		}
		List<Identifier> ids = new ArrayList<>();
		ids.addAll(registry.getIds());
		EnchantmentStorageCategoryLogic.sortById(ids);
		return ids;
	}
	// endregion
}
