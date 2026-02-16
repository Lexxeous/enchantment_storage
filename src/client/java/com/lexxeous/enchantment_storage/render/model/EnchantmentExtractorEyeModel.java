package com.lexxeous.enchantment_storage.render.model;

import com.lexxeous.enchantment_storage.render.block.entity.state.EnchantmentExtractorEyeRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;

public final class EnchantmentExtractorEyeModel extends Model<EnchantmentExtractorEyeRenderState> {
	// region Constructors
	public EnchantmentExtractorEyeModel(ModelPart root) {
		super(root, RenderLayer::getEntityCutoutNoCull);
	}
	// endregion

	// region Helpers
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();
		root.addChild(
			"eye",
			ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -4.0f, 0.0f, 8.0f, 8.0f, 0.0f),
			ModelTransform.NONE
		);
		return TexturedModelData.of(modelData, 16, 16);
	}
	// endregion
}