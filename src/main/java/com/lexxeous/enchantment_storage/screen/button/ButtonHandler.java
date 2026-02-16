package com.lexxeous.enchantment_storage.screen.button;

import com.lexxeous.enchantment_storage.EnchantmentStorage;
import com.lexxeous.enchantment_storage.blockentity.EnchantmentExtractorBlockEntity;
import com.lexxeous.enchantment_storage.logic.EnchantmentStorageActionLogic;
import com.lexxeous.enchantment_storage.mapping.EnchantmentCategory;
import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorScreenHandler;
import com.lexxeous.enchantment_storage.util.EnchantmentStorageExperienceUtils;
import com.lexxeous.enchantment_storage.util.EnchantmentStorageUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class ButtonHandler {
	// region Constants
	private static final int GRID_COLS = EnchantmentExtractorBlockEntity.GRID_COLS;
	// endregion

	// region Class Variables
	private final EnchantmentExtractorScreenHandler handler;
	private final Inventory inventory;
	private final EnchantmentExtractorBlockEntity blockEntity;
	// endregion

	// region Constructors
	public ButtonHandler(
		EnchantmentExtractorScreenHandler handler,
		Inventory inventory,
		EnchantmentExtractorBlockEntity blockEntity
	) {
		this.handler = handler;
		this.inventory = inventory;
		this.blockEntity = blockEntity;
	}
	// endregion

	// region Helpers
	public boolean canStore() {
		ItemStack stack = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT);
		if (!EnchantmentStorageActionLogic.canStore(
			stack.isEmpty(),
			stack.hasEnchantments(),
			stack.isOf(Items.ENCHANTED_BOOK)
		)) {
			return false;
		}

		ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
		Set<Identifier> storedIds = computeStorableEnchantmentIds(enchantments);
		if (storedIds.isEmpty()) {
			return false;
		}

		ItemStack previewOutput = buildStoreOutput(stack, storedIds);
		return canOutputAccept(previewOutput);
	}

	public boolean canExtract() {
		int selectedGridIndex = handler.getSelectedGridIndex();
		int row = selectedGridIndex / GRID_COLS;
		int col = selectedGridIndex % GRID_COLS;
		int selectedCount = handler.getGridValue(row, col);
		if (!EnchantmentStorageActionLogic.canExtractSelection(isOutputEmpty(), selectedGridIndex, selectedCount)) {
			return false;
		}

		ItemStack stack = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT);
		boolean selectedEnchantmentOnInput = false;
		if (blockEntity != null) {
			var selectedId = blockEntity.getEnchantmentIdForRow(row);
			selectedEnchantmentOnInput = EnchantmentStorageUtils.hasEnchantment(stack, selectedId);
		}

		return EnchantmentStorageActionLogic.canExtractInput(
			stack.isEmpty(),
			stack.isOf(Items.BOOK),
			stack.isOf(Items.ENCHANTED_BOOK),
			selectedEnchantmentOnInput
		);
	}

	public boolean handleGridClick(PlayerEntity player, int gridIndex) {
		if (blockEntity == null || player.getEntityWorld().isClient()) {
			return false;
		}

		handler.setSelectedGridIndex(gridIndex);
		int row = gridIndex / GRID_COLS;
		int col = gridIndex % GRID_COLS;
		if (row < 0 || col < 0 || row >= EnchantmentExtractorBlockEntity.GRID_ROWS) {
			return false;
		}

		var enchantmentId = blockEntity.getEnchantmentIdForRow(row);
		if (enchantmentId == null) {
			return false;
		}

		int amount = blockEntity.getGridValue(row, col);
		int rank = col + 1;
		String name = blockEntity.getEnchantmentDisplayName(enchantmentId);
		EnchantmentStorage.logDebugDev("Can extract {} {} rank {}", amount, name, rank);
		return true;
	}

	public boolean handleStore(PlayerEntity player) {
		if (blockEntity == null || player.getEntityWorld().isClient()) {
			return false;
		}
		if (!canStore()) {
			return false;
		}

		ItemStack input = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT);
		if (input.isEmpty()) {
			return false;
		}

		ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(input);
		if (enchantments.getEnchantments().isEmpty()) {
			return false;
		}

		int baseCost = EnchantmentStorageUtils.getEnchantmentLevelTotal(input);
		ItemStack lapis = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_LAPIS);
		int lapisUsed = EnchantmentStorageExperienceUtils.getLapisDiscountUsed(baseCost, lapis);
		int experienceCost = EnchantmentStorageExperienceUtils.getDiscountedCost(baseCost, lapis);
		if (!EnchantmentStorageExperienceUtils.canAffordExperience(player, experienceCost)) {
			return false;
		}

		List<StorableEnchantment> storableEnchantments = computeStorableEnchantments(enchantments);
		Set<Identifier> storedIds = new HashSet<>();
		for (StorableEnchantment storable : storableEnchantments) {
			storedIds.add(storable.id());
			blockEntity.debugAdjustEnchantment(storable.id(), storable.level() - 1, 1);
		}

		if (storedIds.isEmpty()) {
			return false;
		}

		ItemStack output = buildStoreOutput(input, storedIds);
		if (!canOutputAccept(output)) {
			return false;
		}

		addToOutput(output);
		decrementInputStack(input);
		consumeLapis(lapis, lapisUsed);
		chargeExperience(player, experienceCost);
		return true;
	}

	public boolean handleExtract(PlayerEntity player) {
		if (blockEntity == null || player.getEntityWorld().isClient()) {
			return false;
		}
		if (!canExtract()) {
			return false;
		}
		int selectedGridIndex = handler.getSelectedGridIndex();
		if (selectedGridIndex < 0) {
			return false;
		}

		int row = selectedGridIndex / GRID_COLS;
		int col = selectedGridIndex % GRID_COLS;
		int amount = handler.getGridValue(row, col);
		if (amount <= 0) {
			return false;
		}

		ItemStack input = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT);
		if (input.isEmpty()) {
			return false;
		}

		var enchantmentId = blockEntity.getEnchantmentIdForRow(row);
		if (enchantmentId == null) {
			return false;
		}
		var enchantmentEntry = blockEntity.getEnchantmentEntry(enchantmentId);
		if (enchantmentEntry == null) {
			return false;
		}

		int rank = col + 1;
		ItemStack lapis = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_LAPIS);
		int lapisUsed = EnchantmentStorageExperienceUtils.getLapisDiscountUsed(rank, lapis);
		int experienceCost = EnchantmentStorageExperienceUtils.getDiscountedCost(rank, lapis);
		if (!EnchantmentStorageExperienceUtils.canAffordExperience(player, experienceCost)) {
			return false;
		}
		ItemStack output = input.isOf(Items.ENCHANTED_BOOK)
			? input.copyWithCount(1)
			: input.copyComponentsToNewStack(Items.ENCHANTED_BOOK, 1);
		EnchantmentHelper.apply(output, builder -> builder.set(enchantmentEntry, rank));
		if (!canOutputAccept(output)) {
			return false;
		}

		addToOutput(output);
		blockEntity.debugAdjustEnchantment(enchantmentId, col, -1);
		decrementInputStack(input);
		consumeLapis(lapis, lapisUsed);
		chargeExperience(player, experienceCost);
		return true;
	}
	// endregion

	// region Private
	private void decrementInputStack(ItemStack input) {
		if (input.getCount() > 1) {
			input.decrement(1);
			inventory.setStack(EnchantmentExtractorBlockEntity.SLOT_INPUT, input);
		} else {
			inventory.setStack(EnchantmentExtractorBlockEntity.SLOT_INPUT, ItemStack.EMPTY);
		}
	}

	private boolean isOutputEmpty() {
		return inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_OUTPUT).isEmpty();
	}

	// region Compute
	private Set<Identifier> computeStorableEnchantmentIds(ItemEnchantmentsComponent enchantments) {
		Set<Identifier> storedIds = new HashSet<>();
		for (StorableEnchantment storable : computeStorableEnchantments(enchantments)) {
			storedIds.add(storable.id());
		}
		return storedIds;
	}

	private List<StorableEnchantment> computeStorableEnchantments(ItemEnchantmentsComponent enchantments) {
		List<StorableEnchantment> storableEnchantments = new ArrayList<>();
		for (var entry : enchantments.getEnchantmentEntries()) {
			int level = entry.getIntValue();
			if (level < 1 || level > EnchantmentCategory.MAX_LEVELS) {
				continue;
			}
			Identifier id = entry.getKey().getKey()
				.map(RegistryKey::getValue)
				.orElse(null);
			if (id == null) {
				continue;
			}
			storableEnchantments.add(new StorableEnchantment(id, level));
		}
		return storableEnchantments;
	}
	// endregion

	private ItemStack buildStoreOutput(ItemStack input, Set<Identifier> storedIds) {
		ItemStack output = input.copyWithCount(1);
		EnchantmentHelper.apply(output, builder ->
			builder.remove(entry -> entry.getKey()
				.map(RegistryKey::getValue)
				.filter(storedIds::contains)
				.isPresent())
		);

		if (input.isOf(Items.ENCHANTED_BOOK) && EnchantmentHelper.getEnchantments(output).isEmpty()) {
			output = output.copyComponentsToNewStack(Items.BOOK, 1);
		}
		return output;
	}

	private boolean canOutputAccept(ItemStack nextOutput) {
		ItemStack output = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_OUTPUT);
		if (output.isEmpty()) {
			return true;
		}

		if (!output.isOf(Items.BOOK) || !nextOutput.isOf(Items.BOOK)) {
			return false;
		}

		if (!ItemStack.areItemsAndComponentsEqual(output, nextOutput)) {
			return false;
		}

		return output.getCount() + nextOutput.getCount() <= output.getMaxCount();
	}

	private void addToOutput(ItemStack nextOutput) {
		ItemStack output = inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_OUTPUT);
		if (output.isEmpty()) {
			inventory.setStack(EnchantmentExtractorBlockEntity.SLOT_OUTPUT, nextOutput);
			return;
		}

		if (output.isOf(Items.BOOK) && nextOutput.isOf(Items.BOOK) && ItemStack.areItemsAndComponentsEqual(output, nextOutput)) {
			output.setCount(Math.min(output.getMaxCount(), output.getCount() + nextOutput.getCount()));
			inventory.setStack(EnchantmentExtractorBlockEntity.SLOT_OUTPUT, output);
			return;
		}

		inventory.setStack(EnchantmentExtractorBlockEntity.SLOT_OUTPUT, nextOutput);
	}

	private void consumeLapis(ItemStack lapis, int lapisUsed) {
		if (lapisUsed <= 0 || lapis.isEmpty()) {
			return;
		}
		if (lapis.getCount() > lapisUsed) {
			lapis.decrement(lapisUsed);
		} else {
			lapis = ItemStack.EMPTY;
		}
		inventory.setStack(EnchantmentExtractorBlockEntity.SLOT_LAPIS, lapis);
	}

	private void chargeExperience(PlayerEntity player, int experienceCost) {
		if (experienceCost <= 0 || player.isCreative()) {
			return;
		}
		player.addExperienceLevels(-experienceCost);
	}

	private record StorableEnchantment(Identifier id, int level) {}
	// endregion
}
