package net.cosette.columbina.shop;

import net.cosette.columbina.Columbina;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopPlayerStockData extends PersistentState {

    private static final String DATA_KEY = "columbina_shop_stock";
    private final Map<String, Map<UUID, Integer>> purchases = new HashMap<>();

    public static ShopPlayerStockData get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new PersistentState.Type<>(
                        ShopPlayerStockData::new,
                        ShopPlayerStockData::fromNbt,
                        null
                ),
                DATA_KEY
        );
    }

    private static ShopPlayerStockData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        ShopPlayerStockData data = new ShopPlayerStockData();
        NbtCompound root = nbt.getCompound("purchases");
        for (String key : root.getKeys()) {
            NbtCompound players = root.getCompound(key);
            Map<UUID, Integer> map = new HashMap<>();
            for (String uuidStr : players.getKeys()) {
                map.put(UUID.fromString(uuidStr), players.getInt(uuidStr));
            }
            data.purchases.put(key, map);
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound root = new NbtCompound();
        for (Map.Entry<String, Map<UUID, Integer>> entry : purchases.entrySet()) {
            NbtCompound players = new NbtCompound();
            for (Map.Entry<UUID, Integer> p : entry.getValue().entrySet()) {
                players.putInt(p.getKey().toString(), p.getValue());
            }
            root.put(entry.getKey(), players);
        }
        nbt.put("purchases", root);
        return nbt;
    }

    public int getPurchaseCount(String shopId, String itemId, UUID playerId) {
        return purchases
                .getOrDefault(shopId + "_" + itemId, new HashMap<>())
                .getOrDefault(playerId, 0);
    }

    public void incrementPurchase(String shopId, String itemId, UUID playerId, int qty) {
        String key = shopId + "_" + itemId;
        purchases.computeIfAbsent(key, k -> new HashMap<>())
                .merge(playerId, qty, Integer::sum);
        markDirty();
    }

    public void resetAllPlayerStocks() {
        purchases.clear();
        markDirty();
        Columbina.LOGGER.info("[Shop] Stocks joueurs réinitialisés.");
    }
}