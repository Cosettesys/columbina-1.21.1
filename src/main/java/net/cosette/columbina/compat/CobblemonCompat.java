package net.cosette.columbina.compat;

import net.cosette.columbina.Columbina;
import net.fabricmc.loader.api.FabricLoader;

public class CobblemonCompat {

    public static final boolean LOADED =
            FabricLoader.getInstance().isModLoaded("cobblemon");
    public static void tryRegister() {
        if (!LOADED) {
            Columbina.LOGGER.info("[Columbina] Cobblemon absent — intégration désactivée.");
            return;
        }
        Columbina.LOGGER.info("[Columbina] Cobblemon détecté — intégration activée.");
        CobblemonListeners.INSTANCE.register();
    }
}