package com.lexxeous.enchantment_storage.debug;

import com.lexxeous.enchantment_storage.EnchantmentStorage;
import com.lexxeous.enchantment_storage.blockentity.EnchantmentExtractorBlockEntity;
import com.lexxeous.enchantment_storage.mapping.EnchantmentCategory;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class DebugCommands {
	// region Constants
	private static final double RAYCAST_DISTANCE = 5.0;
	private static final float RAYCAST_TICK_DELTA = 0.0f;
	private static final int DEFAULT_SEED_ROWS = 4;
	private static final int DEFAULT_SEED_BASE_COUNT = 1;
	private static final int MAX_LAPIS_STACK = 64;
	// endregion

	// region Constructors
	private DebugCommands() {}
	// endregion

	// region Registration & Initialization
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			dispatcher.register(CommandManager.literal("es")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("seed").executes(ctx ->
					seedExtractor(ctx, DEFAULT_SEED_ROWS, DEFAULT_SEED_BASE_COUNT)
				).then(CommandManager.argument("rows", IntegerArgumentType.integer(1, EnchantmentExtractorBlockEntity.GRID_ROWS))
					.executes(ctx ->
						seedExtractor(ctx, IntegerArgumentType.getInteger(ctx, "rows"), DEFAULT_SEED_BASE_COUNT)
					).then(CommandManager.argument(
						"baseCount",
						IntegerArgumentType.integer(1, EnchantmentCategory.MAX_COUNT_PER_LEVEL)
					).executes(ctx ->
						seedExtractor(
							ctx,
							IntegerArgumentType.getInteger(ctx, "rows"),
							IntegerArgumentType.getInteger(ctx, "baseCount")
						)
					))
				))
				.then(CommandManager.literal("list").executes(DebugCommands::listStoredEnchantments))
				.then(CommandManager.literal("clear").executes(DebugCommands::clearStoredEnchantments))
				.then(CommandManager.literal("inspect").executes(DebugCommands::inspectInventory))
				.then(CommandManager.literal("fillLapis").then(CommandManager.argument(
					"count",
					IntegerArgumentType.integer(0, MAX_LAPIS_STACK)
				).executes(DebugCommands::fillLapis)))
				.then(CommandManager.literal("store")
					.then(CommandManager.argument(
						"enchantment",
						RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)
					)
						.then(CommandManager.argument("rank", IntegerArgumentType.integer(1, EnchantmentCategory.MAX_LEVELS))
							.then(CommandManager.argument("quantity", IntegerArgumentType.integer(1))
								.executes(ctx -> adjustEnchantmentCount(ctx, true))
							)
						)
					)
				)
				.then(CommandManager.literal("extract")
					.then(CommandManager.argument(
						"enchantment",
						RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)
					)
						.then(CommandManager.argument("rank", IntegerArgumentType.integer(1, EnchantmentCategory.MAX_LEVELS))
							.then(CommandManager.argument("quantity", IntegerArgumentType.integer(1))
								.executes(ctx -> adjustEnchantmentCount(ctx, false))
							)
						)
					)
				)
			)
		);
	}
	// endregion

	// region Debug
	private static int seedExtractor(CommandContext<ServerCommandSource> ctx, int rows, int baseCount) {
		EnchantmentStorage.logDebugDev("es seed rows={} baseCount={}", rows, baseCount);
		return withTargetedExtractor(ctx, (source, extractor) -> {
			extractor.applySeedCategories(rows, baseCount);
			return String.format("Seeded %d row(s) with base count %d.", rows, baseCount);
		});
	}

	private static int listStoredEnchantments(CommandContext<ServerCommandSource> ctx) {
		EnchantmentStorage.logDebugDev("es list");
		return withTargetedExtractor(ctx, (source, extractor) -> {
			var lines = extractor.debugGetStoredEnchantmentLines();
			if (lines.isEmpty()) {
				source.sendFeedback(() -> Text.literal("No stored enchantments in targeted extractor."), false);
			} else {
				source.sendFeedback(() -> Text.literal("Stored enchantments:"), false);
				for (var line : lines) {
					String text = String.format("%s (rank %d): %d", line.name(), line.rank(), line.count());
					source.sendFeedback(() -> Text.literal("- " + text), false);
				}
			}

			int totalFinal = extractor.debugGetStoredTotalCount();
			source.sendFeedback(() -> Text.literal("Total stored count: " + totalFinal), false);
			return "";
		});
	}

	private static int clearStoredEnchantments(CommandContext<ServerCommandSource> ctx) {
		EnchantmentStorage.logDebugDev("es clear");
		return withTargetedExtractor(ctx, (source, extractor) -> {
			extractor.debugClearStoredEnchantData();
			return "Cleared all stored enchant data from targeted extractor.";
		});
	}

	private static int inspectInventory(CommandContext<ServerCommandSource> ctx) {
		EnchantmentStorage.logDebugDev("es inspect");
		return withTargetedExtractor(ctx, (source, extractor) -> {
			source.sendFeedback(() -> Text.literal("Targeted extractor inventory:"), false);
			source.sendFeedback(() -> Text.literal("input: " + describeStack(extractor.getStack(EnchantmentExtractorBlockEntity.SLOT_INPUT))), false);
			source.sendFeedback(() -> Text.literal("output: " + describeStack(extractor.getStack(EnchantmentExtractorBlockEntity.SLOT_OUTPUT))), false);
			source.sendFeedback(() -> Text.literal("lapis: " + describeStack(extractor.getStack(EnchantmentExtractorBlockEntity.SLOT_LAPIS))), false);
			return "";
		});
	}

	private static int fillLapis(CommandContext<ServerCommandSource> ctx) {
		int count = IntegerArgumentType.getInteger(ctx, "count");
		EnchantmentStorage.logDebugDev("es filllapis count={}", count);
		return withTargetedExtractor(ctx, (source, extractor) -> {
			ItemStack lapis = count <= 0 ? ItemStack.EMPTY : new ItemStack(Items.LAPIS_LAZULI, count);
			extractor.setStack(EnchantmentExtractorBlockEntity.SLOT_LAPIS, lapis);
			return "Set lapis slot to " + describeStack(lapis) + ".";
		});
	}

	private static String describeStack(ItemStack stack) {
		if (stack.isEmpty()) {
			return "empty";
		}
		return stack.getCount() + "x " + stack.getName().getString();
	}

	private static int adjustEnchantmentCount(CommandContext<ServerCommandSource> ctx, boolean store) throws CommandSyntaxException {
		RegistryEntry.Reference<?> enchantmentEntry =
			RegistryEntryReferenceArgumentType.getEnchantment(ctx, "enchantment");
		Identifier enchantmentId = enchantmentEntry.getKey()
			.map(RegistryKey::getValue)
			.orElse(null);
		if (enchantmentId == null) {
			ctx.getSource().sendError(Text.literal("Unknown enchantment."));
			return 0;
		}
		int rank = IntegerArgumentType.getInteger(ctx, "rank");
		int quantity = IntegerArgumentType.getInteger(ctx, "quantity");
		int levelIndex = rank - 1;
		int amount = store ? quantity : -quantity;
		String action = store ? "store" : "extract";
		EnchantmentStorage.logDebugDev(
			"es {} enchantment={} rank={} quantity={}",
			action,
			enchantmentId,
			rank,
			quantity
		);

		return withTargetedExtractor(ctx, (source, extractor) -> {
			extractor.debugAdjustEnchantment(enchantmentId, levelIndex, amount);
			String verb = store ? "Stored" : "Extracted";
			return String.format("%s %d of %s rank %d.", verb, quantity, enchantmentId, rank);
		});
	}

	private static int withTargetedExtractor(CommandContext<ServerCommandSource> ctx, DebugAction action) {
		ServerCommandSource source = ctx.getSource();
		ServerPlayerEntity player;

		try {
			player = source.getPlayer();
		} catch (Exception e) {
			source.sendError(Text.literal("Player required to run this command."));
			return 0;
		}

		if (!(player.raycast(RAYCAST_DISTANCE, RAYCAST_TICK_DELTA, false) instanceof BlockHitResult hit)) {
			source.sendFeedback(() -> Text.literal("Look at an Enchantment Extractor block."), false);
			return 0;
		}

		var be = source.getWorld().getBlockEntity(hit.getBlockPos());
		if (!(be instanceof EnchantmentExtractorBlockEntity extractor)) {
			source.sendFeedback(() -> Text.literal("Targeted block is not an Enchantment Extractor."), false);
			return 0;
		}
		EnchantmentStorage.logDebugDev(
			"es command player={} target={}",
			player.getName().getString(),
			hit.getBlockPos()
		);

		String message = action.apply(source, extractor);
		if (message != null && !message.isEmpty()) {
			source.sendFeedback(() -> Text.literal(message), false);
		}

		return 1;
	}

	@FunctionalInterface
	private interface DebugAction {
		@Nullable String apply(ServerCommandSource source, EnchantmentExtractorBlockEntity extractor);
	}
	// endregion
}
