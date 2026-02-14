package com.lexxeous.enchantment_storage.screen.infotext;

import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorScreenHandler;
import com.lexxeous.enchantment_storage.screen.list.ListSection;
import com.lexxeous.enchantment_storage.util.EnchantmentStorageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class InfoTextSection {
    private static final int INFO_TEXT_X = 78;
    private static final int INFO_EXP_Y_OFFSET = 65;
    private static final int INFO_SPACING = 12;
    private static final int INFO_TOTAL_Y_OFFSET = INFO_EXP_Y_OFFSET + INFO_SPACING;
    private static final int INFO_LEVELS_Y_OFFSET = INFO_TOTAL_Y_OFFSET + INFO_SPACING;
    private static final int INFO_TEXT_COLOR = 0xFF404040;
    private static final float INFO_TEXT_SCALE = 0.85f;
    private final EnchantmentStorageUtils.StoreCostCache storeCostCache = new EnchantmentStorageUtils.StoreCostCache();

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

        if (drawExperienceShadow) {
            drawScaledTextWithShadow(
                context,
                textRenderer,
                "Experience Cost: " + experienceCostText,
                x + INFO_TEXT_X,
                y + INFO_EXP_Y_OFFSET,
                INFO_TEXT_SCALE,
                experienceCostColor
            );
        } else {
            drawScaledText(
                context,
                textRenderer,
                "Experience Cost: " + experienceCostText,
                x + INFO_TEXT_X,
                y + INFO_EXP_Y_OFFSET,
                INFO_TEXT_SCALE,
                experienceCostColor
            );
        }

        drawScaledText(
            context,
            textRenderer,
            "Total Remaining: " + totalText,
            x + INFO_TEXT_X,
            y + INFO_TOTAL_Y_OFFSET,
            INFO_TEXT_SCALE,
            INFO_TEXT_COLOR
        );

        drawScaledText(
            context,
            textRenderer,
            "Levels Remaining: " + levelsText,
            x + INFO_TEXT_X,
            y + INFO_LEVELS_Y_OFFSET,
            INFO_TEXT_SCALE,
            INFO_TEXT_COLOR
        );
    }

    private void drawScaledText(
        DrawContext context,
        TextRenderer textRenderer,
        String text,
        int x,
        int y,
        float scale,
        int color
    ) {
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate((float) x, (float) y);
        matrices.scale(scale, scale);
        context.drawText(textRenderer, text, 0, 0, color, false);
        matrices.popMatrix();
    }

    private void drawScaledTextWithShadow(
        DrawContext context,
        TextRenderer textRenderer,
        String text,
        int x,
        int y,
        float scale,
        int color
    ) {
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate((float) x, (float) y);
        matrices.scale(scale, scale);
        context.drawTextWithShadow(textRenderer, text, 0, 0, color);
        matrices.popMatrix();
    }

    private boolean shouldDrawExperienceShadow(int cost) {
        return cost >= 0;
    }

    private int getExperienceCostColor(int cost, MinecraftClient client) {
        if (cost < 0) {
            return INFO_TEXT_COLOR;
        }
        var player = client == null ? null : client.player;
        if (player != null && (player.isCreative() || player.experienceLevel >= cost)) {
            return 0xFF80FF20;
        }
        return 0xFFFF6060;
    }

    private int getExperienceCost(ListSection listSection, EnchantmentExtractorScreenHandler handler) {
        if (listSection.hasSelection()) {
            int selectedCost = listSection.getSelectedExtractCost(handler);
            if (selectedCost <= 0) {
                return -1;
            }
            return EnchantmentStorageUtils.getDiscountedCost(
                selectedCost,
                handler.getLapisStack()
            );
        }
        int storeCost = getStoreExperienceCost(handler);
        if (storeCost <= 0) {
            return -1;
        }
        return EnchantmentStorageUtils.getDiscountedCost(
            storeCost,
            handler.getLapisStack()
        );
    }

    private int getStoreExperienceCost(EnchantmentExtractorScreenHandler handler) {
        return storeCostCache.get(handler.getInputStack());
    }
}
