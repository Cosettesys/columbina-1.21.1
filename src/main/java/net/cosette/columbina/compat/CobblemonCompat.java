package net.cosette.columbina.compat;

import net.cosette.columbina.Columbina;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class CobblemonCompat {
    public static final boolean LOADED =
            FabricLoader.getInstance().isModLoaded("cobblemon");
    public static void tryRegister(MinecraftServer server) {
        if (!LOADED) {
            Columbina.LOGGER.info("[Columbina] Cobblemon absent — intégration désactivée.");
            return;
        }
        Columbina.LOGGER.info("[Columbina] Cobblemon détecté — intégration activée.");
        CobblemonListeners.INSTANCE.register(server);
    }
}