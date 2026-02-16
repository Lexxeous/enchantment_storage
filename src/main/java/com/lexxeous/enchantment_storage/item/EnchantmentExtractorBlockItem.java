package com.lexxeous.enchantment_storage.item;

import com.lexxeous.enchantment_storage.constant.EnchantmentStorageNbtConstants;
import com.lexxeous.enchantment_storage.util.EnchantmentStorageCategoryUtils;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class EnchantmentExtractorBlockItem extends BlockItem {
	// region Constructors
	public EnchantmentExtractorBlockItem(Block block, Item.Settings settings) {
		super(block, settings);
	}
	// endregion

	// region Helpers
	public static void appendStoredEnchantmentsTooltip(ItemStack stack, List<Text> tooltipLines) {
		TypedEntityData<?> blockEntityData = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
		if (blockEntityData == null) {
			return;
		}

		NbtCompound root = blockEntityData.copyNbtWithoutId();
		NbtCompound categories = root.getCompound(EnchantmentStorageNbtConstants.ENCHANTMENT_CATEGORIES).orElse(null);
		if (categories == null) {
			return;
		}

		List<EnchantmentStorageCategoryUtils.StoredCategoryLine> storedLines = EnchantmentStorageCategoryUtils.getStoredCategoryLines(categories);
		int totalStored = EnchantmentStorageCategoryUtils.getStoredCategoryTotal(storedLines);

		if (totalStored <= 0 || storedLines.isEmpty()) {
			return;
		}

		tooltipLines.add(Text.literal("Total Enchantments Stored: " + totalStored).formatted(Formatting.GRAY));
		for (EnchantmentStorageCategoryUtils.StoredCategoryLine line : storedLines) {
			tooltipLines.add(Text.literal(line.name() + ": " + line.count()).formatted(Formatting.DARK_GRAY));
		}
	}
	// endregion
}
