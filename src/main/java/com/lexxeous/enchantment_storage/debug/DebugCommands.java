package com.lexxeous.enchantment_storage.debug;

import com.lexxeous.enchantment_storage.blockentity.EnchantmentExtractorBlockEntity;
import com.lexxeous.enchantment_storage.mapping.EnchantmentCategory;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;

public final class DebugCommands {
    private static final double RAYCAST_DISTANCE = 5.0;
    private static final float RAYCAST_TICK_DELTA = 0.0f;

    private DebugCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(CommandManager.literal("es")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("demo").executes(ctx ->
                    withTargetedExtractor(ctx, extractor -> {
                        extractor.applyDemoCategories();
                        return "Debug demo applied to targeted block.";
                    })
                ))
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

        return withTargetedExtractor(ctx, extractor -> {
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

        String message = action.apply(extractor);
        if (message != null && !message.isEmpty()) {
            source.sendFeedback(() -> Text.literal(message), false);
        }

        return 1;
    }

    @FunctionalInterface
    private interface DebugAction {
        String apply(EnchantmentExtractorBlockEntity extractor);
    }
}
