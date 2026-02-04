package com.lexxeous.enchantment_storage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class EnchantmentExtractorScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public EnchantmentExtractorScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, new SimpleInventory(EnchantmentExtractorBlockEntity.INVENTORY_SIZE));
    }

    public EnchantmentExtractorScreenHandler(int syncId, PlayerInventory playerInv, Inventory blockInv) {
        super(ModScreenHandlers.ENCHANTMENT_EXTRACTOR, syncId);

        this.inventory = blockInv;

        // If your Inventory isn't a SimpleInventory, ensure size matches.
        checkSize(blockInv, EnchantmentExtractorBlockEntity.INVENTORY_SIZE);

        // Machine slots (x,y are GUI coordinates)
        this.addSlot(new Slot(blockInv, EnchantmentExtractorBlockEntity.SLOT_INPUT, 44, 35));
        this.addSlot(new Slot(blockInv, EnchantmentExtractorBlockEntity.SLOT_BOOK, 44, 58));
        this.addSlot(new Slot(blockInv, EnchantmentExtractorBlockEntity.SLOT_OUTPUT, 116, 46) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false; // output-only
            }
        });

        // Player inventory (3 rows)
        int startX = 8;
        int startY = 84;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, startX + col * 18, startY + row * 18));
            }
        }

        // Hotbar
        int hotbarY = startY + 58;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, startX + col * 18, hotbarY));
        }
    }

    public record ScreenData(BlockPos pos) {
        public static final PacketCodec<RegistryByteBuf, ScreenData> CODEC =
                PacketCodec.tuple(BlockPos.PACKET_CODEC, ScreenData::pos, ScreenData::new);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    // Optional but nice: shift-click behavior
    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasStack()) return ItemStack.EMPTY;

        ItemStack original = slot.getStack();
        newStack = original.copy();

        int machineSlotCount = EnchantmentExtractorBlockEntity.INVENTORY_SIZE;
        int playerInvStart = machineSlotCount;
        int playerInvEnd = playerInvStart + 36; // 27 main + 9 hotbar

        if (index < machineSlotCount) {
            // Moving from machine to player
            if (!this.insertItem(original, playerInvStart, playerInvEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Moving from player to machine (try input/book first)
            if (!this.insertItem(original, 0, machineSlotCount - 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();

        return newStack;
    }
}