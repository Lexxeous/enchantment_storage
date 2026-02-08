package com.lexxeous.enchantment_storage.screen;

import com.lexxeous.enchantment_storage.EnchantmentStorage;
import com.lexxeous.enchantment_storage.blockentity.EnchantmentExtractorBlockEntity;
import com.lexxeous.enchantment_storage.mapping.EnchantmentCategory;
import com.lexxeous.enchantment_storage.registry.ModBlocks;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

public class EnchantmentExtractorScreenHandler extends ScreenHandler {
    public static final int BUTTON_STORE = 0;
    public static final int BUTTON_EXTRACT = 1;
    public static final int BUTTON_GRID_BASE = 1000;

    private static final int MODE_STORE = EnchantmentExtractorBlockEntity.MODE_STORE;
    private static final int MODE_EXTRACT = EnchantmentExtractorBlockEntity.MODE_EXTRACT;
    private static final int GRID_COLS = EnchantmentExtractorBlockEntity.GRID_COLS;
    private static final int GRID_SIZE = EnchantmentExtractorBlockEntity.GRID_SIZE;
    private static final int PROPERTY_COUNT = 1 + GRID_SIZE;

    private final Inventory inventory;
    private final PropertyDelegate properties;
    private final ScreenHandlerContext context;
    private final EnchantmentExtractorBlockEntity blockEntity;
    private int selectedGridIndex = -1;

    // region Constructor(s)
    public EnchantmentExtractorScreenHandler(int syncId, PlayerInventory playerInv) {
        this(
            syncId,
            playerInv,
            new SimpleInventory(EnchantmentExtractorBlockEntity.INVENTORY_SIZE),
            new ArrayPropertyDelegate(PROPERTY_COUNT),
            ScreenHandlerContext.EMPTY,
            null
        );
    }

    public EnchantmentExtractorScreenHandler(
        int syncId,
        PlayerInventory playerInv,
        EnchantmentExtractorBlockEntity blockEntity,
        ScreenHandlerContext context
    ) {
        this(syncId, playerInv, blockEntity, new ModePropertyDelegate(blockEntity), context, blockEntity);
    }

