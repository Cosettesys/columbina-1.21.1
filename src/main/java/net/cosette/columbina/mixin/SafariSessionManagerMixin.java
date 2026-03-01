package net.cosette.columbina.mixin;

import com.safari.config.SafariConfig;
import com.safari.economy.SafariEconomy;
import com.safari.session.SafariSessionManager;
import net.cosette.columbina.ColumbinaConfig;
import net.cosette.columbina.SafariEntryGuard;
import net.cosette.columbina.team.TeamManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SafariSessionManager.class, remap = false)
public class SafariSessionManagerMixin {
    @Inject(method = "tryStartSession(Lnet/minecraft/class_3222;Z)Z",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void onTryStartSession(ServerPlayerEntity player, boolean chargeEntry, CallbackInfoReturnable<Boolean> cir) {
        if (!chargeEntry) return;

        // Bloquer si pas autorisé par Columbina
        if (!SafariEntryGuard.AUTHORIZED.get()) {
            player.sendMessage(Text.literal("§c[Safari] Tu dois parler au gardien du Safari pour entrer !"), false);
            cir.setReturnValue(false);
            return;
        }

        TeamManager tm = TeamManager.getInstance();
        String team = tm.getPlayerTeam(player);
        if (team == null) {
            player.sendMessage(Text.literal("§c[Safari] Tu dois être dans une équipe pour entrer !"), false);
            cir.setReturnValue(false);
            return;
        }
        int cost = ColumbinaConfig.getInstance().getSafariCost();
        int points = tm.getPoints(team);
        if (points < cost) {
            player.sendMessage(Text.literal("§c[Safari] Pas assez de points ! Il faut §6" + cost + " §cpoints (tu en as §6" + points + "§c)."), false);
            cir.setReturnValue(false);
            return;
        }
        tm.addPoints(team, -cost);
        SafariConfig.get().entrancePrice = 0;
    }
}
