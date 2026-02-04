package com.lexxeous.enchantment_storage.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class EnchantmentStorageMixin {
    // For Mojang mappings
    // @Inject(at = @At("HEAD"), method = "loadLevel")

    // For Yarn mappings
    @Inject(at = @At("HEAD"), method = "runServer")
    private void init(CallbackInfo info) {
        // This code is injected into the start of MinecraftServer.loadLevel()V
    }
}