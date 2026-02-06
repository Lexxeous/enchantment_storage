package com.lexxeous.enchantment_storage.blockentity;

import com.lexxeous.enchantment_storage.registry.ModBlockEntities;
import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

public class EnchantmentExtractorBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_LAPIS = 2;
    public static final int INVENTORY_SIZE = 3;

    public static final int MODE_STORE = 0;
    public static final int MODE_EXTRACT = 1;

    public static final int GRID_ROWS = 30;
    public static final int GRID_COLS = 5;
    public static final int GRID_SIZE = GRID_ROWS * GRID_COLS;

    private int progress = 0;
    private int maxProgress = 200; // arbitrary for now

    private DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int mode = MODE_STORE;
    private int[] enchantmentGrid = new int[GRID_SIZE];

    public EnchantmentExtractorBlockEntity(net.minecraft.util.math.BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTMENT_EXTRACTOR, pos, state);
    }

    // -------- Basic inventory methods --------
    @Override
    public int size() {
        return INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(items, slot, amount);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(items, slot);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        // Optional: enforce max count, etc.
        if (stack.getCount() > getMaxCountPerStack()) stack.setCount(getMaxCountPerStack());
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (world == null) return false;
        if (world.getBlockEntity(pos) != this) return false;
        return player.squaredDistanceTo(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5
        ) <= 64.0;
    }

    @Override
    public void clear() {
        items.clear();
        markDirty();
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.enchantment_storage.enchantment_extractor");
    }

    // -------- Handlers --------

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInv, PlayerEntity player) {
        ScreenHandlerContext context = this.world == null
                ? ScreenHandlerContext.EMPTY
                : ScreenHandlerContext.create(this.world, this.pos);
        return new EnchantmentExtractorScreenHandler(syncId, playerInv, this, context);
    }

    // -------- Hopper automation rules --------

    // Which slots are exposed on which side.
    // Basic sane defaults:
    // - Top: input
    // - Sides: item
    // - Bottom: output only
    private static final int[] SLOTS_TOP = new int[]{SLOT_LAPIS};
    private static final int[] SLOTS_SIDE = new int[]{SLOT_INPUT};
    private static final int[] SLOTS_BOTTOM = new int[]{SLOT_OUTPUT};

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) return SLOTS_BOTTOM;
        if (side == Direction.UP) return SLOTS_TOP;
        return SLOTS_SIDE;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        // Don’t let things insert into output slot.
        if (slot == SLOT_OUTPUT) return false;

        if (slot == SLOT_LAPIS) return stack.isOf(Items.LAPIS_LAZULI);

        if (slot == SLOT_INPUT) {
            if (mode == MODE_STORE) {
                return stack.hasEnchantments() || stack.isOf(Items.ENCHANTED_BOOK);
            }
            return stack.isOf(Items.BOOK);
        }

        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == SLOT_OUTPUT; // Only output can be extracted (from bottom).
    }

    // -------- NBT persistence --------
    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        // Re-init to correct size (matches what furnace does)
        this.items = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readData(view, this.items);
        this.mode = view.getInt("Mode", MODE_STORE);
        int[] grid = view.getOptionalIntArray("EnchantmentGrid").orElse(new int[GRID_SIZE]);
        if (grid.length == GRID_SIZE) {
            this.enchantmentGrid = grid;
        } else {
            this.enchantmentGrid = new int[GRID_SIZE];
        }
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, this.items);
        view.putInt("Mode", this.mode);
        view.putIntArray("EnchantmentGrid", this.enchantmentGrid);
    }

    // region Hooks
    public static void tick(
        net.minecraft.world.World world,
        net.minecraft.util.math.BlockPos pos,
        BlockState state,
        EnchantmentExtractorBlockEntity be)
    {
        if (world.isClient()) return;

        if (!be.getStack(SLOT_INPUT).isEmpty()) {
            be.progress++;
            if (be.progress >= be.maxProgress) {
                be.progress = 0;
                // later: do extraction
            }
            be.markDirty();
        } else {
            be.progress = 0;
        }
    }
    // endregion

    // region Getters & Setters
    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        if (this.mode == mode) return;
        this.mode = mode;
        markDirty();
    }

    public int getGridValue(int row, int col) {
        return enchantmentGrid[row * GRID_COLS + col];
    }

    public void setGridValue(int row, int col, int value) {
        enchantmentGrid[row * GRID_COLS + col] = value;
        markDirty();
    }
    // endregion
}
