// src/client/java/net/cosette/columbina/shop/ShopConfirmScreen.java
package net.cosette.columbina.shop;

import net.cosette.columbina.network.ShopPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ShopConfirmScreen extends Screen {
    private static final Identifier TEX_CONFIRM =
            Identifier.of("columbina", "textures/gui/shop_confirm.png");
    private static final int GUI_W = 180;
    private static final int GUI_H = 92;
    private final Screen parent;
    private final ShopPayloads.ShopOpenPayload payload;
    private final int slotIndex;
    private final int quantity;
    private int guiLeft, guiTop;
    public ShopConfirmScreen(Screen parent, ShopPayloads.ShopOpenPayload payload,
                             int slotIndex, int quantity) {
        super(Text.literal("Confirmation"));
        this.parent = parent;
        this.payload = payload;
        this.slotIndex = slotIndex;
        this.quantity = quantity;
    }
    @Override
    protected void init() {
        guiLeft = (width - GUI_W) / 2;
        guiTop = (height - GUI_H) / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("§aConfirmer"), btn -> {
            ClientPlayNetworking.send(new ShopPayloads.ShopActionPayload(
                    payload.shopId(), payload.isBuy(), slotIndex, quantity));
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(guiLeft + 10, guiTop + GUI_H - 25, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("§cAnnuler"), btn ->
                        MinecraftClient.getInstance().setScreen(parent))
                .dimensions(guiLeft + GUI_W - 80, guiTop + GUI_H - 25, 70, 20)
                .build());
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(TEX_CONFIRM, guiLeft, guiTop, 0.0f, 0.0f, GUI_W, GUI_H, GUI_W, GUI_H);
        ShopPayloads.SlotData slot = payload.slots().get(slotIndex);
        var itemId = Identifier.tryParse(slot.itemId());
        int totalPrice = slot.price() * quantity;
        String action = payload.isBuy() ? "Acheter" : "Vendre";
        if (itemId != null) {
            var item = Registries.ITEM.get(itemId);
            var stack = new ItemStack(item, quantity);
            context.drawItem(stack, guiLeft + GUI_W / 2 - 8, guiTop + 20);
            context.drawText(textRenderer,
                    stack.getName(),
                    guiLeft + GUI_W / 2 - textRenderer.getWidth(stack.getName()) / 2,
                    guiTop + 10, 0xFFFFFF, true);
        }
        context.drawText(textRenderer,
                Text.literal(action + " x" + quantity + " pour §6" + totalPrice + " pts§r ?"),
                guiLeft + GUI_W / 2 - 60, guiTop + 45, 0xFFFFFF, true);
        context.drawText(textRenderer,
                Text.literal("§7Vos points : §6" + payload.playerPoints()),
                guiLeft + GUI_W / 2 - 40, guiTop + 57, 0xFFFFFF, true);
        super.render(context, mouseX, mouseY, delta);
    }
    @Override
    public boolean shouldPause() { return false; }
    @Override
    protected void applyBlur(float delta) {
    }
}