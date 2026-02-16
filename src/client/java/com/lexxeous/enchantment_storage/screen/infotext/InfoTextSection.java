package com.lexxeous.enchantment_storage.screen.infotext;

import com.lexxeous.enchantment_storage.constant.EnchantmentStorageUiColorConstants;
import com.lexxeous.enchantment_storage.logic.EnchantmentStorageActionLogic;
import com.lexxeous.enchantment_storage.logic.EnchantmentStorageUiLogic;
import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorScreenHandler;
import com.lexxeous.enchantment_storage.screen.list.ListSection;
import com.lexxeous.enchantment_storage.util.EnchantmentStorageCacheUtils;
import com.lexxeous.enchantment_storage.util.EnchantmentStorageRenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class InfoTextSection {
	// region Constants
	private static final int INFO_TEXT_X = 78;
	private static final int INFO_EXP_Y_OFFSET = 65;
	private static final int INFO_SPACING = 12;
	private static final int INFO_TOTAL_Y_OFFSET = INFO_EXP_Y_OFFSET + INFO_SPACING;
	private static final int INFO_LEVELS_Y_OFFSET = INFO_TOTAL_Y_OFFSET + INFO_SPACING;
	private static final int INFO_TEXT_COLOR =
		EnchantmentStorageUiColorConstants.withArgb(EnchantmentStorageUiColorConstants.INFO_TEXT);
	private static final float INFO_TEXT_SCALE = 0.85f;
	// endregion

	// region Class Variables
	private final EnchantmentStorageCacheUtils.StoreCostCache storeCostCache = new EnchantmentStorageCacheUtils.StoreCostCache();
	private int cachedInfoFingerprint = Integer.MIN_VALUE;
	private String cachedExperienceLine = "Experience Cost: ＿";
	private String cachedTotalLine = "Total Remaining: ＿";
	private String cachedLevelsLine = "Levels Remaining: ＿";
	// endregion

	// region UI
	public void draw(
		DrawContext context,
		int x,
		int y,
		ListSection listSection,
		EnchantmentExtractorScreenHandler handler,
		MinecraftClient client,
		TextRenderer textRenderer
	) {
		String totalText = listSection.getTotalRemainingText(handler);
		String levelsText = listSection.getLevelsRemainingText(handler);
		int experienceCost = getExperienceCost(listSection, handler);
		String experienceCostText = experienceCost < 0 ? "＿" : String.valueOf(experienceCost);
		int experienceCostColor = getExperienceCostColor(experienceCost, client);
		boolean drawExperienceShadow = shouldDrawExperienceShadow(experienceCost);
		updateCachedInfoLines(totalText, levelsText, experienceCostText, experienceCostColor, drawExperienceShadow);

		if (drawExperienceShadow) {
			EnchantmentStorageRenderUtils.drawScaledTextWithShadow(
				context,
				textRenderer,
				cachedExperienceLine,
				x + INFO_TEXT_X,
				y + INFO_EXP_Y_OFFSET,
				INFO_TEXT_SCALE,
				experienceCostColor
			);
		} else {
			EnchantmentStorageRenderUtils.drawScaledText(
				context,
				textRenderer,
				cachedExperienceLine,
				x + INFO_TEXT_X,
				y + INFO_EXP_Y_OFFSET,
				INFO_TEXT_SCALE,
				experienceCostColor
			);
		}

		EnchantmentStorageRenderUtils.drawScaledText(
			context,
			textRenderer,
			cachedTotalLine,
			x + INFO_TEXT_X,
			y + INFO_TOTAL_Y_OFFSET,
			INFO_TEXT_SCALE,
			INFO_TEXT_COLOR
		);

		EnchantmentStorageRenderUtils.drawScaledText(
			context,
			textRenderer,
			cachedLevelsLine,
			x + INFO_TEXT_X,
			y + INFO_LEVELS_Y_OFFSET,
			INFO_TEXT_SCALE,
			INFO_TEXT_COLOR
		);
	}
	// endregion

	// region Validation
	private boolean shouldDrawExperienceShadow(int cost) {
		return cost >= 0;
	}

	private int getExperienceCostColor(int cost, MinecraftClient client) {
		if (cost < 0) {
			return INFO_TEXT_COLOR;
		}
		var player = client == null ? null : client.player;
		if (player != null && (player.isCreative() || player.experienceLevel >= cost)) {
			return EnchantmentStorageUiColorConstants.withArgb(EnchantmentStorageUiColorConstants.EXPERIENCE_AFFORDABLE);
		}
		return EnchantmentStorageUiColorConstants.withArgb(EnchantmentStorageUiColorConstants.EXPERIENCE_UNAFFORDABLE);
	}
	// endregion

	// region Helpers
	private int getExperienceCost(ListSection listSection, EnchantmentExtractorScreenHandler handler) {
		int lapisCount = handler.getLapisStack().isEmpty() ? 0 : handler.getLapisStack().getCount();
		if (listSection.hasSelection()) {
			int selectedCost = listSection.getSelectedExtractCost(handler);
			if (selectedCost <= 0) {
				return -1;
			}
			return EnchantmentStorageActionLogic.getDiscountedCost(selectedCost, lapisCount);
		}
		int storeCost = getCachedStoreExperienceCost(handler);
		if (storeCost <= 0) {
			return -1;
		}
		return EnchantmentStorageActionLogic.getDiscountedCost(storeCost, lapisCount);
	}

	private int getCachedStoreExperienceCost(EnchantmentExtractorScreenHandler handler) {
		return storeCostCache.get(handler.getInputStack());
	}

	private void updateCachedInfoLines(
		String totalText,
		String levelsText,
		String experienceCostText,
		int experienceCostColor,
		boolean drawExperienceShadow
	) {
		int fingerprint = EnchantmentStorageUiLogic.computeInfoFingerprint(
			totalText,
			levelsText,
			experienceCostText,
			experienceCostColor,
			drawExperienceShadow
		);
		if (fingerprint == cachedInfoFingerprint) {
			return;
		}

		cachedExperienceLine = EnchantmentStorageUiLogic.computeExperienceCostLine(experienceCostText);
		cachedTotalLine = EnchantmentStorageUiLogic.computeTotalRemainingLine(totalText);
		cachedLevelsLine = EnchantmentStorageUiLogic.computeLevelsRemainingLine(levelsText);
		cachedInfoFingerprint = fingerprint;
	}
	// endregion
}
