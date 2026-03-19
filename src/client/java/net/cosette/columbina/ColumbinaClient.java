package net.cosette.columbina;

import net.cosette.columbina.network.ShopPayloads;
import net.cosette.columbina.shop.ShopScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class ColumbinaClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(
				ShopPayloads.ShopOpenPayload.ID,
				(payload, context) -> context.client().execute(() -> {
					Screen current = MinecraftClient.getInstance().currentScreen;
					if (current instanceof ShopScreen shopScreen) {
						// Mise à jour de la page en cours
						shopScreen.updatePayload(payload);
					} else {
						MinecraftClient.getInstance().setScreen(new ShopScreen(payload));
					}
				}));
	}
}