package net.cosette.columbina.mixin.client;

import net.cosette.columbina.portal.ModBlocks;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.BlockModels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModels.class)
public class PortalRenderLayerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void columbina$registerPortalRenderLayers(CallbackInfo ci) {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.POKETOPIA_PORTAL, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.NETHER_PORTAL_CUSTOM, RenderLayer.getTranslucent());
    }
}