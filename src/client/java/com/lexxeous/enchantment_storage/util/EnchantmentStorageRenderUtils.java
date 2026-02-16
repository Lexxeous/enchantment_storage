package com.lexxeous.enchantment_storage.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class EnchantmentStorageRenderUtils {
	// region Constructors
	private EnchantmentStorageRenderUtils() {}
	// endregion

	// region Helpers
	public static void drawScaledText(
		DrawContext context,
		TextRenderer textRenderer,
		String text,
		int x,
		int y,
		float scale,
		int color
	) {
		drawScaledText(context, textRenderer, text, x, y, scale, color, false);
	}

	public static void drawScaledTextWithShadow(
		DrawContext context,
		TextRenderer textRenderer,
		String text,
		int x,
		int y,
		float scale,
		int color
	) {
		drawScaledText(context, textRenderer, text, x, y, scale, color, true);
	}

	private static void drawScaledText(
		DrawContext context,
		TextRenderer textRenderer,
		String text,
		int x,
		int y,
		float scale,
		int color,
		boolean shadow
	) {
		var matrices = context.getMatrices();
		matrices.pushMatrix();
		matrices.translate((float) x, (float) y);
		matrices.scale(scale, scale);
		if (shadow) {
			context.drawTextWithShadow(textRenderer, text, 0, 0, color);
		} else {
			context.drawText(textRenderer, text, 0, 0, color, false);
		}
		matrices.popMatrix();
	}
	// endregion
}