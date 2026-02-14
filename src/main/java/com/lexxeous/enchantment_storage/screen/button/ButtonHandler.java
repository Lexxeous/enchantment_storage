package com.lexxeous.enchantment_storage.screen.button;

import com.lexxeous.enchantment_storage.EnchantmentStorage;
import com.lexxeous.enchantment_storage.blockentity.EnchantmentExtractorBlockEntity;
import com.lexxeous.enchantment_storage.mapping.EnchantmentCategory;
import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorScreenHandler;
import com.lexxeous.enchantment_storage.util.EnchantmentStorageUtils;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class ButtonHandler {
    private static final int GRID_COLS = EnchantmentExtractorBlockEntity.GRID_COLS;

    private final EnchantmentExtractorScreenHandler handler;
    private final Inventory inventory;
    private final EnchantmentExtractorBlockEntity blockEntity;

    public ButtonHandler(
        EnchantmentExtractorScreenHandler handler,
        Inventory inventory,
        EnchantmentExtractorBlockEntity blockEntity
    ) {
        this.handler = handler;
        this.inventory = inventory;
        this.blockEntity = blockEntity;
    }

    public boolean canStore() {
        if (!isOutputEmpty()) return false;
        ItemStack stack = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT);
        return !stack.isEmpty() && (stack.hasEnchantments() || stack.isOf(Items.ENCHANTED_BOOK));
    }

    public boolean canExtract() {
        if (!isOutputEmpty()) return false;
        int selectedGridIndex = handler.getSelectedGridIndex();
        if (selectedGridIndex < 0) return false;

        int row = selectedGridIndex / GRID_COLS;
        int col = selectedGridIndex % GRID_COLS;
        if (handler.getGridValue(row, col) <= 0) return false;

        ItemStack stack = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT);
        if (stack.isEmpty() || (!stack.isOf(Items.BOOK) && !stack.isOf(Items.ENCHANTED_BOOK))) {
            return false;
        }

        if (blockEntity != null) {
            var selectedId = blockEntity.getEnchantmentIdForRow(row);
            if (EnchantmentStorageUtils.hasEnchantment(stack, selectedId)) {
                return false;
            }
        }

        return true;
    }

    public boolean handleGridClick(PlayerEntity player, int gridIndex) {
        if (blockEntity == null || player.getEntityWorld().isClient()) {
            return false;
        }

        handler.setSelectedGridIndex(gridIndex);
        int row = gridIndex / GRID_COLS;
        int col = gridIndex % GRID_COLS;
        if (row < 0 || col < 0 || row >= EnchantmentExtractorBlockEntity.GRID_ROWS) {
            return false;
        }

        var enchantmentId = blockEntity.getEnchantmentIdForRow(row);
        if (enchantmentId == null) {
            return false;
        }

        int amount = blockEntity.getGridValue(row, col);
        int rank = col + 1;
        String name = blockEntity.getEnchantmentDisplayName(enchantmentId);
        EnchantmentStorage.LOGGER.info("Can extract {} {} rank {}", amount, name, rank);
        return true;
    }

    public boolean handleStore(PlayerEntity player) {
        if (blockEntity == null || player.getEntityWorld().isClient()) {
            return false;
        }
        if (!canStore()) {
            return false;
        }

        ItemStack input = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT);
        if (input.isEmpty()) {
            return false;
        }

        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(input);
        if (enchantments.getEnchantments().isEmpty()) {
            return false;
        }

        int baseCost = EnchantmentStorageUtils.getEnchantmentLevelTotal(input);
        ItemStack lapis = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_LAPIS);
        int lapisUsed = EnchantmentStorageUtils.getLapisDiscountUsed(baseCost, lapis);
        int experienceCost = EnchantmentStorageUtils.getDiscountedCost(baseCost, lapis);
        if (!canAffordExperience(player, experienceCost)) {
            return false;
        }

        Set<net.minecraft.util.Identifier> storedIds = new HashSet<>();
        for (var entry : enchantments.getEnchantmentEntries()) {
            int level = entry.getIntValue();
            if (level < 1 || level > EnchantmentCategory.MAX_LEVELS) {
                continue;
            }
            var id = entry.getKey().getKey()
                .map(net.minecraft.registry.RegistryKey::getValue)
                .orElse(null);
            if (id == null) {
                continue;
            }
            storedIds.add(id);
            blockEntity.debugAdjustEnchantment(id, level - 1, 1);
        }

        if (storedIds.isEmpty()) {
            return false;
        }

        ItemStack output = input.copyWithCount(1);
        EnchantmentHelper.apply(output, builder ->
            builder.remove(entry -> entry.getKey()
                .map(net.minecraft.registry.RegistryKey::getValue)
                .filter(storedIds::contains)
                .isPresent())
        );

        if (input.isOf(Items.ENCHANTED_BOOK) && EnchantmentHelper.getEnchantments(output).isEmpty()) {
            output = output.copyComponentsToNewStack(Items.BOOK, 1);
        }

        inventory.setStack(EnchantmentExtractorBlockEntity.SLOT_OUTPUT, output);
        decrementInputStack(input);
        consumeLapis(lapis, lapisUsed);
        chargeExperience(player, experienceCost);
        return true;
    }

    public boolean handleExtract(PlayerEntity player) {
        if (blockEntity == null || player.getEntityWorld().isClient()) {
            return false;
        }
        if (!canExtract()) {
            return false;
        }
        int selectedGridIndex = handler.getSelectedGridIndex();
        if (selectedGridIndex < 0) {
            return false;
        }

        int row = selectedGridIndex / GRID_COLS;
        int col = selectedGridIndex % GRID_COLS;
        int amount = handler.getGridValue(row, col);
        if (amount <= 0) {
            return false;
        }

        ItemStack input = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT);
        if (input.isEmpty()) {
            return false;
        }

        var enchantmentId = blockEntity.getEnchantmentIdForRow(row);
        if (enchantmentId == null) {
            return false;
        }
        var enchantmentEntry = blockEntity.getEnchantmentEntry(enchantmentId);
        if (enchantmentEntry == null) {
            return false;
        }

        int rank = col + 1;
        ItemStack lapis = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_LAPIS);
        int lapisUsed = EnchantmentStorageUtils.getLapisDiscountUsed(rank, lapis);
        int experienceCost = EnchantmentStorageUtils.getDiscountedCost(rank, lapis);
        if (!canAffordExperience(player, experienceCost)) {
            return false;
        }
        ItemStack output = input.isOf(Items.ENCHANTED_BOOK)
            ? input.copyWithCount(1)
            : input.copyComponentsToNewStack(Items.ENCHANTED_BOOK, 1);
        EnchantmentHelper.apply(output, builder -> builder.set(enchantmentEntry, rank));

        inventory.setStack(EnchantmentExtractorBlockEntity.SLOT_OUTPUT, output);
        blockEntity.debugAdjustEnchantment(enchantmentId, col, -1);
        decrementInputStack(input);
        consumeLapis(lapis, lapisUsed);
        chargeExperience(player, experienceCost);
        return true;
    }

    private void decrementInputStack(ItemStack input) {
        if (input.getCount() > 1) {
            input.decrement(1);
            inventory.setStack(EnchantmentExtractorBlockEntity.SLOT_INPUT, input);
        } else {
            inventory.setStack(EnchantmentExtractorBlockEntity.SLOT_INPUT, ItemStack.EMPTY);
        }
    }

    private boolean isOutputEmpty() {
        return inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_OUTPUT).isEmpty();
    }

    private boolean canAffordExperience(PlayerEntity player, int experienceCost) {
        if (experienceCost <= 0) {
            return true;
        }
        return player.isCreative() || player.experienceLevel >= experienceCost;
    }

    private void consumeLapis(ItemStack lapis, int lapisUsed) {
        if (lapisUsed <= 0 || lapis.isEmpty()) {
            return;
        }
        if (lapis.getCount() > lapisUsed) {
            lapis.decrement(lapisUsed);
        } else {
            lapis = ItemStack.EMPTY;
        }
        inventory.setStack(EnchantmentExtractorBlockEntity.SLOT_LAPIS, lapis);
    }

    private void chargeExperience(PlayerEntity player, int experienceCost) {
        if (experienceCost <= 0 || player.isCreative()) {
            return;
        }
        player.addExperienceLevels(-experienceCost);
    }
}
