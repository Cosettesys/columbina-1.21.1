package net.cosette.columbina;

import net.cosette.columbina.command.ColumbinaCommands;
import net.cosette.columbina.item.ModItems;
import net.cosette.columbina.scoreboard.ScoreboardManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.server.world.ServerWorld;
import net.cosette.columbina.team.TeamManager;

public class Columbina implements ModInitializer {
	public static final String MOD_ID = "columbina";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final int SCOREBOARD_UPDATE_INTERVAL = 100; // 5 secondes (20 ticks = 1 seconde)
	private int tickCounter = 0;
	@Override
	public void onInitialize() {
		LOGGER.info("Columbina charge!");
		ColumbinaCommands.register();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ServerWorld world = server.getOverworld();
			TeamManager.getInstance().init(world);
			ScoreboardManager.getInstance().init(world);
			System.out.println("TeamManager et ScoreboardManager initialisés !");
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCounter++;
			if (tickCounter >= SCOREBOARD_UPDATE_INTERVAL) {
				tickCounter = 0;
				ScoreboardManager.getInstance().updateAllScoreboards();
			}
		});
		ModItems.registerItems();
	}
}