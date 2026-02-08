package com.lexxeous.enchantment_storage.screen.slot;

import com.lexxeous.enchantment_storage.blockentity.EnchantmentExtractorBlockEntity;
import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorScreenHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public final class SlotHandler {
    private SlotHandler() {}

    public static void addSlots(
        EnchantmentExtractorScreenHandler handler,
        Inventory inventory,
        PlayerInventory playerInv
    ) {
        int machineInputX = 76;
        int lapisInputY = 18;
        int itemInputY = 38;
        int outputSlotX = 130;
        int outputSlotY = 28;

        handler.addSlotPublic(new Slot(inventory, EnchantmentExtractorBlockEntity.SLOT_LAPIS, machineInputX, lapisInputY) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.LAPIS_LAZULI);
            }
        });

        handler.addSlotPublic(new Slot(inventory, EnchantmentExtractorBlockEntity.SLOT_INPUT, machineInputX, itemInputY) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return isValidBottomInput(stack);
            }
        });

        handler.addSlotPublic(new Slot(inventory, EnchantmentExtractorBlockEntity.SLOT_OUTPUT, outputSlotX, outputSlotY) {
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
                handler.addSlotPublic(new Slot(
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
            handler.addSlotPublic(new Slot(playerInv, col, playerInvStartX + col * playerInvColSpacing, hotbarY));
        }
    }

    private static boolean isValidBottomInput(ItemStack stack) {
        return stack.isOf(Items.BOOK)
            || stack.isOf(Items.ENCHANTED_BOOK)
            || stack.isEnchantable()
            || stack.hasEnchantments();
    }
}
