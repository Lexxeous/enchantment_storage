package com.lexxeous.enchantment_storage.screen;

import com.lexxeous.enchantment_storage.EnchantmentStorage;
import com.lexxeous.enchantment_storage.screen.button.ButtonSection;
import com.lexxeous.enchantment_storage.screen.infotext.InfoTextSection;
import com.lexxeous.enchantment_storage.screen.list.ListSection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class EnchantmentExtractorClientScreen extends HandledScreen<EnchantmentExtractorScreenHandler> {
	// region Constants
	private static final Identifier BACKGROUND_TEXTURE =
		Identifier.of(EnchantmentStorage.MOD_ID, "textures/gui/container/enchantment_extractor.png");
	private static final Identifier LAPIS_PLACEHOLDER_TEXTURE =
		Identifier.ofVanilla("container/slot/lapis_lazuli");
	private static final int SCREEN_WIDTH = 176;
	private static final int SCREEN_HEIGHT = 222;
	private static final int LAPIS_SLOT_INDEX = 0;
	// endregion

	// region Class Variables
	private final ListSection listSection = new ListSection();
	private final InfoTextSection infoText = new InfoTextSection();
	private final ButtonSection actionButtons = new ButtonSection();
	// endregion

	// region Constructors
	public EnchantmentExtractorClientScreen(EnchantmentExtractorScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundWidth = SCREEN_WIDTH;
		this.backgroundHeight = SCREEN_HEIGHT;
	}
	// endregion

	// region UI
	// region Overrides
	@Override
	protected void init() {
		super.init();
		int x = (this.width - this.backgroundWidth) / 2;
		int y = (this.height - this.backgroundHeight) / 2;

		this.playerInventoryTitleX = 8;
		this.playerInventoryTitleY = 128;

		this.actionButtons.initialize(this, x, y);
	}

	@Override
	protected void drawBackground(net.minecraft.client.gui.DrawContext context, float delta, int mouseX, int mouseY) {
		int x = (this.width - this.backgroundWidth) / 2;
		int y = (this.height - this.backgroundHeight) / 2;

		context.drawTexture(
				RenderPipelines.GUI_TEXTURED,
				BACKGROUND_TEXTURE,
				x, y,
				0, 0,
				this.backgroundWidth, this.backgroundHeight,
				256, 256
		);

		listSection.drawList(context, x, y, mouseX, mouseY, this.textRenderer, this.handler);
		listSection.drawScrollBar(context, x, y, this.handler);
		drawInputPlaceholders(context, x, y);
		infoText.draw(context, x, y, listSection, this.handler, this.client, this.textRenderer);
	}

	@Override
	public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
		listSection.updateRegistryOrder(getEnchantmentRegistry());
		actionButtons.update(this.handler, listSection, this.client);
		super.render(context, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(context, mouseX, mouseY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (listSection.mouseScrolled(mouseX, mouseY, verticalAmount, getScreenX(), getScreenY(), this.handler)) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubleClick) {
		if (listSection.mouseClicked(click, getScreenX(), getScreenY(), this.handler, this.client)) {
			return true;
		}

		return super.mouseClicked(click, doubleClick);
	}

	@Override
	public boolean mouseDragged(net.minecraft.client.gui.Click click, double deltaX, double deltaY) {
		if (listSection.mouseDragged(click, getScreenX(), getScreenY(), this.handler)) {
			return true;
		}
		return super.mouseDragged(click, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(net.minecraft.client.gui.Click click) {
		listSection.mouseReleased();
		return super.mouseReleased(click);
	}

	public EnchantmentExtractorScreenHandler getHandler() {
		return this.handler;
	}

	public MinecraftClient getClient() {
		return this.client;
	}

	public <T extends net.minecraft.client.gui.Element
		& net.minecraft.client.gui.Drawable
		& net.minecraft.client.gui.Selectable> void addDrawableChildPublic(T child) {
		this.addDrawableChild(child);
	}
	// endregion

	// region Private
	private int getScreenX() {
		return (this.width - this.backgroundWidth) / 2;
	}

	private int getScreenY() {
		return (this.height - this.backgroundHeight) / 2;
	}

	private @Nullable Registry<Enchantment> getEnchantmentRegistry() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.world == null) {
			return null;
		}
		return client.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
	}

	private void drawInputPlaceholders(net.minecraft.client.gui.DrawContext context, int x, int y) {
		if (this.handler.getLapisStack().isEmpty() && !this.handler.slots.isEmpty()) {
			var lapisSlot = this.handler.slots.get(LAPIS_SLOT_INDEX);
			context.drawGuiTexture(
				RenderPipelines.GUI_TEXTURED,
				LAPIS_PLACEHOLDER_TEXTURE,
				x + lapisSlot.x,
				y + lapisSlot.y,
				16,
				16
			);
		}
	}
	// endregion
}