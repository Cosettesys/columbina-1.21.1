package net.cosette.columbina;

import net.cosette.columbina.command.ColumbinaCommands;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.server.world.ServerWorld;
import net.cosette.columbina.team.TeamManager;

public class Columbina implements ModInitializer {
	public static final String MOD_ID = "columbina";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Columbina charge!");

		ColumbinaCommands.register();
		// Initialisation du TeamManager à chaque démarrage de serveur
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ServerWorld world = server.getOverworld(); // récupère l'Overworld
			TeamManager.getInstance().init(world);    // initialise le TeamManager avec ce monde
			System.out.println("TeamManager initialisé !");
		});
	}
}