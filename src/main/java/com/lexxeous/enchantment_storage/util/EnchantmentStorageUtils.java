package com.lexxeous.enchantment_storage.util;

public final class EnchantmentStorageUtils {
    private EnchantmentStorageUtils() {}

    public static String formatFallbackName(String path) {
        if (path == null || path.isEmpty()) return "";
        String[] parts = path.split("_");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            if (i > 0) builder.append(' ');
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) builder.append(part.substring(1));
        }
        return builder.toString();
    }
}
