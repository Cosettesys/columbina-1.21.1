// src/client/java/net/cosette/columbina/shop/ShopScreen.java
package net.cosette.columbina.shop;

import net.cosette.columbina.Columbina;
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

public class ShopScreen extends Screen {
    // Texture principale (shop_buy.png ou shop_sell.png de CobblemonRealms)
    private static final Identifier TEX_BUY = Identifier.of("columbina", "textures/gui/shop_buy.png");
    private static final Identifier TEX_SELL = Identifier.of("columbina", "textures/gui/shop_sell.png");
    private static final Identifier TEX_BTN_PREV = Identifier.of("columbina", "textures/gui/shop_btn_prev.png");
    private static final Identifier TEX_BTN_PREV_HOVER = Identifier.of("columbina", "textures/gui/shop_btn_prev_hover.png");
    private static final Identifier TEX_BTN_NEXT = Identifier.of("columbina", "textures/gui/shop_btn_next.png");
    private static final Identifier TEX_BTN_NEXT_HOVER = Identifier.of("columbina", "textures/gui/shop_btn_next_hover.png");
    private static final Identifier TEX_BTN_SWITCH = Identifier.of("columbina", "textures/gui/shop_btn_switch.png");
    // Dimensions de la GUI
    private static final int GUI_W = 195;
    private static final int GUI_H = 200;
    // Grille : 6 colonnes x 3 lignes = 18 slots
    private static final int COLS = 9;
    private static final int ROWS = 3;
    private static final int SLOT_SIZE = 18;
    // Décalage de la grille dans la texture (à ajuster selon la texture)
    private static final int GRID_OFFSET_X = 17;
    private static final int GRID_OFFSET_Y = 44;
    // Cycle de quantités
    private static final int[] QTY_CYCLE = {1, 2, 5, 10, 20, 64};
    private int qtyIndex = 0;
    private ShopPayloads.ShopOpenPayload payload;
    private int guiLeft, guiTop;
    private int hoveredSlot = -1;
    public ShopScreen(ShopPayloads.ShopOpenPayload payload) {
        super(Text.literal(payload.isBuy() ? "Boutique - Achat" : "Boutique - Vente"));
        this.payload = payload;
    }
    @Override
    protected void init() {
        guiLeft = (width - GUI_W) / 2;
        guiTop = (height - GUI_H) / 2;
        // Bouton page précédente
        addDrawableChild(ButtonWidget.builder(Text.literal("<"), btn -> changePage(-1))
                .dimensions(guiLeft + 8, guiTop + GUI_H - 0, 16, 13)
                .build());
        // Bouton page suivante
        addDrawableChild(ButtonWidget.builder(Text.literal(">"), btn -> changePage(1))
                .dimensions(guiLeft + GUI_W - 24, guiTop + GUI_H - 0, 16, 13)
                .build());
        // Bouton switch buy/sell
        addDrawableChild(ButtonWidget.builder(
                        Text.literal(payload.isBuy() ? "Vendre" : "Acheter"),
                        btn -> switchMode())
                .dimensions(guiLeft + GUI_W / 2 - 20, guiTop + GUI_H - 0, 40, 13)
                .build());
    }
    private void changePage(int delta) {
        int newPage = payload.pageIndex() + delta;
        if (newPage < 0) return;
        ClientPlayNetworking.send(new ShopPayloads.ShopActionPayload(
                payload.shopId(), payload.isBuy(), -1, newPage // -1 = navigation
        ));
    }
    private void switchMode() {
        ClientPlayNetworking.send(new ShopPayloads.ShopActionPayload(
                payload.shopId(), !payload.isBuy(), -2, 0 // -2 = switch mode
        ));
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Pas de flou, juste un fond semi-transparent léger
        //context.fill(0, 0, width, height, 0x80000000);
        Identifier tex = payload.isBuy() ? TEX_BUY : TEX_SELL;
        context.drawTexture(tex, guiLeft, guiTop, 0.0f, 0.0f, GUI_W, GUI_H, GUI_W, GUI_H);
        context.drawText(textRenderer,
                Text.literal("§6" + payload.playerPoints() + " pts"),
                guiLeft + GUI_W - 60, guiTop + 6, 0xFFFFFF, true);
        context.drawText(textRenderer,
                Text.literal("§fx" + QTY_CYCLE[qtyIndex]),
                guiLeft + 8, guiTop + 6, 0xFFFFFF, true);
        context.drawText(textRenderer,
                Text.literal("§7Page " + (payload.pageIndex() + 1)),
                guiLeft + GUI_W / 2 - 15, guiTop + 6, 0xFFFFFF, true);
        List<ShopPayloads.SlotData> slots = payload.slots();
        hoveredSlot = -1;
        for (int i = 0; i < Math.min(slots.size(), ROWS * COLS); i++) {
            int col = i % COLS;
            int row = i / COLS;
            int sx = guiLeft + GRID_OFFSET_X + col * SLOT_SIZE;
            int sy = guiTop + GRID_OFFSET_Y + row * SLOT_SIZE;
            ShopPayloads.SlotData slot = slots.get(i);
            if (slot.itemId().equals("minecraft:air") || slot.itemId().isEmpty()) continue;
            var itemId = Identifier.tryParse(slot.itemId());
            if (itemId == null) continue;
            var item = Registries.ITEM.get(itemId);
            var stack = new ItemStack(item);
            context.drawItem(stack, sx + 1, sy + 1);
            if (mouseX >= sx && mouseX < sx + SLOT_SIZE &&
                    mouseY >= sy && mouseY < sy + SLOT_SIZE) {
                hoveredSlot = i;
                context.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, 0x80FFFFFF);
            }
        }
        renderPlayerInventory(context);
        super.render(context, mouseX, mouseY, delta);
        if (hoveredSlot >= 0 && hoveredSlot < slots.size()) {
            ShopPayloads.SlotData slot = slots.get(hoveredSlot);
            var itemId = Identifier.tryParse(slot.itemId());
            if (itemId != null) {
                var item = Registries.ITEM.get(itemId);
                var stack = new ItemStack(item);
                int price = slot.price();
                String priceStr = price < 0 ? "Non disponible" : price * QTY_CYCLE[qtyIndex] + " pts";
                context.drawTooltip(textRenderer, List.of(
                        stack.getName(),
                        Text.literal("§7Prix : §6" + priceStr),
                        Text.literal("§8Clic gauche : acheter direct"),
                        Text.literal("§8Clic droit : confirmation"),
                        Text.literal("§8Molette : changer quantité")
                ), mouseX, mouseY);
            }
        }
    }
    private void renderPlayerInventory(DrawContext context) {
        // Zone inventaire en bas — 9 colonnes x 3 lignes + barre de raccourcis
        int invStartX = guiLeft + GRID_OFFSET_X;
        int invStartY = guiTop + GRID_OFFSET_Y + ROWS * SLOT_SIZE + 19;
        var player = MinecraftClient.getInstance().player;
        if (player == null) return;
        // Inventaire principal (lignes 1-3, slots 9-35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIdx = 9 + row * 9 + col;
                int sx = invStartX + col * SLOT_SIZE;
                int sy = invStartY + row * SLOT_SIZE;
                ItemStack stack = player.getInventory().getStack(slotIdx);
                if (!stack.isEmpty()) {
                    context.drawItem(stack, sx + 1, sy + 1);
                    context.drawItemInSlot(textRenderer, stack, sx + 1, sy + 1);
                }
            }
        }
        // Barre de raccourcis (slots 0-8)
        int hotbarY = invStartY + 3 * SLOT_SIZE + 4;
        for (int col = 0; col < 9; col++) {
            int sx = invStartX + col * SLOT_SIZE;
            ItemStack stack = player.getInventory().getStack(col);
            if (!stack.isEmpty()) {
                context.drawItem(stack, sx + 1, hotbarY + 1);
                context.drawItemInSlot(textRenderer, stack, sx + 1, hotbarY + 1);
            }
        }
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredSlot >= 0) {
            ShopPayloads.SlotData slot = payload.slots().get(hoveredSlot);
            if (slot.price() < 0) return true;
            if (button == 1) {
                ClientPlayNetworking.send(new ShopPayloads.ShopActionPayload(
                        payload.shopId(), payload.isBuy(),
                        hoveredSlot, QTY_CYCLE[qtyIndex]));
                return true;
            } else if (button == 0) {
                MinecraftClient.getInstance().setScreen(
                        new ShopConfirmScreen(this, payload, hoveredSlot, QTY_CYCLE[qtyIndex]));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hoveredSlot >= 0) {
            if (verticalAmount > 0) {
                qtyIndex = (qtyIndex + 1) % QTY_CYCLE.length;
            } else {
                qtyIndex = (qtyIndex - 1 + QTY_CYCLE.length) % QTY_CYCLE.length;
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    public void updatePayload(ShopPayloads.ShopOpenPayload newPayload) {
        this.payload = newPayload;
    }
    @Override
    public boolean shouldPause() { return false; }
    @Override
    protected void applyBlur(float delta) {
    }
}