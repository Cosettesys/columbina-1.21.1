package net.cosette.columbina;

import net.cosette.columbina.command.ColumbinaCommands;
import net.cosette.columbina.daily.DailyResetManager;
import net.cosette.columbina.item.ModItems;
import net.cosette.columbina.scoreboard.ScoreboardManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.server.world.ServerWorld;
import net.cosette.columbina.team.TeamManager;
import net.cosette.columbina.ColumbinaConfig;

public class Columbina implements ModInitializer {
	public static final String MOD_ID = "columbina";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final int SCOREBOARD_UPDATE_INTERVAL = 100; // 5 secondes (20 ticks = 1 seconde)
	private int tickCounter = 0;
	@Override
	public void onInitialize() {
		LOGGER.info("Columbina charge!");
		ColumbinaConfig.load();
		ColumbinaCommands.register();
		SafariCommandBlocker.register();
		EconomyConfigWriter.register();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ServerWorld world = server.getOverworld();
			TeamManager.getInstance().init(world);
			ScoreboardManager.getInstance().init(world);
			DailyResetManager.getInstance().init(world);
			System.out.println("DailyResetManager, TeamManager et ScoreboardManager init");
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCounter++;
			if (tickCounter >= SCOREBOARD_UPDATE_INTERVAL) {
				tickCounter = 0;
				ScoreboardManager.getInstance().updateAllScoreboards();
			}
		});
		ModItems.registerItems();
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			DailyResetManager.getInstance().onPlayerJoin(handler.player);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			DailyResetManager.getInstance().onPlayerDisconnect(handler.player);
		});
	}
}