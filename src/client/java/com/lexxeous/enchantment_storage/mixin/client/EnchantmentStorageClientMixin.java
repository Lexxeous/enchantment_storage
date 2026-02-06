package com.lexxeous.enchantment_storage.mixin.client;

// region Mojang mappings
//import net.minecraft.client.Minecraft;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(Minecraft.class)
//public class EnchantmentStorageClientMixin {
//	@Inject(at = @At("HEAD"), method = "run")
//	private void init(CallbackInfo info) {
//		// This code is injected into the start of Minecraft.run()
//	}
//}
// endregion

// region Yarn mappings
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class EnchantmentStorageClientMixin {
	@Inject(method = "run", at = @At("HEAD"))
	private void init(CallbackInfo info) {
		// injected at start of MinecraftClient.run()
	}
}
// endregion