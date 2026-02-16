# NBT Schema

## Purpose
This document tracks the persisted NBT format used by Enchantment Storage so future schema changes can be planned safely.

## Current schema (v1)
Status: active

### Block Entity root payload
Written/read by:
- `src/main/java/com/lexxeous/enchantment_storage/blockentity/EnchantmentExtractorBlockEntity.java`

Keys:
1. `Mode` (`int`)
- `0` = store
- `1` = extract

2. `EnchantmentGrid` (`int[]`)
- Fixed size expected by code: `GRID_ROWS * GRID_COLS` (currently `128 * 5 = 640`)
- If array length mismatches, runtime falls back to a zeroed grid.

3. `EnchantmentCategories` (`NbtCompound`)
- Serialized by `EnchantmentCategories.toNbt()`
- Read by `EnchantmentCategories.readNbt()`

### EnchantmentCategories payload
Written/read by:
- `src/main/java/com/lexxeous/enchantment_storage/mapping/EnchantmentCategories.java`

Keys:
1. `categories` (`NbtList`)
- Entry shape:
  - `id` (`String`) enchantment id (example: `minecraft:sharpness`)
  - `levels` (`byte[]`) per-rank counts, max 5 entries used

2. `total` (`int`)
- Aggregate stored count across all categories.

Notes:
- Invalid ids/empty levels are skipped during read.
- Per-level values are clamped in `EnchantmentCategory`.

## Reserved keys
1. `DataVersion` (`int`)
- Reserved constant exists in `EnchantmentStorageNbtConstants`.
- Not persisted yet in v1.

## Future migration note (v2 candidate)
Status: design-only (not implemented)

### Proposed root payload (v2)
Keys:
1. `DataVersion` (`int`)
- Value: `2`

2. `Mode` (`int`)
- Proposed values:
  - `10` = store
  - `20` = extract

3. `EnchantmentCategories` (`NbtCompound`)
- Canonical source of stored data.

Removed from persisted payload:
1. `EnchantmentGrid` (`int[]`)
- Runtime-only derived cache built from categories during load.

### Proposed category entry payload (v2)
Path: `EnchantmentCategories.categories[i]`

Keys:
1. `id` (`String`)
2. `levels` (`byte[]`)
3. `meta` (`NbtCompound`)
- `weight` (`int`) default `100`
- `decayTicks` (`int`) default `0`
- `sourceFlags` (`int`) default `0`
- `lastTouchedTick` (`long`) default `0L`

### Proposed constants and variable names
File: `src/main/java/com/lexxeous/enchantment_storage/constant/EnchantmentStorageNbtConstants.java`

```java
public static final String DATA_VERSION = "DataVersion";
public static final int CURRENT_DATA_VERSION = 2;

public static final String MODE = "Mode";
public static final int MODE_STORE_V2 = 10;
public static final int MODE_EXTRACT_V2 = 20;

public static final String ENCHANTMENT_CATEGORIES = "EnchantmentCategories";
public static final String CATEGORIES_LIST = "categories";
public static final String CATEGORY_ID = "id";
public static final String CATEGORY_LEVELS = "levels";
public static final String CATEGORY_META = "meta";

public static final String META_WEIGHT = "weight";
public static final String META_DECAY_TICKS = "decayTicks";
public static final String META_SOURCE_FLAGS = "sourceFlags";
public static final String META_LAST_TOUCHED_TICK = "lastTouchedTick";
```

Suggested runtime variable names:
1. `int dataVersion`
2. `int rawMode`
3. `int migratedMode`
4. `int categoryListLimit`
5. `NbtCompound metadataDefaults`

### Read/write shape examples
File: `src/main/java/com/lexxeous/enchantment_storage/blockentity/EnchantmentExtractorBlockEntity.java`

```java
// writeData (v2)
view.putInt(EnchantmentStorageNbtConstants.DATA_VERSION, EnchantmentStorageNbtConstants.CURRENT_DATA_VERSION);
view.putInt(EnchantmentStorageNbtConstants.MODE, this.mode);
view.put(EnchantmentStorageNbtConstants.ENCHANTMENT_CATEGORIES, NbtCompound.CODEC, categories.toNbtV2());
```

```java
// readData (v2-aware)
int dataVersion = view.getInt(EnchantmentStorageNbtConstants.DATA_VERSION, 1);
if (dataVersion <= 1) {
	migrateV1ToV2(view);
} else {
	readV2(view);
}
rebuildGridFromCategories(); // no persisted EnchantmentGrid in v2
```

### Migration behavior (`v1 -> v2`)
File touchpoints:
1. `src/main/java/com/lexxeous/enchantment_storage/blockentity/EnchantmentExtractorBlockEntity.java`
2. `src/main/java/com/lexxeous/enchantment_storage/mapping/EnchantmentCategories.java`
3. `src/main/java/com/lexxeous/enchantment_storage/item/EnchantmentExtractorBlockItem.java`
4. `src/main/java/com/lexxeous/enchantment_storage/util/EnchantmentStorageCategoryUtils.java`

Expected mapping rules:
1. If `DataVersion` missing, treat as `1`.
2. Convert v1 mode values:
- `0 -> 10`
- `1 -> 20`
- any other value -> `10` (safe default)
3. Ignore persisted `EnchantmentGrid`.
4. Read v1 categories entries and inject default `meta`.
5. Save back using v2 schema on next write.

### Bounds and recovery rules (v2 hardening)
1. Cap category list length with `categoryListLimit` (example: `1024`).
2. Clamp `levels` byte array to `EnchantmentCategory.MAX_LEVELS`.
3. Clamp metadata:
- `weight` to `[0, 10000]`
- `decayTicks` to `[0, Integer.MAX_VALUE]`
4. Skip malformed entries instead of failing entire payload.
5. If all entries are invalid, recover to empty categories and continue.
