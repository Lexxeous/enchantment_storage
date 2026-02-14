package com.lexxeous.enchantment_storage.screen.button;

import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorScreenHandler;
import com.lexxeous.enchantment_storage.screen.EnchantmentStorageClientScreen;
import com.lexxeous.enchantment_storage.screen.list.ListSection;
import com.lexxeous.enchantment_storage.util.EnchantmentStorageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ButtonSection {
    private ButtonWidget storeButton;
    private ButtonWidget extractButton;
    private final EnchantmentStorageUtils.StoreCostCache storeCostCache = new EnchantmentStorageUtils.StoreCostCache();

    public void init(EnchantmentStorageClientScreen screen, int x, int y) {
        int storeButtonX = x + 78;
        int extractButtonX = x + 126;
        int actionButtonY = y + 107;

        this.storeButton = ButtonWidget.builder(Text.literal("Store"), button -> {
                screen.getClient().interactionManager.clickButton(
                        screen.getHandler().syncId,
                        EnchantmentExtractorScreenHandler.BUTTON_STORE
                );
                screen.getHandler().clearSelection();
        }).dimensions(storeButtonX, actionButtonY, 44, 16).build();
        screen.addDrawableChildPublic(this.storeButton);

        this.extractButton = ButtonWidget.builder(Text.literal("Extract"), button -> {
                screen.getClient().interactionManager.clickButton(
                        screen.getHandler().syncId,
                        EnchantmentExtractorScreenHandler.BUTTON_EXTRACT
                );
                screen.getHandler().clearSelection();
        }).dimensions(extractButtonX, actionButtonY, 44, 16).build();
        screen.addDrawableChildPublic(this.extractButton);
    }

    public void update(
        EnchantmentExtractorScreenHandler handler,
        ListSection listSection,
        MinecraftClient client
    ) {
        if (storeButton == null || extractButton == null) return;
        int storeCost = EnchantmentStorageUtils.getDiscountedCost(
            storeCostCache.get(handler.getInputStack()),
            handler.getLapisStack()
        );
        storeButton.active = handler.canStore() && canAffordExperience(storeCost, client);
        boolean canExtract = handler.canExtract();
        if (canExtract && listSection.isSelectedEnchantmentOnInput(handler)) {
            canExtract = false;
        }
        int extractCost = EnchantmentStorageUtils.getDiscountedCost(
            listSection.getSelectedExtractCost(handler),
            handler.getLapisStack()
        );
        if (canExtract && !canAffordExperience(extractCost, client)) {
            canExtract = false;
        }
        extractButton.active = canExtract;
    }

    private boolean canAffordExperience(int cost, MinecraftClient client) {
        if (cost <= 0) {
            return true;
        }
        var player = client == null ? null : client.player;
        if (player == null) {
            return true;
        }
        return player.isCreative() || player.experienceLevel >= cost;
    }
}
