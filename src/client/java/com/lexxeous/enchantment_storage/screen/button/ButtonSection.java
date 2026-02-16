package com.lexxeous.enchantment_storage.screen.button;

import com.lexxeous.enchantment_storage.logic.EnchantmentStorageActionLogic;
import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorClientScreen;
import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorScreenHandler;
import com.lexxeous.enchantment_storage.screen.list.ListSection;
import com.lexxeous.enchantment_storage.util.EnchantmentStorageCacheUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ButtonSection {
	// region Class Variables
	private ButtonWidget storeButton;
	private ButtonWidget extractButton;
	private final EnchantmentStorageCacheUtils.StoreCostCache storeCostCache = new EnchantmentStorageCacheUtils.StoreCostCache();
	// endregion

	// region Registration & Initialization
	public void initialize(EnchantmentExtractorClientScreen screen, int x, int y) {
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
	// endregion

	// region Helpers
	public void update(
		EnchantmentExtractorScreenHandler handler,
		ListSection listSection,
		MinecraftClient client
	) {
		if (storeButton == null || extractButton == null) return;
		int lapisCount = handler.getLapisStack().isEmpty() ? 0 : handler.getLapisStack().getCount();
		boolean creativeMode = client != null && client.player != null && client.player.isCreative();
		int experienceLevel = (client != null && client.player != null) ? client.player.experienceLevel : Integer.MAX_VALUE;

		int storeBaseCost = storeCostCache.get(handler.getInputStack());
		storeButton.active = EnchantmentStorageActionLogic.isStoreButtonActive(
			handler.canStore(),
			storeBaseCost,
			lapisCount,
			creativeMode,
			experienceLevel
		);

		extractButton.active = EnchantmentStorageActionLogic.isExtractButtonActive(
			handler.canExtract(),
			listSection.isSelectedEnchantmentOnInput(handler),
			listSection.getSelectedExtractCost(handler),
			lapisCount,
			creativeMode,
			experienceLevel
		);
	}
	// endregion
}
