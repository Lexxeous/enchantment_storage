package com.lexxeous.enchantment_storage.logic;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import net.minecraft.util.Identifier;

public final class EnchantmentStorageCategoryLogic {
	// region Constructors
	private EnchantmentStorageCategoryLogic() {}
	// endregion

	// region Helpers
	public static void sortById(List<Identifier> ids) {
		ids.sort(Comparator.comparing(Identifier::toString));
	}

	public static void sortByResolvedName(List<Identifier> ids, Function<Identifier, String> nameResolver) {
		ids.sort((a, b) -> compareResolvedNames(a, b, nameResolver));
	}
	// endregion

	// region Private
	private static int compareResolvedNames(
		Identifier first,
		Identifier second,
		Function<Identifier, String> nameResolver
	) {
		String firstName = safeName(nameResolver.apply(first));
		String secondName = safeName(nameResolver.apply(second));

		int compareByName = firstName.compareToIgnoreCase(secondName);
		if (compareByName != 0) {
			return compareByName;
		}

		return first.toString().compareTo(second.toString());
	}

	private static String safeName(String name) {
		return name == null ? "" : name;
	}
	// endregion
}
