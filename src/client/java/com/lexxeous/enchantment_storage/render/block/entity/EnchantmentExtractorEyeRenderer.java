package com.lexxeous.enchantment_storage.render.block.entity;

import com.lexxeous.enchantment_storage.blockentity.EnchantmentExtractorBlockEntity;
import com.lexxeous.enchantment_storage.render.block.entity.state.EnchantmentExtractorEyeRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.util.math.MatrixStack;

public final class EnchantmentExtractorEyeRenderer implements
    BlockEntityRenderer<EnchantmentExtractorBlockEntity, EnchantmentExtractorEyeRenderState> {

    private static final ItemStack EYE_ITEM_STACK = new ItemStack(Items.ENDER_EYE);
    private final ItemModelManager itemModelManager;

    public EnchantmentExtractorEyeRenderer(BlockEntityRendererFactory.Context context) {
        this.itemModelManager = MinecraftClient.getInstance().getItemModelManager();
    }

    @Override
    public EnchantmentExtractorEyeRenderState createRenderState() {
        return new EnchantmentExtractorEyeRenderState();
    }

    @Override
    public void updateRenderState(
        EnchantmentExtractorBlockEntity blockEntity,
        EnchantmentExtractorEyeRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);

        if (blockEntity.getWorld() == null) {
            return;
        }

        state.ticks = blockEntity.getWorld().getTime() + tickProgress;
        itemModelManager.clearAndUpdate(
            state.itemRenderState,
            EYE_ITEM_STACK,
            ItemDisplayContext.GROUND,
            blockEntity.getWorld(),
            null,
            blockEntity.getPos().hashCode()
        );

        if (blockEntity.getWorld() instanceof ClientWorld clientWorld) {
            long worldTick = clientWorld.getTime();
            if (state.lastParticleWorldTick != worldTick) {
                state.lastParticleWorldTick = worldTick;

                // Every other tick, with ~20% less density.
                if ((worldTick & 1L) == 0L && clientWorld.random.nextFloat() < 0.3f) {
                    spawnPortalParticles(clientWorld, blockEntity.getPos(), state.ticks);
                }
            }
        }
    }

    @Override
    public void render(
        EnchantmentExtractorEyeRenderState state,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        CameraRenderState cameraState
    ) {
        matrices.push();
        matrices.translate(0.5f, 0.95f + (MathHelper.sin(state.ticks * 0.12f) * 0.06f), 0.5f);
        matrices.scale(0.8f, 0.8f, 0.8f);
        matrices.multiply(cameraState.orientation);
        state.itemRenderState.render(
            matrices,
            queue,
            state.lightmapCoordinates,
            OverlayTexture.DEFAULT_UV,
            0
        );

        matrices.pop();
    }

    private void spawnPortalParticles(ClientWorld world, net.minecraft.util.math.BlockPos pos, float ticks) {
        Vec3d center = Vec3d.ofCenter(pos).add(0.0, 0.95, 0.0);
        double bobOffsetY = MathHelper.sin(ticks * 0.12f) * 0.06f;
        double x = center.x + world.random.nextDouble() - 0.5;
        double y = center.y + bobOffsetY - 0.65;
        double z = center.z + world.random.nextDouble() - 0.5;
        double velocityX = (world.random.nextDouble() - 0.5) * 0.02;
        double velocityY = -0.005;
        double velocityZ = (world.random.nextDouble() - 0.5) * 0.02;
        world.addParticleClient(ParticleTypes.REVERSE_PORTAL, x, y, z, velocityX, velocityY, velocityZ);
    }
}
