package com.lexxeous.enchantment_storage.screen;

import com.lexxeous.enchantment_storage.blockentity.EnchantmentExtractorBlockEntity;
import com.lexxeous.enchantment_storage.registry.ModBlocks;
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
import com.lexxeous.enchantment_storage.EnchantmentStorage;

public class EnchantmentExtractorScreenHandler extends ScreenHandler {
    public static final int BUTTON_STORE = 0;
    public static final int BUTTON_EXTRACT = 1;

    private static final int MODE_STORE = EnchantmentExtractorBlockEntity.MODE_STORE;
    private static final int MODE_EXTRACT = EnchantmentExtractorBlockEntity.MODE_EXTRACT;

    private final Inventory inventory;
    private final PropertyDelegate properties;
    private final ScreenHandlerContext context;

    // region Constructor(s)
    public EnchantmentExtractorScreenHandler(int syncId, PlayerInventory playerInv) {
        this(
            syncId,
            playerInv,
            new SimpleInventory(EnchantmentExtractorBlockEntity.INVENTORY_SIZE),
            new ArrayPropertyDelegate(1),
            ScreenHandlerContext.EMPTY
        );
    }

    public EnchantmentExtractorScreenHandler(
        int syncId,
        PlayerInventory playerInv,
        EnchantmentExtractorBlockEntity blockEntity,
        ScreenHandlerContext context
    ) {
        this(syncId, playerInv, blockEntity, new ModePropertyDelegate(blockEntity), context);
    }

    private EnchantmentExtractorScreenHandler(
        int syncId,
        PlayerInventory playerInv,
        Inventory inventory,
        PropertyDelegate properties,
        ScreenHandlerContext context
    ) {
        super(com.lexxeous.enchantment_storage.registry.ModScreenHandlers.ENCHANTMENT_EXTRACTOR, syncId);

        checkSize(inventory, EnchantmentExtractorBlockEntity.INVENTORY_SIZE);
        checkDataCount(properties, 1);

        this.inventory = inventory;
        this.properties = properties;
        this.context = context;

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

    // region Getters & Setters
    public int getMode() {
        return this.properties.get(0);
    }

    private void setMode(int mode) {
        this.properties.set(0, mode);
    }
    // endregion

    // region Helpers
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
        ItemStack stack = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT);
        return !stack.isEmpty() && (stack.hasEnchantments() || stack.isOf(Items.ENCHANTED_BOOK));
    }

    public boolean canExtract() {
        ItemStack stack = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT);
        return !stack.isEmpty() && (stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK));
    }
    // endregion

    // region Override(s)
    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id == BUTTON_STORE) {
            if (!canStore()) return false;
            setMode(MODE_STORE);
            EnchantmentStorage.LOGGER.info("Store clicked by {}", player.getName().getString());
            return true;
        }

        if (id == BUTTON_EXTRACT) {
            if (!canExtract()) return false;
            setMode(MODE_EXTRACT);
            EnchantmentStorage.LOGGER.info("Extract clicked by {}", player.getName().getString());
            return true;
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
            if (index == 0) return blockEntity.getMode();
            return 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) blockEntity.setMode(value);
        }

        @Override
        public int size() {
            return 1;
        }
    }
}
