package com.lexxeous.enchantment_storage.prototype;

import java.util.*;

import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

public class EnchantmentCategories {
    // region Class Variable(s)
    private final Map<Identifier, EnchantmentCategory> categories = new HashMap<>();
    private final List<Identifier> cachedSortedById = new ArrayList<>();
    private final List<Identifier> cachedSortedByName = new ArrayList<>();
    private boolean sortedByIdDirty = true;
    private boolean sortedByNameDirty = true;
    private NameResolver cachedNameResolver;
    private int totalCount = 0;
    // endregion

    // region Getter(s) & Setter(s)
    public int getCount(Identifier enchantmentId, int levelIndex) {
        EnchantmentCategory category = categories.get(enchantmentId);

        if (category == null) {
            return 0;
        }

        return category.getCount(levelIndex);
    }

    public void setCount(Identifier enchantmentId, int levelIndex, int count) {
        EnchantmentCategory category = getOrCreateCategory(enchantmentId);

        int before = category.getCount(levelIndex);
        category.setCount(levelIndex, count);
        int after = category.getCount(levelIndex);

        totalCount += (after - before);
        cleanupIfEmpty(category);
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getTotalForEnchantment(Identifier enchantmentId) {
        EnchantmentCategory category = categories.get(enchantmentId);

        if (category == null) {
            return 0;
        }

        return category.getTotalCount();
    }

    private EnchantmentCategory getOrCreateCategory(Identifier enchantmentId) {
        EnchantmentCategory category = categories.get(enchantmentId);
        if (category != null) {
            return category;
        }

        category = new EnchantmentCategory(enchantmentId);
        categories.put(enchantmentId, category);
        sortedByIdDirty = true;
        sortedByNameDirty = true;

        return category;
    }
    // endregion

    // region Increment & Decrement
    public void increment(Identifier enchantmentId, int levelIndex, int amount) {
        if (amount == 0) {
            return;
        }

        EnchantmentCategory category = getOrCreateCategory(enchantmentId);
        int delta = category.increment(levelIndex, amount);
        totalCount += delta;
        cleanupIfEmpty(category);
    }

    public void decrement(Identifier enchantmentId, int levelIndex, int amount) {
        increment(enchantmentId, levelIndex, -amount);
    }
    // endregion

    // region Sort
    public List<Identifier> getSortedEnchantments() {
        if (!sortedByIdDirty) {
            return Collections.unmodifiableList(cachedSortedById);
        }

        cachedSortedById.clear();
        cachedSortedById.addAll(categories.keySet());
        cachedSortedById.sort(Comparator.comparing(Identifier::toString));
        sortedByIdDirty = false;

        return Collections.unmodifiableList(cachedSortedById);
    }

    public List<Identifier> getSortedEnchantments(NameResolver resolver) {
        if (resolver == null) {
            return getSortedEnchantments();
        }

        if (!sortedByNameDirty && cachedNameResolver == resolver) {
            return Collections.unmodifiableList(cachedSortedByName);
        }

        cachedSortedByName.clear();
        cachedSortedByName.addAll(categories.keySet());
        cachedSortedByName.sort((a, b) -> {
            String nameA = safeName(resolver.resolve(a));
            String nameB = safeName(resolver.resolve(b));

            int cmp = nameA.compareToIgnoreCase(nameB);
            if (cmp != 0) {
                return cmp;
            }

            return a.toString().compareTo(b.toString());
        });

        cachedNameResolver = resolver;
        sortedByNameDirty = false;

        return Collections.unmodifiableList(cachedSortedByName);
    }
    // endregion

    // region NBT
    public NbtCompound toNbt() {
        NbtCompound root = new NbtCompound();
        NbtList list = new NbtList();

        for (EnchantmentCategory category : categories.values()) {
            NbtCompound entryTag = new NbtCompound();
            entryTag.putString("id", category.getEnchantmentId().toString());
            entryTag.put("levels", new NbtByteArray(category.getLevelsCopy()));
            list.add(entryTag);
        }

        root.put("categories", list);
        root.putInt("total", totalCount);

        return root;
    }

    public void readNbt(NbtCompound root) {
        clear();

        if (root == null) {
            return;
        }

        NbtList list = root.getList("categories").orElse(new NbtList());

        for (int i = 0; i < list.size(); i++) {
            NbtCompound entryTag = list.getCompound(i).orElse(null);
            if (entryTag == null) {
                continue;
            }

            String idString = entryTag.getString("id").orElse("");
            if (idString.isEmpty()) {
                continue;
            }

            Identifier id = Identifier.tryParse(idString);
            if (id == null) {
                continue;
            }

            byte[] stored = entryTag.getByteArray("levels").orElse(new byte[0]);
            if (stored.length == 0) {
                continue;
            }

            EnchantmentCategory category = new EnchantmentCategory(id, stored);
            if (category.isEmpty()) {
                continue;
            }

            categories.put(id, category);
            totalCount += category.getTotalCount();
        }

        sortedByIdDirty = true;
        sortedByNameDirty = true;
    }
    // endregion

    // region Clean
    public void clear() {
        categories.clear();
        cachedSortedById.clear();
        cachedSortedByName.clear();
        sortedByIdDirty = true;
        sortedByNameDirty = true;
        cachedNameResolver = null;
        totalCount = 0;
    }

    private void cleanupIfEmpty(EnchantmentCategory category) {
        if (!category.isEmpty()) {
            return;
        }

        categories.remove(category.getEnchantmentId());
        sortedByIdDirty = true;
        sortedByNameDirty = true;
    }
    // endregion

    // region Helpers
    private String safeName(String name) {
        return name == null ? "" : name;
    }

    public interface NameResolver {
        String resolve(Identifier enchantmentId);
    }
    // endregion
}
