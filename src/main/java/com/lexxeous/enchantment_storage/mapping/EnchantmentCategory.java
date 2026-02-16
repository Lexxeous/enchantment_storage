package com.lexxeous.enchantment_storage.mapping;

import net.minecraft.util.Identifier;

public class EnchantmentCategory {
	// region Constants
	public static final int MAX_LEVELS = 5;
	public static final int MAX_COUNT_PER_LEVEL = 99;
	// endregion

	// region Class Variables
	private final Identifier enchantmentId;
	private final byte[] levels;
	// endregion

	// region Constructors
	public EnchantmentCategory(Identifier enchantmentId) {
		this.enchantmentId = enchantmentId;
		this.levels = new byte[MAX_LEVELS];
	}

	public EnchantmentCategory(Identifier enchantmentId, byte[] levels) {
		this.enchantmentId = enchantmentId;
		this.levels = new byte[MAX_LEVELS];

		int limit = Math.min(levels.length, MAX_LEVELS);
		for (int i = 0; i < limit; i++) {
			this.levels[i] = (byte) clampCount(Byte.toUnsignedInt(levels[i]));
		}
	}
	// endregion

	// region Getters & Setters
	public Identifier getEnchantmentId() {
		return enchantmentId;
	}

	public byte[] getLevelsCopy() {
		byte[] copy = new byte[MAX_LEVELS];
		System.arraycopy(levels, 0, copy, 0, MAX_LEVELS);
		return copy;
	}

	public int getCount(int levelIndex) {
		return Byte.toUnsignedInt(levels[validateLevelIndex(levelIndex)]);
	}

	public int getTotalCount() {
		int total = 0;
		for (byte level : levels) {
			total += Byte.toUnsignedInt(level);
		}
		return total;
	}

	public void setCount(int levelIndex, int count) {
		levels[validateLevelIndex(levelIndex)] = (byte) clampCount(count);
	}
	// endregion

	// region Increment & Decrement
	public int increment(int levelIndex, int amount) {
		int index = validateLevelIndex(levelIndex);
		int current = Byte.toUnsignedInt(levels[index]);
		int next = clampCount(current + amount);
		levels[index] = (byte) next;
		return next - current;
	}
	// endregion

	// region Helpers
	public boolean isEmpty() {
		for (byte level : levels) {
			if (level != 0) {
				return false;
			}
		}
		return true;
	}
	// endregion

	// region Validation
	private int validateLevelIndex(int levelIndex) {
		if (levelIndex < 0 || levelIndex >= MAX_LEVELS) {
			throw new IllegalArgumentException("levelIndex out of range: " + levelIndex);
		}
		return levelIndex;
	}

	private int clampCount(int count) {
		if (count < 0) {
			return 0;
		}
		if (count > MAX_COUNT_PER_LEVEL) {
			return MAX_COUNT_PER_LEVEL;
		}
		return count;
	}
	// endregion
}