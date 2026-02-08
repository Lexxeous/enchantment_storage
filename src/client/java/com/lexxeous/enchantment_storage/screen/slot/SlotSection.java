package com.lexxeous.enchantment_storage.screen.slot;

import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorScreenHandler;
import net.minecraft.client.gui.DrawContext;

public final class SlotSection {
    private static final int SLOT_OUTLINE_COLOR = 0xFF00FF00;

    public void draw(DrawContext context, int x, int y, EnchantmentExtractorScreenHandler handler) {
        int outlineThickness = 1;
        int slotSize = 16;
        int machineSlotCount = 3;
        for (int i = 0; i < machineSlotCount && i < handler.slots.size(); i++) {
            var slot = handler.slots.get(i);
            int slotX = x + slot.x;
            int slotY = y + slot.y;
            context.fill(slotX, slotY, slotX + slotSize, slotY + outlineThickness, SLOT_OUTLINE_COLOR);
            context.fill(slotX, slotY + slotSize - outlineThickness, slotX + slotSize, slotY + slotSize, SLOT_OUTLINE_COLOR);
            context.fill(slotX, slotY, slotX + outlineThickness, slotY + slotSize, SLOT_OUTLINE_COLOR);
            context.fill(slotX + slotSize - outlineThickness, slotY, slotX + slotSize, slotY + slotSize, SLOT_OUTLINE_COLOR);
        }
    }
}
