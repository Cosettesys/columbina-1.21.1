package net.cosette.columbina.mixin;

import maxigregrze.cobblesafari.block.teleporter.SafariTeleporterBlock;
import net.cosette.columbina.Columbina;
import net.cosette.columbina.poketopia.PoketopiaManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.lang.reflect.Method;

@Mixin(value = SafariTeleporterBlock.class, remap = false)
public class CobbleSafariTeleporterMixin {
    @Inject(
            method = "method_9548",
            at = @At("HEAD"),
            cancellable = true
    )
    private void columbina$allowPoketopia(
            BlockState state,
            World level,
            BlockPos pos,
            Entity entity,
            CallbackInfo ci
    ) {
        if (level.isClient()) return;
        if (!(entity instanceof ServerPlayerEntity)) return;
        if (level.getRegistryKey().equals(PoketopiaManager.POKETOPIA_KEY)) {
            try {
                Class<?> handlerClass = Class.forName("maxigregrze.cobblesafari.teleporter.TeleporterTickHandler");
                for (Method m : handlerClass.getMethods()) {
                    if (m.getName().equals("updatePlayerOnTeleporter")) {
                        m.invoke(null, entity);
                        break;
                    }
                }
            } catch (Exception e) {
                Columbina.LOGGER.error("[Columbina] CobbleSafariTeleporterMixin: failed to invoke updatePlayerOnTeleporter", e);
            }
            ci.cancel();
        }
    }
}