package net.cosette.columbina.mixin;

import net.cosette.columbina.poketopia.PoketopiaManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerRespawnMixin {

    @Inject(
            method = "respawnPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;ZLnet/minecraft/entity/Entity$RemovalReason;)Lnet/minecraft/server/network/ServerPlayerEntity;",
            at = @At("RETURN")
    )
    private void columbina$onRespawn(
            ServerPlayerEntity player,
            boolean alive,
            Entity.RemovalReason removalReason,   // ← paramètre manquant
            CallbackInfoReturnable<ServerPlayerEntity> cir
    ) {
        ServerPlayerEntity respawned = cir.getReturnValue();
        if (respawned == null) return;

        if (!alive) {
            PoketopiaManager.getInstance().onRespawn(respawned);
        }
    }
}