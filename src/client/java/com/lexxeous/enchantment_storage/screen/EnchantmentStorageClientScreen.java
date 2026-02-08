package com.lexxeous.enchantment_storage.screen;

import com.lexxeous.enchantment_storage.mapping.EnchantmentCategoriesHarness;
import com.lexxeous.enchantment_storage.util.EnchantmentStorageUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EnchantmentStorageClientScreen extends HandledScreen<EnchantmentExtractorScreenHandler> {
    // region Constant(s)
    private static final Identifier BACKGROUND_TEXTURE =
            Identifier.of("minecraft", "textures/gui/container/generic_54.png");

    private static final int RANK_COUNT = 5;
    private static final int SCROLL_X_OFFSET = -5;
    private static final int SCROLL_WIDTH = 3;
    private static final int SLOT_OUTLINE_COLOR = 0xFF00FF00;
    private static final int INFO_TEXT_X = 78;
    private static final int INFO_TOTAL_Y_OFFSET = 75;
    private static final int INFO_LEVELS_Y_OFFSET = 87;
    private static final int INFO_TEXT_COLOR = 0xFF000000;
    private static final float INFO_TEXT_SCALE = 0.85f;
    private static final int LIST_X_OFFSET = 10;
    private static final int LIST_Y_OFFSET = 18;
    private static final int LIST_WIDTH = 60;
    private static final int LIST_HEIGHT = 105;
    private static final int LIST_ITEM_HEIGHT = 21;
    private static final float LIST_ITEM_TEXT_SCALE = 0.70f;
    // endregion

    // region Class variables
    private ButtonWidget storeButton;
    private ButtonWidget extractButton;
    private int scrollOffset = 0;
    private int selectedIndex = -1;
    private int selectedLevel = -1;
    private boolean isDraggingScroll = false;
    private List<Identifier> enchantmentOrder = List.of();
    // endregion

    // region Constructor(s)
    public EnchantmentStorageClientScreen(EnchantmentExtractorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 222;
    }
    // endregion

    // region Override(s)
    @Override
    protected void init() {
        super.init();
        this.enchantmentOrder = EnchantmentCategoriesHarness.getRegistryOrder(getEnchantmentRegistry());
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        this.playerInventoryTitleX = 8;
        this.playerInventoryTitleY = 128;

        int storeButtonX = x + 78;
        int extractButtonX = x + 126;
        int actionButtonY = y + 107;

        this.storeButton = ButtonWidget.builder(Text.literal("Store"), button -> {
                this.client.interactionManager.clickButton(
                        this.handler.syncId,
                        EnchantmentExtractorScreenHandler.BUTTON_STORE
                );
                this.handler.clearSelection();
        }).dimensions(storeButtonX, actionButtonY, 44, 16).build();
        this.addDrawableChild(this.storeButton);

        this.extractButton = ButtonWidget.builder(Text.literal("Extract"), button -> {
                this.client.interactionManager.clickButton(
                        this.handler.syncId,
                        EnchantmentExtractorScreenHandler.BUTTON_EXTRACT
                );
                this.handler.clearSelection();
        }).dimensions(extractButtonX, actionButtonY, 44, 16).build();
        this.addDrawableChild(this.extractButton);
    }

    @Override
    protected void drawBackground(net.minecraft.client.gui.DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(
                RenderPipelines.GUI,
                BACKGROUND_TEXTURE,
                x, y,
                0, 0,
                this.backgroundWidth, this.backgroundHeight,
                256, 256
        );

        drawSelectionList(context, x, y);
        drawScrollBar(context, x, y);
        drawSlotOutlines(context, x, y);
        drawRemainingInfo(context, x, y);
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        updateActionButtons();
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isMouseOverList(mouseX, mouseY)) {
            // Clamp scroll offset to the number of off-screen entries.
            int maxOffset = Math.max(0, getEntryCount() - getVisibleEntries());
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset - (int) Math.signum(verticalAmount)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubleClick) {
        if (click.button() == 0 && isMouseOverScrollBar(click.x(), click.y())) {
            isDraggingScroll = true;
            updateScrollOffsetFromMouse(click.y());
            return true;
        }
        if (click.button() == 0 && isMouseOverList(click.x(), click.y())) {
            int x = (this.width - this.backgroundWidth) / 2;
            int y = (this.height - this.backgroundHeight) / 2;
            int localY = (int) click.y() - (y + LIST_Y_OFFSET);
            int index = scrollOffset + (localY / LIST_ITEM_HEIGHT);
            if (index >= 0 && index < getEntryCount()) {
                List<Integer> rows = getVisibleRows();
                selectedIndex = index;
                int localX = (int) click.x() - (x + LIST_X_OFFSET);
                selectedLevel = getRankIndexFromLocal(localX, localY);
                if (selectedLevel >= 0 && index < rows.size()) {
                    int rowIndex = rows.get(index);
                    int gridIndex = (rowIndex * RANK_COUNT) + selectedLevel;
                    this.handler.setSelectedGridIndex(gridIndex);
                    if (this.client != null && this.client.interactionManager != null) {
                        this.client.interactionManager.clickButton(
                            this.handler.syncId,
                            EnchantmentExtractorScreenHandler.BUTTON_GRID_BASE + gridIndex
                        );
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.gui.Click click, double deltaX, double deltaY) {
        if (isDraggingScroll) {
            updateScrollOffsetFromMouse(click.y());
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.gui.Click click) {
        isDraggingScroll = false;
        return super.mouseReleased(click);
    }
    // endregion

    // region Helper(s)
    // region Helper method(s)
    private void drawSelectionList(net.minecraft.client.gui.DrawContext context, int x, int y) {
        if (enchantmentOrder.isEmpty()) {
            enchantmentOrder = EnchantmentCategoriesHarness.getRegistryOrder(getEnchantmentRegistry());
        }
        int listX = x + LIST_X_OFFSET;
        int listY = y + LIST_Y_OFFSET;
        List<Integer> rows = getVisibleRows();
        int visible = getVisibleEntries();
        int maxOffset = Math.max(0, rows.size() - visible);
        if (scrollOffset > maxOffset) {
            scrollOffset = maxOffset;
        }
        int end = Math.min(rows.size(), scrollOffset + visible);
        int entryPaddingX = 2;
        int entryTextYOffset = 2;
        int rankRowYOffset = 10;
        int rankSquareSize = 10;
        int listContentWidth = LIST_WIDTH - (entryPaddingX * 2);
        // Evenly space rank squares across the row (CSS space-between style).
        double rankGap = Math.max(1.0, (listContentWidth - (RANK_COUNT * rankSquareSize)) / (double) (RANK_COUNT + 1));

        if (selectedIndex >= rows.size()) {
            selectedIndex = -1;
            selectedLevel = -1;
        }

        for (int i = scrollOffset; i < end; i++) {
            int row = i - scrollOffset;
            int rowIndex = rows.get(i);
            int top = listY + row * LIST_ITEM_HEIGHT;
            int bottom = top + LIST_ITEM_HEIGHT - 1;
            int bg = i == selectedIndex ? 0xFFBBBBBB : 0xFFDDDDDD;
            context.fill(listX, top, listX + LIST_WIDTH, bottom, bg);
            drawScaledText(context, getEnchantmentName(rowIndex), listX + entryPaddingX, top + entryTextYOffset, LIST_ITEM_TEXT_SCALE, 0xFF000000);

            int rankStartX = listX + entryPaddingX;
            int rankY = top + rankRowYOffset;
            for (int r = 0; r < RANK_COUNT; r++) {
                int rx = rankStartX + (int) Math.round(rankGap + r * (rankSquareSize + rankGap));
                context.fill(rx, rankY, rx + rankSquareSize, rankY + rankSquareSize, 0xFFAAAAAA);
                context.fill(rx + 1, rankY + 1, rx + (rankSquareSize - 1), rankY + (rankSquareSize - 1), 0xFFEEEEEE);
                String numeral = getRomanNumeral(r);
                float numeralScale = 0.6f;

                // Center the roman numeral inside its square.
                int textWidth = (int) (this.textRenderer.getWidth(numeral) * numeralScale);
                int textHeight = (int) (this.textRenderer.fontHeight * numeralScale);
                int textX = rx + (rankSquareSize - textWidth) / 2;
                int textY = rankY + (rankSquareSize - textHeight) / 2 + 1;
                if (r >= 2) textX += 1;
                drawScaledText(context, numeral, textX, textY, numeralScale, 0xFF000000);
            }
        }
    }

    private void drawScrollBar(net.minecraft.client.gui.DrawContext context, int x, int y) {
        int listX = x + LIST_X_OFFSET;
        int listY = y + LIST_Y_OFFSET;
        int barX = listX + SCROLL_X_OFFSET;
        int barY = listY;
        int barHeight = LIST_HEIGHT - 1;
        int maxOffset = Math.max(0, getEntryCount() - getVisibleEntries());

        context.fill(barX, barY, barX + SCROLL_WIDTH, barY + barHeight, 0xFFCCCCCC);
        if (maxOffset == 0) {
            context.fill(barX, barY, barX + SCROLL_WIDTH, barY + barHeight, 0xFF999999);
            return;
        }

        // Thumb size scales with visible entries vs total entries.
        int thumbHeight = Math.max(8, (barHeight * getVisibleEntries()) / Math.max(1, getEntryCount()));
        int thumbY = barY + (barHeight - thumbHeight) * scrollOffset / maxOffset;
        context.fill(barX, thumbY, barX + SCROLL_WIDTH, thumbY + thumbHeight, 0xFF888888);
    }

    private void drawScaledText(net.minecraft.client.gui.DrawContext context, String text, int x, int y, float scale, int color) {
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate((float) x, (float) y);
        matrices.scale(scale, scale);
        context.drawText(this.textRenderer, text, 0, 0, color, false);
        matrices.popMatrix();
    }

    private void drawRemainingInfo(net.minecraft.client.gui.DrawContext context, int x, int y) {
        String totalText = getTotalRemainingText();
        String levelsText = getLevelsRemainingText();

        drawScaledText(
                context,
                "Total Remaining: " + totalText,
                x + INFO_TEXT_X,
                y + INFO_TOTAL_Y_OFFSET,
                INFO_TEXT_SCALE,
                INFO_TEXT_COLOR
        );

        drawScaledText(
                context,
                "Levels Remaining: " + levelsText,
                x + INFO_TEXT_X,
                y + INFO_LEVELS_Y_OFFSET,
                INFO_TEXT_SCALE,
                INFO_TEXT_COLOR
        );
    }

    private void drawSlotOutlines(net.minecraft.client.gui.DrawContext context, int x, int y) {
        int outlineThickness = 1;
        int slotSize = 16;
        int machineSlotCount = 3;
        for (int i = 0; i < machineSlotCount && i < this.handler.slots.size(); i++) {
            var slot = this.handler.slots.get(i);
            int slotX = x + slot.x;
            int slotY = y + slot.y;
            context.fill(slotX, slotY, slotX + slotSize, slotY + outlineThickness, SLOT_OUTLINE_COLOR);
            context.fill(slotX, slotY + slotSize - outlineThickness, slotX + slotSize, slotY + slotSize, SLOT_OUTLINE_COLOR);
            context.fill(slotX, slotY, slotX + outlineThickness, slotY + slotSize, SLOT_OUTLINE_COLOR);
            context.fill(slotX + slotSize - outlineThickness, slotY, slotX + slotSize, slotY + slotSize, SLOT_OUTLINE_COLOR);
        }
    }

    private int getVisibleEntries() {
        return LIST_HEIGHT / LIST_ITEM_HEIGHT;
    }

    private void updateScrollOffsetFromMouse(double mouseY) {
        int maxOffset = Math.max(0, getEntryCount() - getVisibleEntries());
        if (maxOffset == 0) {
            scrollOffset = 0;
            return;
        }
        int y = (this.height - this.backgroundHeight) / 2;
        int barY = y + LIST_Y_OFFSET;
        int barHeight = LIST_HEIGHT - 1;
        int thumbHeight = Math.max(8, (barHeight * getVisibleEntries()) / Math.max(1, getEntryCount()));
        double track = barHeight - thumbHeight;
        double normalized = (mouseY - barY - (thumbHeight / 2.0)) / track;
        int next = (int) Math.round(normalized * maxOffset);
        scrollOffset = Math.max(0, Math.min(maxOffset, next));
    }

    private void updateActionButtons() {
        if (storeButton == null || extractButton == null) return;
        storeButton.active = this.handler.canStore();
        extractButton.active = this.handler.canExtract();
    }



    // region Remaining
    private String getTotalRemainingText() {
        if (selectedIndex < 0) return "__";
        List<Integer> rows = getVisibleRows();
        if (selectedIndex >= rows.size()) return "__";
        int rowIndex = rows.get(selectedIndex);
        return String.valueOf(getTotalForRow(rowIndex));
    }

    private String getLevelsRemainingText() {
        if (selectedIndex < 0 || selectedLevel < 0) return "__";
        List<Integer> rows = getVisibleRows();
        if (selectedIndex >= rows.size()) return "__";
        int rowIndex = rows.get(selectedIndex);
        return String.valueOf(getLevelCount(rowIndex, selectedLevel));
    }
    // endregion

    private String getEnchantmentName(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= enchantmentOrder.size()) {
            return "";
        }
        Identifier id = enchantmentOrder.get(rowIndex);
        String translationKey = "enchantment." + id.getNamespace() + "." + id.getPath();
        String name = Text.translatable(translationKey).getString();
        if (name.equals(translationKey)) {
            return EnchantmentStorageUtils.formatFallbackName(id.getPath());
        }
        return name;
    }

    private String getRomanNumeral(int levelIndex) {
        return switch (levelIndex) {
            case 0 -> "I";
            case 1 -> "II";
            case 2 -> "III";
            case 3 -> "IV";
            case 4 -> "V";
            default -> "";
        };
    }

    private int getRankIndexFromLocal(int localX, int localY) {
        int entryPaddingX = 2;
        int rankRowYOffset = 12;
        int rankCellSize = 10;
        int listContentWidth = LIST_WIDTH - (entryPaddingX * 2);
        // Match hitboxes to the same spacing math used for rendering.
        double rankGap = Math.max(1.0, (listContentWidth - (RANK_COUNT * rankCellSize)) / (double) (RANK_COUNT + 1));
        int rowIndex = localY / LIST_ITEM_HEIGHT;
        int rowTop = rowIndex * LIST_ITEM_HEIGHT;
        int rankTop = rowTop + rankRowYOffset;
        if (localY < rankTop || localY > rankTop + rankCellSize) return -1;
        int rankStartX = entryPaddingX;
        for (int r = 0; r < RANK_COUNT; r++) {
            int rx = rankStartX + (int) Math.round(rankGap + r * (rankCellSize + rankGap));
            if (localX >= rx && localX <= rx + rankCellSize) return r;
        }
        return -1;
    }

    private int getEntryCount() {
        if (enchantmentOrder.isEmpty()) {
            enchantmentOrder = EnchantmentCategoriesHarness.getRegistryOrder(getEnchantmentRegistry());
        }
        return getVisibleRows().size();
    }

    private List<Integer> getVisibleRows() {
        if (enchantmentOrder.isEmpty()) {
            enchantmentOrder = EnchantmentCategoriesHarness.getRegistryOrder(getEnchantmentRegistry());
        }
        List<Integer> rows = new ArrayList<>();
        for (int row = 0; row < enchantmentOrder.size(); row++) {
            if (getTotalForRow(row) > 0) {
                rows.add(row);
            }
        }
        return rows;
    }

    private Registry<Enchantment> getEnchantmentRegistry() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return null;
        }
        return client.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
    }

    private int getTotalForRow(int rowIndex) {
        int total = 0;
        for (int col = 0; col < RANK_COUNT; col++) {
            total += handler.getGridValue(rowIndex, col);
        }
        return total;
    }

    private int getLevelCount(int rowIndex, int levelIndex) {
        if (levelIndex < 0 || levelIndex >= RANK_COUNT) return 0;
        return handler.getGridValue(rowIndex, levelIndex);
    }

    private boolean isMouseOverList(double mouseX, double mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        int left = x + LIST_X_OFFSET;
        int right = left + LIST_WIDTH;
        int top = y + LIST_Y_OFFSET;
        int bottom = top + LIST_HEIGHT;
        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    private boolean isMouseOverScrollBar(double mouseX, double mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        int barX = x + LIST_X_OFFSET + SCROLL_X_OFFSET;
        int barY = y + LIST_Y_OFFSET;
        int barHeight = LIST_HEIGHT - 1;
        return mouseX >= barX && mouseX < barX + SCROLL_WIDTH && mouseY >= barY && mouseY < barY + barHeight;
    }
    // endregion

    // endregion
}
