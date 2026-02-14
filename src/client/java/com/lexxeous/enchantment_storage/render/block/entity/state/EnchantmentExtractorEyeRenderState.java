package com.lexxeous.enchantment_storage.render.block.entity.state;

import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;

public final class EnchantmentExtractorEyeRenderState extends BlockEntityRenderState {
    public float ticks;
    public final ItemRenderState itemRenderState = new ItemRenderState();
    public long lastParticleWorldTick = Long.MIN_VALUE;
}
