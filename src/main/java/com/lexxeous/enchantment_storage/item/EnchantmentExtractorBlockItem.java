package com.lexxeous.enchantment_storage.item;

import com.lexxeous.enchantment_storage.util.EnchantmentStorageUtils;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Consumer;

public class EnchantmentExtractorBlockItem extends BlockItem {
    public EnchantmentExtractorBlockItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(
        ItemStack stack,
        Item.TooltipContext context,
        TooltipDisplayComponent displayComponent,
        Consumer<Text> textConsumer,
        TooltipType type
    ) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);

        TypedEntityData<?> blockEntityData = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
        if (blockEntityData == null) {
            return;
        }

        NbtCompound root = blockEntityData.copyNbtWithoutId();
        NbtCompound categories = root.getCompound("EnchantmentCategories").orElse(null);
        if (categories == null) {
            return;
        }

        List<EnchantmentStorageUtils.StoredCategoryLine> lines = EnchantmentStorageUtils.getStoredCategoryLines(categories);
        int totalStored = EnchantmentStorageUtils.getStoredCategoryTotal(lines);

        if (totalStored <= 0 || lines.isEmpty()) {
            return;
        }

        textConsumer.accept(Text.literal("Total Enchantments Stored: " + totalStored).formatted(Formatting.GRAY));
        for (EnchantmentStorageUtils.StoredCategoryLine line : lines) {
            textConsumer.accept(Text.literal(line.name() + ": " + line.count()).formatted(Formatting.DARK_GRAY));
        }
    }
}
