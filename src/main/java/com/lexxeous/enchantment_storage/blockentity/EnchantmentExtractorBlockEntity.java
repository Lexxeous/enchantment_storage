package com.lexxeous.enchantment_storage.blockentity;

import com.lexxeous.enchantment_storage.EnchantmentStorage;
import com.lexxeous.enchantment_storage.mapping.EnchantmentCategories;
import com.lexxeous.enchantment_storage.mapping.EnchantmentCategoriesHarness;
import com.lexxeous.enchantment_storage.registry.ModBlockEntities;
import com.lexxeous.enchantment_storage.screen.EnchantmentExtractorScreenHandler;
import com.lexxeous.enchantment_storage.util.EnchantmentStorageUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EnchantmentExtractorBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory {
    // region Constant(s)
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_LAPIS = 2;
    public static final int INVENTORY_SIZE = 3;

    // Slot exposure for hopper automation
    private static final int[] SLOTS_TOP = new int[]{SLOT_LAPIS};
    private static final int[] SLOTS_SIDE = new int[]{SLOT_INPUT};
    private static final int[] SLOTS_BOTTOM = new int[]{SLOT_OUTPUT};

    public static final int MODE_STORE = 0;
    public static final int MODE_EXTRACT = 1;

    public static final int GRID_ROWS = 128;
    public static final int GRID_COLS = 5;
    public static final int GRID_SIZE = GRID_ROWS * GRID_COLS;
    // endregion

    // region Class Variable(s)
    private DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int mode = MODE_STORE;
    private int[] enchantmentGrid = new int[GRID_SIZE];
    private final EnchantmentCategories categories = new EnchantmentCategories();
    // endregion

    // region Constructor(s)
    public EnchantmentExtractorBlockEntity(net.minecraft.util.math.BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTMENT_EXTRACTOR, pos, state);
    }
    // endregion

    // region Override(s)
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
        ItemStack previous = items.get(slot);
        items.set(slot, stack);
        // Optional: enforce max count, etc.
        if (stack.getCount() > getMaxCountPerStack()) stack.setCount(getMaxCountPerStack());
        markDirty();

        if (slot == SLOT_INPUT && world != null && !world.isClient()) {
            if (!ItemStack.areItemsAndComponentsEqual(previous, stack)) {
                logStoreableEnchantments(stack);
            }
        }
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

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInv, PlayerEntity player) {
        ScreenHandlerContext context = this.world == null
                ? ScreenHandlerContext.EMPTY
                : ScreenHandlerContext.create(this.world, this.pos);
        return new EnchantmentExtractorScreenHandler(syncId, playerInv, this, context);
    }

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
            if (stack.isOf(Items.BOOK)) {
                return true;
            }

            if (mode == MODE_STORE) {
                return stack.hasEnchantments() || stack.isOf(Items.ENCHANTED_BOOK);
            }

            return false;
        }

        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == SLOT_OUTPUT; // Only output can be extracted (from bottom).
    }

    // region NBT
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
        view.read("EnchantmentCategories", NbtCompound.CODEC)
            .ifPresent(categories::readNbt);
        if (world != null && getEnchantmentRegistry() != null && !categories.getSortedEnchantments().isEmpty()) {
            applyCategoriesToGrid(categories, getRegistryOrder());
        }
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, this.items);
        view.putInt("Mode", this.mode);
        view.putIntArray("EnchantmentGrid", this.enchantmentGrid);
        view.put("EnchantmentCategories", NbtCompound.CODEC, categories.toNbt());
    }
    // endregion
    // endregion

    // region Getter(s) & Setter(s)
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

    public int getGridValueByIndex(int index) {
        if (index < 0 || index >= enchantmentGrid.length) return 0;
        return enchantmentGrid[index];
    }

    public void setGridValueByIndex(int index, int value) {
        if (index < 0 || index >= enchantmentGrid.length) return;
        enchantmentGrid[index] = value;
        markDirty();
    }

    public boolean hasStoredEnchantData() {
        for (int value : enchantmentGrid) {
            if (value > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPersistentExtractorData() {
        if (hasStoredEnchantData()) {
            return true;
        }
        return mode != MODE_STORE || !categories.getSortedEnchantments().isEmpty();
    }

    public void writePersistentDataWithoutInventory(WriteView view) {
        view.putInt("Mode", this.mode);
        view.putIntArray("EnchantmentGrid", this.enchantmentGrid);
        view.put("EnchantmentCategories", NbtCompound.CODEC, categories.toNbt());
    }
    // endregion

    // region Helper(s)

    // region Category Helper(s)
    public void applyDemoCategories() {
        applyCategories(
            EnchantmentCategoriesHarness.buildDemoCategories(getEnchantmentRegistry()),
            getRegistryOrder()
        );
    }

    public void applyCategories(EnchantmentCategories updated, List<Identifier> order) {
        categories.clear();
        categories.readNbt(updated.toNbt());
        applyCategoriesToGrid(categories, order);
        markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    private void applyCategoriesToGrid(EnchantmentCategories source, List<Identifier> order) {
        for (int i = 0; i < enchantmentGrid.length; i++) {
            enchantmentGrid[i] = 0;
        }
        for (int row = 0; row < order.size() && row < GRID_ROWS; row++) {
            Identifier id = order.get(row);
            for (int col = 0; col < GRID_COLS; col++) {
                int count = source.getCount(id, col);
                enchantmentGrid[row * GRID_COLS + col] = count;
            }
        }
    }

    // region Debug
    public void debugAdjustEnchantment(Identifier enchantmentId, int levelIndex, int amount) {
        if (amount == 0) {
            return;
        }

        categories.increment(enchantmentId, levelIndex, amount);
        applyCategoriesToGrid(categories, getRegistryOrder());
        markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    private Registry<Enchantment> getEnchantmentRegistry() {
        if (world == null) {
            return null;
        }
        return world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
    }

    private List<Identifier> getRegistryOrder() {
        Registry<Enchantment> registry = getEnchantmentRegistry();
        if (registry == null) {
            return categories.getSortedEnchantments();
        }
        return EnchantmentCategoriesHarness.getRegistryOrder(registry);
    }

    public Identifier getEnchantmentIdForRow(int rowIndex) {
        if (rowIndex < 0) {
            return null;
        }
        List<Identifier> order = getRegistryOrder();
        if (rowIndex >= order.size()) {
            return null;
        }
        return order.get(rowIndex);
    }

    public String getEnchantmentDisplayName(Identifier id) {
        return formatEnchantmentName(id);
    }

    public net.minecraft.registry.entry.RegistryEntry<Enchantment> getEnchantmentEntry(Identifier id) {
        Registry<Enchantment> registry = getEnchantmentRegistry();
        if (registry == null) {
            return null;
        }
        return registry.getEntry(id).orElse(null);
    }
    // endregion

    // region Logging
    private void logStoreableEnchantments(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.getEnchantments().isEmpty()) {
            return;
        }

        List<EnchantmentEntry> entries = new ArrayList<>();
        for (var entry : enchantments.getEnchantmentEntries()) {
            Identifier id = entry.getKey().getKey()
                .map(net.minecraft.registry.RegistryKey::getValue)
                .orElse(null);
            if (id == null) {
                continue;
            }
            String name = formatEnchantmentName(id);
            entries.add(new EnchantmentEntry(name, entry.getIntValue()));
        }

        if (entries.isEmpty()) {
            return;
        }

        entries.sort(Comparator.comparing(e -> e.name));
        List<String> parts = new ArrayList<>();
        for (EnchantmentEntry entry : entries) {
            parts.add(String.format("1 %s rank %d", entry.name, entry.level));
        }

        EnchantmentStorage.LOGGER.info("Can store {}", joinWithCommasAnd(parts));
    }

    private String formatEnchantmentName(Identifier id) {
        if (id == null) {
            return "";
        }
        String translationKey = "enchantment." + id.getNamespace() + "." + id.getPath();
        String name = Text.translatable(translationKey).getString();
        if (name.equals(translationKey)) {
            return EnchantmentStorageUtils.formatFallbackName(id.getPath());
        }
        return name;
    }

    private String joinWithCommasAnd(List<String> parts) {
        if (parts == null || parts.isEmpty()) {
            return "";
        }
        if (parts.size() == 1) {
            return parts.getFirst();
        }
        if (parts.size() == 2) {
            return parts.get(0) + " & " + parts.get(1);
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.size() - 1; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(parts.get(i));
        }
        builder.append(", & ").append(parts.getLast());
        return builder.toString();
    }

    private record EnchantmentEntry(String name, int level) { }
    // endregion

    // endregion

    // endregion
}
