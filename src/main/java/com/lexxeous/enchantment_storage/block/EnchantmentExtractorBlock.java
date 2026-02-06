package com.lexxeous.enchantment_storage.block;

import com.lexxeous.enchantment_storage.blockentity.EnchantmentExtractorBlockEntity;
import com.lexxeous.enchantment_storage.EnchantmentStorage;
import com.lexxeous.enchantment_storage.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnchantmentExtractorBlock extends Block implements BlockEntityProvider {
    // region Constructor(s)
    public EnchantmentExtractorBlock(Settings settings) {
        super(settings);
    }
    // endregion

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnchantmentExtractorBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient() ? null : checkType(type, ModBlockEntities.ENCHANTMENT_EXTRACTOR, EnchantmentExtractorBlockEntity::tick);
    }

    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(
            BlockEntityType<A> givenType,
            BlockEntityType<E> expectedType,
            BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
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
}
