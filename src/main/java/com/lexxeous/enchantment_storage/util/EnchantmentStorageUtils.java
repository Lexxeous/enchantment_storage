package com.lexxeous.enchantment_storage.util;

import com.lexxeous.enchantment_storage.mapping.EnchantmentCategories;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

    public static boolean hasEnchantment(ItemStack stack, Identifier enchantmentId) {
        if (stack == null || stack.isEmpty() || enchantmentId == null) {
            return false;
        }

        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.getEnchantments().isEmpty()) {
            return false;
        }

        for (var entry : enchantments.getEnchantmentEntries()) {
            var id = entry.getKey().getKey()
                .map(net.minecraft.registry.RegistryKey::getValue)
                .orElse(null);

            if (enchantmentId.equals(id)) {
                return true;
            }
        }

        return false;
    }

    public static int getEnchantmentLevelTotal(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.getEnchantments().isEmpty()) {
            return 0;
        }

        int total = 0;
        for (var entry : enchantments.getEnchantmentEntries()) {
            total += entry.getIntValue();
        }
        return total;
    }

    public static int getStoreExperienceCost(ItemStack input) {
        int total = getEnchantmentLevelTotal(input);
        return total > 0 ? total : -1;
    }

    public static int getDiscountedCost(int baseCost, ItemStack lapisStack) {
        if (baseCost <= 0) {
            return 0;
        }

        int lapisCount = getLapisCount(lapisStack);
        return Math.max(0, baseCost - lapisCount);
    }

    public static int getLapisDiscountUsed(int baseCost, ItemStack lapisStack) {
        if (baseCost <= 0) {
            return 0;
        }

        int lapisCount = getLapisCount(lapisStack);
        return Math.min(baseCost, lapisCount);
    }

    private static int getLapisCount(ItemStack lapisStack) {
        if (lapisStack == null || lapisStack.isEmpty()) {
            return 0;
        }

        return lapisStack.getCount();
    }

    public static final class StoreCostCache {
        private ItemStack cachedInput = ItemStack.EMPTY;
        private int cachedStoreCost = -1;

        public int get(ItemStack input) {
            if (input == null || input.isEmpty()) {
                cachedInput = ItemStack.EMPTY;
                cachedStoreCost = -1;
                return -1;
            }

            if (!cachedInput.isEmpty() && ItemStack.areItemsAndComponentsEqual(cachedInput, input)) {
                return cachedStoreCost;
            }

            cachedInput = input.copyWithCount(1);
            cachedStoreCost = EnchantmentStorageUtils.getStoreExperienceCost(input);
            return cachedStoreCost;
        }
    }

    public static List<StoredCategoryLine> getStoredCategoryLines(NbtCompound categoriesNbt) {
        if (categoriesNbt == null) {
            return List.of();
        }

        EnchantmentCategories categories = new EnchantmentCategories();
        categories.readNbt(categoriesNbt);
        if (categories.getTotalCount() <= 0) {
            return List.of();
        }

        List<Identifier> ids = categories.getSortedEnchantments(EnchantmentStorageUtils::displayEnchantmentName);
        List<StoredCategoryLine> lines = new ArrayList<>();
        for (Identifier id : ids) {
            int total = categories.getTotalForEnchantment(id);
            if (total <= 0) {
                continue;
            }
            lines.add(new StoredCategoryLine(displayEnchantmentName(id), total));
        }

        lines.sort(Comparator.comparing(line -> line.name().toLowerCase(Locale.ROOT)));
        return lines;
    }

    public static int getStoredCategoryTotal(List<StoredCategoryLine> lines) {
        if (lines == null || lines.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (StoredCategoryLine line : lines) {
            total += Math.max(0, line.count());
        }
        return total;
    }

    public static String displayEnchantmentName(Identifier id) {
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

    public record StoredCategoryLine(String name, int count) {}
}
