package com.lexxeous.enchantment_storage.screen;

import com.lexxeous.enchantment_storage.blockentity.EnchantmentExtractorBlockEntity;
import com.lexxeous.enchantment_storage.registry.ModBlocks;
import com.lexxeous.enchantment_storage.screen.button.ButtonHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

public class EnchantmentExtractorScreenHandler extends ScreenHandler {
	// region Constants
	public static final int BUTTON_STORE = 0;
	public static final int BUTTON_EXTRACT = 1;
	public static final int BUTTON_GRID_BASE = 1000;

	private static final int MODE_STORE = EnchantmentExtractorBlockEntity.MODE_STORE;
	private static final int MODE_EXTRACT = EnchantmentExtractorBlockEntity.MODE_EXTRACT;
	private static final int GRID_COLS = EnchantmentExtractorBlockEntity.GRID_COLS;
	private static final int GRID_SIZE = EnchantmentExtractorBlockEntity.GRID_SIZE;
	private static final int PROPERTY_COUNT = 1 + GRID_SIZE;
	// endregion

	// region Class Variables
	private final Inventory inventory;
	private final PropertyDelegate properties;
	private final ScreenHandlerContext context;
	private final @Nullable EnchantmentExtractorBlockEntity blockEntity;
	private final ButtonHandler actionHandler;
	private int selectedGridIndex = -1;
	// endregion

	// region Constructors
	public EnchantmentExtractorScreenHandler(int syncId, PlayerInventory playerInv) {
		this(
			syncId,
			playerInv,
			new SimpleInventory(EnchantmentExtractorBlockEntity.INVENTORY_SIZE),
			new ArrayPropertyDelegate(PROPERTY_COUNT),
			ScreenHandlerContext.EMPTY,
			null
		);
	}

	public EnchantmentExtractorScreenHandler(
		int syncId,
		PlayerInventory playerInv,
		EnchantmentExtractorBlockEntity blockEntity,
		ScreenHandlerContext context
	) {
		this(syncId, playerInv, blockEntity, new ModePropertyDelegate(blockEntity), context, blockEntity);
	}

	private EnchantmentExtractorScreenHandler(
		int syncId,
		PlayerInventory playerInv,
		Inventory inventory,
		PropertyDelegate properties,
		ScreenHandlerContext context,
		@Nullable EnchantmentExtractorBlockEntity blockEntity
	) {
		super(com.lexxeous.enchantment_storage.registry.ModScreenHandlers.ENCHANTMENT_EXTRACTOR, syncId);

		checkSize(inventory, EnchantmentExtractorBlockEntity.INVENTORY_SIZE);
		checkDataCount(properties, PROPERTY_COUNT);

		this.inventory = inventory;
		this.properties = properties;
		this.context = context;
		this.blockEntity = blockEntity;
		this.actionHandler = new ButtonHandler(this, inventory, blockEntity);

		inventory.onOpen(playerInv.player);
		addMachineSlots(inventory);
		addPlayerInventorySlots(playerInv);

		this.addProperties(this.properties);
	}
	// endregion

	// region Getters & Setters
	private void setMode(int mode) {
		this.properties.set(0, mode);
	}

	public int getGridValue(int row, int col) {
		int index = 1 + (row * GRID_COLS) + col;
		if (!isValidPropertyIndex(index)) {
			return 0;
		}
		return this.properties.get(index);
	}

	public void setSelectedGridIndex(int gridIndex) {
		this.selectedGridIndex = gridIndex;
	}

	public int getSelectedGridIndex() {
		return selectedGridIndex;
	}

	public void clearSelection() {
		this.selectedGridIndex = -1;
	}

	public @Nullable EnchantmentExtractorBlockEntity getBlockEntity() {
		return blockEntity;
	}

