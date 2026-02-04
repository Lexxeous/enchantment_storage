package com.lexxeous.enchantment_storage;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

public class EnchantmentExtractorBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory {
    // ----- Slot mapping -----
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_BOOK = 2;
    public static final int INVENTORY_SIZE = 3;

    private DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

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
        return new EnchantmentExtractorScreenHandler(syncId, playerInv, this);
    }

    // -------- Hopper automation rules --------

    // Which slots are exposed on which side.
    // Basic sane defaults:
    // - Top: input
    // - Sides: item
    // - Bottom: output only
    private static final int[] SLOTS_TOP = new int[]{SLOT_INPUT};
    private static final int[] SLOTS_SIDE = new int[]{SLOT_BOOK};
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

        // You can tighten this later:
        // e.g. require enchanted items in SLOT_INPUT, plain book in SLOT_BOOK, etc.
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        // Typically only output can be extracted (especially from bottom).
        return slot == SLOT_OUTPUT;
    }

    // -------- NBT persistence --------
    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        // Re-init to correct size (matches what furnace does)
        this.items = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readData(view, this.items);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, this.items);
    }

    // -------- Tick hook --------
    public static void tick(net.minecraft.world.World world,
                            net.minecraft.util.math.BlockPos pos,
                            BlockState state,
                            EnchantmentExtractorBlockEntity be) {

        if (world.isClient()) return;

        // Later: progress, extraction logic, etc.
        // For now: do nothing.
    }
}
