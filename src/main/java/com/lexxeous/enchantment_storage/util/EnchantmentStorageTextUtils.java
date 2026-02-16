package com.lexxeous.enchantment_storage.util;

import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class EnchantmentStorageTextUtils {
	// region Constructors
	private EnchantmentStorageTextUtils() {}
	// endregion

	// region Helpers
	public static String formatFallbackName(@Nullable String path) {
		if (path == null || path.isEmpty()) {
			return "";
		}

		String[] parts = path.split("_");
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			if (part.isEmpty()) {
				continue;
			}
			if (i > 0) {
				builder.append(' ');
			}
			builder.append(Character.toUpperCase(part.charAt(0)));
			if (part.length() > 1) {
				builder.append(part.substring(1));
			}
		}
		return builder.toString();
	}

	public static String displayEnchantmentName(@Nullable Identifier id) {
		if (id == null) {
			return "";
		}

		String translationKey = "enchantment." + id.getNamespace() + "." + id.getPath();
		String translated = Text.translatable(translationKey).getString();
		if (!translationKey.equals(translated)) {
			return translated;
		}

		return formatFallbackName(id.getPath());
	}

	public static String joinWithCommasAnd(@Nullable List<String> parts) {
		if (parts == null || parts.isEmpty()) {
			return "";
		}
		if (parts.size() == 1) {
			return parts.getFirst();
		}
		if (parts.size() == 2) {
			return parts.get(0) + " & " + parts.get(1);
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < parts.size() - 1; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(parts.get(i));
		}
		builder.append(", & ").append(parts.getLast());
		return builder.toString();
	}
	// endregion
}
