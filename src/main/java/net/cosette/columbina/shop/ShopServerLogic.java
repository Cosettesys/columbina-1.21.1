package net.cosette.columbina.shop;

import net.cosette.columbina.Columbina;
import net.cosette.columbina.network.ShopPayloads;
import net.cosette.columbina.team.TeamManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.stream.Collectors;

public class ShopServerLogic {
    public static void openShop(ServerPlayerEntity player, String shopId, boolean isBuy) {
        ShopConfig config = ShopConfigManager.getInstance().getConfig(shopId);
        if (config == null) {
            player.sendMessage(Text.literal("§cShop introuvable : " + shopId), false);
            return;
        }
        List<ShopConfig.Page> pages = isBuy ? config.buyPages : config.sellPages;
        if (pages.isEmpty()) {
            player.sendMessage(Text.literal("§cCe shop n'a pas de page " + (isBuy ? "achat" : "vente")), false);
            return;
        }
        sendPage(player, shopId, isBuy, 0, pages);
    }
    private static void sendPage(ServerPlayerEntity player, String shopId,
                                 boolean isBuy, int pageIndex,
                                 List<ShopConfig.Page> pages) {
        ShopConfig.Page page = pages.get(pageIndex);
        String teamName = TeamManager.getInstance().getPlayerTeam(player);
        int points = teamName != null ? TeamManager.getInstance().getPoints(teamName) : 0;
        List<ShopPayloads.SlotData> slots = page.items.stream()
                .map(e -> new ShopPayloads.SlotData(
                        e.item,
                        isBuy ? e.buyPrice : e.sellPrice,
                        e.stock))
                .collect(Collectors.toList());

        ServerPlayNetworking.send(player,
                new ShopPayloads.ShopOpenPayload(shopId, isBuy, pageIndex, points, slots));
    }
    public static void handleAction(ServerPlayerEntity player, ShopPayloads.ShopActionPayload action) {
        if (action.slotIndex() == -1) {
            ShopConfig config = ShopConfigManager.getInstance().getConfig(action.shopId());
            if (config == null) return;
            List<ShopConfig.Page> pages = action.isBuy() ? config.buyPages : config.sellPages;
            int newPage = action.quantity(); // quantity utilisé pour stocker le numéro de page
            if (newPage < 0 || newPage >= pages.size()) return;
            sendPage(player, action.shopId(), action.isBuy(), newPage, pages);
            return;
        }
        if (action.slotIndex() == -2) {
            openShop(player, action.shopId(), action.isBuy());
            return;
        }
        ShopConfig config = ShopConfigManager.getInstance().getConfig(action.shopId());
        if (config == null) return;
        List<ShopConfig.Page> pages = action.isBuy() ? config.buyPages : config.sellPages;
        if (pages.isEmpty()) return;
        ShopConfig.Page page = pages.get(0); // TODO : gérer pageIndex si pagination
        if (action.slotIndex() < 0 || action.slotIndex() >= page.items.size()) return;
        ShopConfig.Entry entry = page.items.get(action.slotIndex());
        int price = action.isBuy() ? entry.buyPrice : entry.sellPrice;
        if (price < 0) return;
        int qty = action.quantity();
        int totalCost = price * qty;
        String teamName = TeamManager.getInstance().getPlayerTeam(player);
        if (teamName == null) {
            player.sendMessage(Text.literal("§cTu n'es dans aucune équipe !"), false);
            return;
        }
        if (action.isBuy()) {
            var itemId = Identifier.tryParse(entry.item);
            if (itemId == null) return;
            var item = Registries.ITEM.get(itemId);
            // Vérifier les points
            int points = TeamManager.getInstance().getPoints(teamName);
            if (points < totalCost) {
                player.sendMessage(Text.literal("§cPas assez de points ! (besoin: " + totalCost + ", disponible: " + points + ")"), false);
                return;
            }
            ItemStack toGive = new ItemStack(item, qty);
            // Calculer l'espace disponible pour cet item
            int availableSpace = 0;
            for (int i = 0; i < player.getInventory().main.size(); i++) {
                ItemStack existing = player.getInventory().main.get(i);
                if (existing.isEmpty()) {
                    availableSpace += item.getMaxCount();
                } else if (existing.getItem() == item) {
                    availableSpace += existing.getMaxCount() - existing.getCount();
                }
            }
            if (availableSpace < qty) {
                player.sendMessage(Text.literal("§cPas assez de place ! (place dispo: " + availableSpace + ", demandé: " + qty + ")"), false);
                return;
            }
            player.getInventory().insertStack(toGive);
            TeamManager.getInstance().addPoints(teamName, -totalCost);
            player.sendMessage(Text.literal("§aAchat effectué ! (-" + totalCost + " pts)"), false);
        } else {
            var itemId = Identifier.tryParse(entry.item);
            if (itemId == null) return;
            var item = Registries.ITEM.get(itemId);
            int found = 0;
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack s = player.getInventory().getStack(i);
                if (s.getItem() == item) found += s.getCount();
            }
            if (found < qty) {
                player.sendMessage(Text.literal("§cTu n'as pas assez de cet item ! (besoin: " + qty + ", trouvé: " + found + ")"), false);
                return;
            }
            int toRemove = qty;
            for (int i = 0; i < player.getInventory().size() && toRemove > 0; i++) {
                ItemStack s = player.getInventory().getStack(i);
                if (s.getItem() == item) {
                    int take = Math.min(s.getCount(), toRemove);
                    s.decrement(take);
                    toRemove -= take;
                }
            }
            TeamManager.getInstance().addPoints(teamName, totalCost);
            player.sendMessage(Text.literal("§aVente effectuée ! (+" + totalCost + " pts)"), false);
        }
    }
}