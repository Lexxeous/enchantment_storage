package com.lexxeous.enchantment_storage.logic;

public final class EnchantmentStorageActionLogic {
	// region Constructors
	private EnchantmentStorageActionLogic() {}
	// endregion

	// region Helpers
	public static int getDiscountedCost(int baseCost, int lapisCount) {
		if (baseCost <= 0) {
			return 0;
		}
		return Math.max(0, baseCost - Math.max(0, lapisCount));
	}

	public static boolean canAffordExperience(boolean creativeMode, int experienceLevel, int experienceCost) {
		if (experienceCost <= 0) {
			return true;
		}
		return creativeMode || experienceLevel >= experienceCost;
	}

	public static boolean isStoreButtonActive(
		boolean canStore,
		int storeBaseCost,
		int lapisCount,
		boolean creativeMode,
		int experienceLevel
	) {
		if (!canStore) {
			return false;
		}

		int discountedCost = getDiscountedCost(storeBaseCost, lapisCount);
		return canAffordExperience(creativeMode, experienceLevel, discountedCost);
	}

	public static boolean isExtractButtonActive(
		boolean canExtract,
		boolean selectedEnchantmentOnInput,
		int selectedExtractBaseCost,
		int lapisCount,
		boolean creativeMode,
		int experienceLevel
	) {
		if (!canExtract || selectedEnchantmentOnInput) {
			return false;
		}

		int discountedCost = getDiscountedCost(selectedExtractBaseCost, lapisCount);
		return canAffordExperience(creativeMode, experienceLevel, discountedCost);
	}

	public static boolean canStore(
		boolean inputEmpty,
		boolean inputHasEnchantments,
		boolean inputIsEnchantedBook
	) {
		if (inputEmpty) {
			return false;
		}
		return inputHasEnchantments || inputIsEnchantedBook;
	}

	public static boolean canExtractSelection(boolean outputEmpty, int selectedGridIndex, int selectedCount) {
		if (!outputEmpty || selectedGridIndex < 0) {
			return false;
		}
		return selectedCount > 0;
	}

	public static boolean canExtractInput(
		boolean inputEmpty,
		boolean inputIsBook,
		boolean inputIsEnchantedBook,
		boolean selectedEnchantmentAlreadyOnInput
	) {
		if (inputEmpty) {
			return false;
		}
		if (!inputIsBook && !inputIsEnchantedBook) {
			return false;
		}
		return !selectedEnchantmentAlreadyOnInput;
	}
	// endregion
}