    private EnchantmentExtractorScreenHandler(
        int syncId,
        PlayerInventory playerInv,
        Inventory inventory,
        PropertyDelegate properties,
        ScreenHandlerContext context,
        EnchantmentExtractorBlockEntity blockEntity
    ) {
        super(com.lexxeous.enchantment_storage.registry.ModScreenHandlers.ENCHANTMENT_EXTRACTOR, syncId);

        checkSize(inventory, EnchantmentExtractorBlockEntity.INVENTORY_SIZE);
        checkDataCount(properties, PROPERTY_COUNT);

        this.inventory = inventory;
        this.properties = properties;
        this.context = context;
        this.blockEntity = blockEntity;

        inventory.onOpen(playerInv.player);

        int machineInputX = 76;
        int lapisInputY = 26;
        int itemInputY = 46;
        int outputSlotX = 130;
        int outputSlotY = 36;

        this.addSlot(new Slot(inventory, EnchantmentExtractorBlockEntity.SLOT_LAPIS, machineInputX, lapisInputY) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.LAPIS_LAZULI);
            }
        });

        this.addSlot(new Slot(inventory, EnchantmentExtractorBlockEntity.SLOT_INPUT, machineInputX, itemInputY) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return isValidBottomInput(stack);
            }
        });

        this.addSlot(new Slot(inventory, EnchantmentExtractorBlockEntity.SLOT_OUTPUT, outputSlotX, outputSlotY) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });

        int playerInvStartX = 8;
        int playerInvStartY = 140;
        int playerInvRowSpacing = 18;
        int playerInvColSpacing = 18;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                    playerInv,
                    col + row * 9 + 9,
                    playerInvStartX + col * playerInvColSpacing,
                    playerInvStartY + row * playerInvRowSpacing
                ));
            }
        }

        // Hotbar sits just below the 3 inventory rows.
        int hotbarY = playerInvStartY + (playerInvRowSpacing * 3) + 4;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, playerInvStartX + col * playerInvColSpacing, hotbarY));
        }

        this.addProperties(this.properties);
    }
    // endregion

    // region Getter(s) & Setter(s)
    public int getMode() {
        return this.properties.get(0);
    }

    private void setMode(int mode) {
        this.properties.set(0, mode);
    }

    public int getGridValue(int row, int col) {
        int index = 1 + (row * GRID_COLS) + col;
        if (index < 1 || index >= this.properties.size()) {
            return 0;
        }
        return this.properties.get(index);
    }

    public void setSelectedGridIndex(int gridIndex) {
        this.selectedGridIndex = gridIndex;
    }

    public void clearSelection() {
        this.selectedGridIndex = -1;
    }

    public EnchantmentExtractorBlockEntity getBlockEntity() {
        return blockEntity;
    }
    // endregion

    // region Helper(s)
    private boolean isStoreMode() {
        return getMode() == MODE_STORE;
    }

    private boolean isExtractMode() {
        return getMode() == MODE_EXTRACT;
    }

    private boolean isValidBottomInput(ItemStack stack) {
        return stack.isOf(Items.BOOK)
            || stack.isOf(Items.ENCHANTED_BOOK)
            || stack.isEnchantable()
            || stack.hasEnchantments();
    }

    public boolean canStore() {
        if (!isOutputEmpty()) return false;
        ItemStack stack = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT);
        return !stack.isEmpty() && (stack.hasEnchantments() || stack.isOf(Items.ENCHANTED_BOOK));
    }

    public boolean canExtract() {
        if (!isOutputEmpty()) return false;
        if (selectedGridIndex < 0) return false;

        int row = selectedGridIndex / GRID_COLS;
        int col = selectedGridIndex % GRID_COLS;
        if (getGridValue(row, col) <= 0) return false;

        ItemStack stack = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT);

        return !stack.isEmpty() && (stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK));
    }

    private boolean handleGridClick(PlayerEntity player, int gridIndex) {
        if (blockEntity == null || player.getEntityWorld().isClient()) {
            return false;
        }

        setSelectedGridIndex(gridIndex);
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
    // endregion

    private boolean handleStore(PlayerEntity player) {
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
        return true;
    }

    private boolean handleExtract(PlayerEntity player) {
        if (blockEntity == null || player.getEntityWorld().isClient()) {
            return false;
        }
        if (!canExtract()) {
            return false;
        }
        if (selectedGridIndex < 0) {
            return false;
        }

        int row = selectedGridIndex / GRID_COLS;
        int col = selectedGridIndex % GRID_COLS;
        int amount = getGridValue(row, col);
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
        ItemStack output = input.isOf(Items.ENCHANTED_BOOK)
            ? input.copyWithCount(1)
            : input.copyComponentsToNewStack(Items.ENCHANTED_BOOK, 1);
        EnchantmentHelper.apply(output, builder -> builder.set(enchantmentEntry, rank));

        inventory.setStack(EnchantmentExtractorBlockEntity.SLOT_OUTPUT, output);
        blockEntity.debugAdjustEnchantment(enchantmentId, col, -1);
        decrementInputStack(input);
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

    // region Override(s)
    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id == BUTTON_STORE) {
            setMode(MODE_STORE);
            if (!handleStore(player)) return false;
            clearSelection();
            return true;
        }

        if (id == BUTTON_EXTRACT) {
            setMode(MODE_EXTRACT);
            if (!handleExtract(player)) return false;
            clearSelection();
            return true;
        }

        if (id >= BUTTON_GRID_BASE) {
            return handleGridClick(player, id - BUTTON_GRID_BASE);
        }

        return super.onButtonClick(player, id);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasStack()) return ItemStack.EMPTY;

        ItemStack original = slot.getStack();
        newStack = original.copy();

        // Split between machine slots and player inventory slots.
        int machineSlotCount = EnchantmentExtractorBlockEntity.INVENTORY_SIZE;
        int playerInvStart = machineSlotCount;
        int playerInvEnd = playerInvStart + 36;

        // Shift-click move
        if (index < machineSlotCount) {
            if (!this.insertItem(original, playerInvStart, playerInvEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.insertItem(original, 0, machineSlotCount, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();

        return newStack;
    }
    // endregion

    // TODO: Ask about what Property Delegates are
    private static final class ModePropertyDelegate implements PropertyDelegate {
        private final EnchantmentExtractorBlockEntity blockEntity;

        private ModePropertyDelegate(EnchantmentExtractorBlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public int get(int index) {
            if (index == 0) {
                return blockEntity.getMode();
            }
            int gridIndex = index - 1;
            return blockEntity.getGridValueByIndex(gridIndex);
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                blockEntity.setMode(value);
                return;
            }
            int gridIndex = index - 1;
            blockEntity.setGridValueByIndex(gridIndex, value);
        }

        @Override
        public int size() {
            return PROPERTY_COUNT;
        }
    }
}