	public ItemStack getInputStack() {
		return inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT);
	}

	public ItemStack getLapisStack() {
		return inventory.getStack(EnchantmentExtractorBlockEntity.SLOT_LAPIS);
	}
	// endregion

	// region UI
	public boolean canStore() {
		return actionHandler.canStore();
	}

	public boolean canExtract() {
		return actionHandler.canExtract();
	}

	private boolean handleGridClick(PlayerEntity player, int gridIndex) {
		return actionHandler.handleGridClick(player, gridIndex);
	}

	private boolean handleStore(PlayerEntity player) {
		return actionHandler.handleStore(player);
	}

	private boolean handleExtract(PlayerEntity player) {
		return actionHandler.handleExtract(player);
	}
	// endregion

	// region Overrides
	@Override
	public boolean canUse(PlayerEntity player) {
		return canUse(this.context, player, ModBlocks.ENCHANTMENT_EXTRACTOR_BLOCK);
	}

	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		if (id == BUTTON_STORE) {
			setMode(MODE_STORE);
			if (!handleStore(player)) return false;
			clearSelection();
			return true;
		}

		if (id == BUTTON_EXTRACT) {
			setMode(MODE_EXTRACT);
			if (!handleExtract(player)) return false;
			clearSelection();
			return true;
		}

		if (id >= BUTTON_GRID_BASE) {
			return handleGridClick(player, id - BUTTON_GRID_BASE);
		}

		return super.onButtonClick(player, id);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasStack()) return ItemStack.EMPTY;

		ItemStack original = slot.getStack();
		newStack = original.copy();

		// Split between machine slots and player inventory slots.
		int machineSlotCount = EnchantmentExtractorBlockEntity.INVENTORY_SIZE;
		int playerInvStart = machineSlotCount;
		int playerInvEnd = playerInvStart + 36;

		// Shift-click move
		if (index < machineSlotCount) {
			if (!this.insertItem(original, playerInvStart, playerInvEnd, true)) {
				return ItemStack.EMPTY;
			}
		} else {
			if (!this.insertItem(original, 0, machineSlotCount, false)) {
				return ItemStack.EMPTY;
			}
		}

		if (original.isEmpty()) slot.setStack(ItemStack.EMPTY);
		else slot.markDirty();

		return newStack;
	}
	// endregion

	// region Private
	private void addMachineSlots(Inventory inventory) {
		int machineInputX = 76;
		int lapisInputY = 18;
		int itemInputY = 38;
		int outputSlotX = 130;
		int outputSlotY = 28;

		this.addSlot(new Slot(inventory, EnchantmentExtractorBlockEntity.SLOT_LAPIS, machineInputX, lapisInputY) {
			@Override
			public boolean canInsert(ItemStack stack) {
				return stack.isOf(Items.LAPIS_LAZULI);
			}
		});

		this.addSlot(new Slot(inventory, EnchantmentExtractorBlockEntity.SLOT_INPUT, machineInputX, itemInputY) {
			@Override
			public boolean canInsert(ItemStack stack) {
				return isValidMachineInput(stack);
			}
		});

		this.addSlot(new Slot(inventory, EnchantmentExtractorBlockEntity.SLOT_OUTPUT, outputSlotX, outputSlotY) {
			@Override
			public boolean canInsert(ItemStack stack) {
				return false;
			}

			@Override
			public int getMaxItemCount(ItemStack stack) {
				return stack.isOf(Items.BOOK) ? stack.getMaxCount() : 1;
			}
		});
	}

	private void addPlayerInventorySlots(PlayerInventory playerInv) {
		int playerInvStartX = 8;
		int playerInvStartY = 140;
		int playerInvRowSpacing = 18;
		int playerInvColSpacing = 18;

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(
					playerInv,
					col + row * 9 + 9,
					playerInvStartX + col * playerInvColSpacing,
					playerInvStartY + row * playerInvRowSpacing
				));
			}
		}

		// Hotbar sits just below the 3 inventory rows.
		int hotbarY = playerInvStartY + (playerInvRowSpacing * 3) + 4;
		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(playerInv, col, playerInvStartX + col * playerInvColSpacing, hotbarY));
		}
	}

	private boolean isValidMachineInput(ItemStack stack) {
		return stack.isOf(Items.BOOK)
			|| stack.isOf(Items.ENCHANTED_BOOK)
			|| stack.isEnchantable()
			|| stack.hasEnchantments();
	}

	// Property delegate mirrors mode + grid values to keep client UI in sync.
	private record ModePropertyDelegate(EnchantmentExtractorBlockEntity blockEntity) implements PropertyDelegate {

		@Override
		public int get(int index) {
			if (index == 0) {
				return blockEntity.getMode();
			}
			int gridIndex = index - 1;
			return blockEntity.getGridValueByIndex(gridIndex);
		}

		@Override
		public void set(int index, int value) {
			if (index == 0) {
				blockEntity.setMode(value);
				return;
			}
			int gridIndex = index - 1;
			blockEntity.setGridValueByIndex(gridIndex, value);
		}

		@Override
		public int size() {
			return PROPERTY_COUNT;
		}
	}

	private boolean isValidPropertyIndex(int index) {
		return index >= 1 && index < this.properties.size();
	}
	// endregion
}
