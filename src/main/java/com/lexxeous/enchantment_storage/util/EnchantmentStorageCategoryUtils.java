package com.lexxeous.enchantment_storage.util;

import com.lexxeous.enchantment_storage.mapping.EnchantmentCategories;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class EnchantmentStorageCategoryUtils {
	// region Constructors
	private EnchantmentStorageCategoryUtils() {}
	// endregion

	// region Serialization
	public static List<StoredCategoryLine> getStoredCategoryLines(@Nullable NbtCompound categoriesNbt) {
		if (categoriesNbt == null) {
			return List.of();
		}

		EnchantmentCategories categories = new EnchantmentCategories();
		categories.readNbt(categoriesNbt);
		if (categories.getTotalCount() <= 0) {
			return List.of();
		}

		List<Identifier> ids = categories.getSortedEnchantments(EnchantmentStorageTextUtils::displayEnchantmentName);
		List<StoredCategoryLine> lines = new ArrayList<>();
		for (Identifier id : ids) {
			int total = categories.getTotalForEnchantment(id);
			if (total <= 0) {
				continue;
			}
			lines.add(new StoredCategoryLine(EnchantmentStorageTextUtils.displayEnchantmentName(id), total));
		}

		lines.sort(Comparator.comparing(line -> line.name().toLowerCase(Locale.ROOT)));
		return lines;
	}

	public static int getStoredCategoryTotal(@Nullable List<StoredCategoryLine> lines) {
		if (lines == null || lines.isEmpty()) {
			return 0;
		}

		int total = 0;
		for (StoredCategoryLine line : lines) {
			total += Math.max(0, line.count());
		}
		return total;
	}

	public record StoredCategoryLine(String name, int count) {}
	// endregion
}
