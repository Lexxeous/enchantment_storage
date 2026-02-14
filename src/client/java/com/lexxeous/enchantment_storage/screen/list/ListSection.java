package com.lexxeous.enchantment_storage.screen.list;

import com.lexxeous.enchantment_storage.mapping.EnchantmentCategoriesHarness;
import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorScreenHandler;
import com.lexxeous.enchantment_storage.util.EnchantmentStorageUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ListSection {
    private static final int RANK_COUNT = 5;
    private static final int SCROLL_X_OFFSET = -5;
    private static final int SCROLL_WIDTH = 3;
    private static final int LIST_X_OFFSET = 10;
    private static final int LIST_Y_OFFSET = 18;
    private static final int LIST_WIDTH = 60;
    private static final int LIST_HEIGHT = 105;
    private static final int LIST_ITEM_HEIGHT = 21;
    private static final float LIST_ITEM_TEXT_SCALE = 0.70f;
    private static final int RANK_ROW_Y_OFFSET = 9;
    private static final int RANK_SQUARE_SIZE = 10;

    private int scrollOffset = 0;
    private int selectedIndex = -1;
    private int selectedLevel = -1;
    private boolean isDraggingScroll = false;
    private List<Identifier> enchantmentOrder = List.of();
    private Registry<Enchantment> registry;
    private final List<Integer> visibleRows = new ArrayList<>();
    private final Map<Identifier, String> enchantmentNameCache = new HashMap<>();
    private int visibleRowsFingerprint = Integer.MIN_VALUE;
    private long lastVisibleRowsWorldTime = Long.MIN_VALUE;

    public void updateRegistryOrder(Registry<Enchantment> registry) {
        if (this.registry == registry) {
            return;
        }

        this.registry = registry;
        if (registry == null) {
            enchantmentOrder = List.of();
        } else {
            enchantmentOrder = EnchantmentCategoriesHarness.getRegistryOrder(registry);
        }

        enchantmentNameCache.clear();
        visibleRows.clear();
        visibleRowsFingerprint = Integer.MIN_VALUE;
        lastVisibleRowsWorldTime = Long.MIN_VALUE;
        selectedIndex = -1;
        selectedLevel = -1;
    }

    public void drawList(
        DrawContext context,
        int x,
        int y,
        int mouseX,
        int mouseY,
        TextRenderer textRenderer,
        EnchantmentExtractorScreenHandler handler
    ) {
        int listX = x + LIST_X_OFFSET;
        int listY = y + LIST_Y_OFFSET;
        List<Integer> rows = getVisibleRows(handler);
        int visible = getVisibleEntries();
        int maxOffset = Math.max(0, rows.size() - visible);
        if (scrollOffset > maxOffset) {
            scrollOffset = maxOffset;
        }
        int end = Math.min(rows.size(), scrollOffset + visible);
        int entryPaddingX = 2;
        int entryTextYOffset = 2;
        int listContentWidth = LIST_WIDTH - (entryPaddingX * 2);

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
            String name = getEnchantmentName(rowIndex);
            float nameScale = calculateNameScale(name);
            drawScaledText(context, textRenderer, name, listX + entryPaddingX, top + entryTextYOffset, nameScale, 0xFF000000);

            int rankStartX = listX + entryPaddingX;
            int rankY = top + RANK_ROW_Y_OFFSET;
            int maxRanks = getMaxRankForRow(rowIndex);
            double rankGap = Math.max(1.0, (listContentWidth - (RANK_COUNT * RANK_SQUARE_SIZE)) / (double) (RANK_COUNT + 1));
            for (int r = 0; r < maxRanks; r++) {
                int rx = rankStartX + (int) Math.round(rankGap + r * (RANK_SQUARE_SIZE + rankGap));
                boolean hasRank = getLevelCount(rowIndex, r, handler) > 0;
                boolean isHovered = mouseX >= rx && mouseX < rx + RANK_SQUARE_SIZE
                    && mouseY >= rankY && mouseY < rankY + RANK_SQUARE_SIZE;
                int outerColor = hasRank ? 0xFF6F6F6F : 0xFF2F2F2F;
                int innerColor = outerColor;
                int textColor = hasRank ? 0xFFFFFFFF : 0xFFA0A0A0;
                context.fill(rx, rankY, rx + RANK_SQUARE_SIZE, rankY + RANK_SQUARE_SIZE, outerColor);
                context.fill(rx + 1, rankY + 1, rx + (RANK_SQUARE_SIZE - 1), rankY + (RANK_SQUARE_SIZE - 1), innerColor);
                int borderColor = 0xFF000000;
                if (hasRank && isHovered) {
                    borderColor = 0xFFFFFFFF;
                }
                if (hasRank && selectedIndex == i && selectedLevel == r) {
                    borderColor = 0xFF3F76E4;
                }
                context.fill(rx, rankY, rx + RANK_SQUARE_SIZE, rankY + 1, borderColor);
                context.fill(rx, rankY + RANK_SQUARE_SIZE - 1, rx + RANK_SQUARE_SIZE, rankY + RANK_SQUARE_SIZE, borderColor);
                context.fill(rx, rankY, rx + 1, rankY + RANK_SQUARE_SIZE, borderColor);
                context.fill(rx + RANK_SQUARE_SIZE - 1, rankY, rx + RANK_SQUARE_SIZE, rankY + RANK_SQUARE_SIZE, borderColor);
                String numeral = getRomanNumeral(r);
                float numeralScale = 0.6f;

                // Center the roman numeral inside its square.
                int textWidth = (int) (textRenderer.getWidth(numeral) * numeralScale);
                int textHeight = (int) (textRenderer.fontHeight * numeralScale);
                int textX = rx + (RANK_SQUARE_SIZE - textWidth) / 2;
                int textY = rankY + (RANK_SQUARE_SIZE - textHeight) / 2 + 1;
                if (r >= 2) textX += 1;
                drawScaledText(context, textRenderer, numeral, textX, textY, numeralScale, textColor);
            }
        }
    }

    public void drawScrollBar(DrawContext context, int x, int y, EnchantmentExtractorScreenHandler handler) {
        int listX = x + LIST_X_OFFSET;
        int listY = y + LIST_Y_OFFSET;
        int barX = listX + SCROLL_X_OFFSET;
        int barY = listY;
        int barHeight = LIST_HEIGHT - 1;
        int maxOffset = Math.max(0, getEntryCount(handler) - getVisibleEntries());

        context.fill(barX, barY, barX + SCROLL_WIDTH, barY + barHeight, 0xFFCCCCCC);
        if (maxOffset == 0) {
            context.fill(barX, barY, barX + SCROLL_WIDTH, barY + barHeight, 0xFF999999);
            return;
        }

        // Thumb size scales with visible entries vs total entries.
        int thumbHeight = Math.max(8, (barHeight * getVisibleEntries()) / Math.max(1, getEntryCount(handler)));
        int thumbY = barY + (barHeight - thumbHeight) * scrollOffset / maxOffset;
        context.fill(barX, thumbY, barX + SCROLL_WIDTH, thumbY + thumbHeight, 0xFF888888);
    }

    public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double verticalAmount,
        int x,
        int y,
        EnchantmentExtractorScreenHandler handler
    ) {
        if (isMouseOverList(mouseX, mouseY, x, y)) {
            int maxOffset = Math.max(0, getEntryCount(handler) - getVisibleEntries());
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset - (int) Math.signum(verticalAmount)));
            return true;
        }
        return false;
    }

    public boolean mouseClicked(
        Click click,
        int x,
        int y,
        EnchantmentExtractorScreenHandler handler,
        MinecraftClient client
    ) {
        if (click.button() == 0 && isMouseOverScrollBar(click.x(), click.y(), x, y)) {
            isDraggingScroll = true;
            updateScrollOffsetFromMouse(click.y(), y, handler);
            return true;
        }
        if (click.button() == 0 && isMouseOverList(click.x(), click.y(), x, y)) {
            int localY = (int) click.y() - (y + LIST_Y_OFFSET);
            int index = scrollOffset + (localY / LIST_ITEM_HEIGHT);
            if (index >= 0 && index < getEntryCount(handler)) {
                List<Integer> rows = getVisibleRows(handler);
                selectedIndex = index;
                int localX = (int) click.x() - (x + LIST_X_OFFSET);
                int rowIndex = rows.get(index);
                selectedLevel = getRankIndexFromLocal(localX, localY, rowIndex);
                if (selectedLevel >= 0 && index < rows.size()) {
                    if (getLevelCount(rowIndex, selectedLevel, handler) > 0) {
                        int gridIndex = (rowIndex * RANK_COUNT) + selectedLevel;
                        handler.setSelectedGridIndex(gridIndex);
                        if (client != null && client.interactionManager != null) {
                            client.interactionManager.clickButton(
                                handler.syncId,
                                EnchantmentExtractorScreenHandler.BUTTON_GRID_BASE + gridIndex
                            );
                        }
                    }
                }
                return true;
            }
        }

        if (click.button() == 0 && !isMouseOverList(click.x(), click.y(), x, y)) {
            clearSelection(handler);
        }

        return false;
    }

    public boolean mouseDragged(Click click, int x, int y, EnchantmentExtractorScreenHandler handler) {
        if (isDraggingScroll) {
            updateScrollOffsetFromMouse(click.y(), y, handler);
            return true;
        }
        return false;
    }

    public void mouseReleased() {
        isDraggingScroll = false;
    }

    public int getEntryCount(EnchantmentExtractorScreenHandler handler) {
        return getVisibleRows(handler).size();
    }

    public String getTotalRemainingText(EnchantmentExtractorScreenHandler handler) {
        if (selectedIndex < 0) return "＿";
        List<Integer> rows = getVisibleRows(handler);
        if (selectedIndex >= rows.size()) return "＿";
        int rowIndex = rows.get(selectedIndex);
        return String.valueOf(getTotalForRow(rowIndex, handler));
    }

    public String getLevelsRemainingText(EnchantmentExtractorScreenHandler handler) {
        if (selectedIndex < 0 || selectedLevel < 0) return "＿";
        List<Integer> rows = getVisibleRows(handler);
        if (selectedIndex >= rows.size()) return "＿";
        int rowIndex = rows.get(selectedIndex);
        return String.valueOf(getLevelCount(rowIndex, selectedLevel, handler));
    }

    public int getSelectedExtractCost(EnchantmentExtractorScreenHandler handler) {
        if (selectedIndex >= 0 && selectedLevel >= 0) {
            List<Integer> rows = getVisibleRows(handler);
            if (selectedIndex >= rows.size()) {
                return -1;
            }

            int rowIndex = rows.get(selectedIndex);
            if (getLevelCount(rowIndex, selectedLevel, handler) <= 0) {
                return -1;
            }

            return selectedLevel + 1;
        }
        return -1;
    }

    public boolean hasSelection() {
        return selectedIndex >= 0 && selectedLevel >= 0;
    }

    public boolean isSelectedEnchantmentOnInput(EnchantmentExtractorScreenHandler handler) {
        Identifier selectedId = getSelectedEnchantmentId(handler);
        if (selectedId == null) {
            return false;
        }
        var input = handler.getInputStack();
        if (input.isEmpty()) {
            return false;
        }
        return EnchantmentStorageUtils.hasEnchantment(input, selectedId);
    }

    public void clearSelection(EnchantmentExtractorScreenHandler handler) {
        selectedIndex = -1;
        selectedLevel = -1;
        handler.clearSelection();
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


    private String getEnchantmentName(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= enchantmentOrder.size()) {
            return "";
        }
        Identifier id = enchantmentOrder.get(rowIndex);
        String cachedName = enchantmentNameCache.get(id);
        if (cachedName != null) {
            return cachedName;
        }

        String translationKey = "enchantment." + id.getNamespace() + "." + id.getPath();
        String name = Text.translatable(translationKey).getString();
        if (name.equals(translationKey)) {
            name = EnchantmentStorageUtils.formatFallbackName(id.getPath());
        }

        enchantmentNameCache.put(id, name);
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

    private int getRankIndexFromLocal(int localX, int localY, int rowIndex) {
        int entryPaddingX = 2;
        int listContentWidth = LIST_WIDTH - (entryPaddingX * 2);
        double rankGap = Math.max(1.0, (listContentWidth - (RANK_COUNT * RANK_SQUARE_SIZE)) / (double) (RANK_COUNT + 1));
        int rowIndexInList = localY / LIST_ITEM_HEIGHT;
        int rowTop = rowIndexInList * LIST_ITEM_HEIGHT;
        int rankTop = rowTop + RANK_ROW_Y_OFFSET;
        if (localY < rankTop || localY > rankTop + RANK_SQUARE_SIZE) return -1;
        int rankStartX = entryPaddingX;
        int maxRanks = getMaxRankForRow(rowIndex);
        for (int r = 0; r < maxRanks; r++) {
            int rx = rankStartX + (int) Math.round(rankGap + r * (RANK_SQUARE_SIZE + rankGap));
            if (localX >= rx && localX <= rx + RANK_SQUARE_SIZE) return r;
        }
        return -1;
    }

    private int getVisibleEntries() {
        return LIST_HEIGHT / LIST_ITEM_HEIGHT;
    }

    private void updateScrollOffsetFromMouse(double mouseY, int y, EnchantmentExtractorScreenHandler handler) {
        int maxOffset = Math.max(0, getEntryCount(handler) - getVisibleEntries());
        if (maxOffset == 0) {
            scrollOffset = 0;
            return;
        }
        int barY = y + LIST_Y_OFFSET;
        int barHeight = LIST_HEIGHT - 1;
        int thumbHeight = Math.max(8, (barHeight * getVisibleEntries()) / Math.max(1, getEntryCount(handler)));
        double track = barHeight - thumbHeight;
        double normalized = (mouseY - barY - (thumbHeight / 2.0)) / track;
        int next = (int) Math.round(normalized * maxOffset);
        scrollOffset = Math.max(0, Math.min(maxOffset, next));
    }

    private List<Integer> getVisibleRows(EnchantmentExtractorScreenHandler handler) {
        MinecraftClient client = MinecraftClient.getInstance();
        long worldTime = (client != null && client.world != null) ? client.world.getTime() : Long.MIN_VALUE;
        if (worldTime == lastVisibleRowsWorldTime) {
            return visibleRows;
        }

        int fingerprint = computeGridFingerprint(handler);
        if (fingerprint == visibleRowsFingerprint) {
            lastVisibleRowsWorldTime = worldTime;
            return visibleRows;
        }

        visibleRows.clear();
        for (int row = 0; row < enchantmentOrder.size(); row++) {
            if (getTotalForRow(row, handler) > 0) {
                visibleRows.add(row);
            }
        }

        visibleRowsFingerprint = fingerprint;

        if (selectedIndex >= visibleRows.size()) {
            selectedIndex = -1;
            selectedLevel = -1;
        }

        lastVisibleRowsWorldTime = worldTime;
        return visibleRows;
    }

    // For caching list value(s)
    private int computeGridFingerprint(EnchantmentExtractorScreenHandler handler) {
        int hash = 1;
        for (int row = 0; row < enchantmentOrder.size(); row++) {
            for (int col = 0; col < RANK_COUNT; col++) {
                hash = (31 * hash) + handler.getGridValue(row, col);
            }
        }
        return hash;
    }

    private int getTotalForRow(int rowIndex, EnchantmentExtractorScreenHandler handler) {
        int total = 0;
        for (int col = 0; col < RANK_COUNT; col++) {
            total += handler.getGridValue(rowIndex, col);
        }
        return total;
    }

    private int getLevelCount(int rowIndex, int levelIndex, EnchantmentExtractorScreenHandler handler) {
        if (levelIndex < 0 || levelIndex >= RANK_COUNT) return 0;
        return handler.getGridValue(rowIndex, levelIndex);
    }

    private int getMaxRankForRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= enchantmentOrder.size()) {
            return RANK_COUNT;
        }

        if (registry == null) {
            return RANK_COUNT;
        }

        Identifier id = enchantmentOrder.get(rowIndex);
        Enchantment enchantment = registry.get(id);
        if (enchantment == null) {
            return RANK_COUNT;
        }

        int max = enchantment.getMaxLevel();
        if (max < 1) {
            return 1;
        }

        return Math.min(RANK_COUNT, max);
    }

    private Identifier getSelectedEnchantmentId(EnchantmentExtractorScreenHandler handler) {
        if (selectedIndex < 0) {
            return null;
        }

        List<Integer> rows = getVisibleRows(handler);
        if (selectedIndex >= rows.size()) {
            return null;
        }

        int rowIndex = rows.get(selectedIndex);
        if (rowIndex < 0 || rowIndex >= enchantmentOrder.size()) {
            return null;
        }

        return enchantmentOrder.get(rowIndex);
    }

    private boolean isMouseOverList(double mouseX, double mouseY, int x, int y) {
        int left = x + LIST_X_OFFSET;
        int right = left + LIST_WIDTH;
        int top = y + LIST_Y_OFFSET;
        int bottom = top + LIST_HEIGHT;
        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    private boolean isMouseOverScrollBar(double mouseX, double mouseY, int x, int y) {
        int barX = x + LIST_X_OFFSET + SCROLL_X_OFFSET;
        int barY = y + LIST_Y_OFFSET;
        int barHeight = LIST_HEIGHT - 1;
        return mouseX >= barX && mouseX < barX + SCROLL_WIDTH && mouseY >= barY && mouseY < barY + barHeight;
    }

    private float calculateNameScale(String name) {
        float nameScale = LIST_ITEM_TEXT_SCALE;

        if (name.length() > 15) nameScale = 0.6f;
        if (name.length() > 17) nameScale = 0.53f;

        return nameScale;
    }
}
