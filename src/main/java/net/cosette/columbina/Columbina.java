package net.cosette.columbina;

import net.cosette.columbina.command.ColumbinaCommands;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Columbina implements ModInitializer {
	public static final String MOD_ID = "columbina";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		LOGGER.info("Columbina charge!");
		ColumbinaCommands.register();
	}
}