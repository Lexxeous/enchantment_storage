package com.lexxeous.enchantment_storage.block;

import com.lexxeous.enchantment_storage.blockentity.EnchantmentExtractorBlockEntity;
import com.lexxeous.enchantment_storage.EnchantmentStorage;
import com.lexxeous.enchantment_storage.registry.ModBlockEntities;
import com.lexxeous.enchantment_storage.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnchantmentExtractorBlock extends Block implements BlockEntityProvider {
    // region Constant(s)
    private static final VoxelShape SHAPE = VoxelShapes.cuboid(
        0.0, 0.0, 0.0,
        1.0, 0.75, 1.0
    );
    // endregion

    // region Constructor(s)
    public EnchantmentExtractorBlock(Settings settings) {
        super(settings);
    }
    // endregion

    // region Override(s)
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnchantmentExtractorBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(
        BlockState state,
        World world,
        BlockPos pos,
        PlayerEntity player,
        BlockHitResult hit)
    {

        if (!world.isClient()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof EnchantmentExtractorBlockEntity extractor) {
                player.openHandledScreen(extractor);
            }

            EnchantmentStorage.LOGGER.info(
                "Enchantment Extractor used at {} by {} (hand={})",
                pos, player.getName().getString(), player.getMainHandStack()
            );

//            ChatComponent chatHud = Minecraft.getInstance().gui.getChat();
//            chatHud.addMessage(Component.translatable("text.enchantment_extractor.use"));
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, net.minecraft.world.BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state,
                           @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(world, player, pos, state, blockEntity, tool);

        if (!(world instanceof ServerWorld serverWorld)
            || !serverWorld.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
            return;
        }

        ItemStack drop = new ItemStack(ModItems.ENCHANTMENT_EXTRACTOR_ITEM);

        if (blockEntity instanceof EnchantmentExtractorBlockEntity extractor
            && extractor.hasPersistentExtractorData()) {
            NbtWriteView view = NbtWriteView.create(ErrorReporter.EMPTY);
            extractor.writePersistentDataWithoutInventory(view);
            BlockItem.setBlockEntityData(drop, ModBlockEntities.ENCHANTMENT_EXTRACTOR, view);
            drop.set(DataComponentTypes.MAX_STACK_SIZE, 1);
        }

        ItemScatterer.spawn(
            serverWorld,
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5,
            drop
        );
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (!moved) {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof EnchantmentExtractorBlockEntity extractor
                && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
                ItemScatterer.spawn(world, pos, extractor); // drop item inventory
            }
        }

        super.onStateReplaced(state, world, pos, moved);
    }
    // endregion
}
