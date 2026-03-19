package net.cosette.columbina.shop;

import com.google.gson.JsonSyntaxException;
import net.cosette.columbina.Columbina;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ShopConfigManager {
    private static final ShopConfigManager INSTANCE = new ShopConfigManager();
    private final Map<String, ShopConfig> configs = new HashMap<>();
    private Path shopsDir;
    private ShopConfigManager() {}
    public static ShopConfigManager getInstance() { return INSTANCE; }
    public void init() {
        shopsDir = FabricLoader.getInstance().getConfigDir()
                .resolve("columbina").resolve("shops");
        try {
            Files.createDirectories(shopsDir);
        } catch (IOException e) {
            Columbina.LOGGER.error("[Shop] Impossible de créer le dossier shops", e);
        }
        reload();
    }
    public void reload() {
        configs.clear();
        if (!Files.exists(shopsDir)) return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(shopsDir, "*.json")) {
            for (Path file : stream) {
                String shopId = file.getFileName().toString().replace(".json", "");
                try (Reader reader = Files.newBufferedReader(file)) {
                    ShopConfig config = ShopConfig.GSON.fromJson(reader, ShopConfig.class);
                    configs.put(shopId, config);
                    Columbina.LOGGER.info("[Shop] Chargé : {}", shopId);
                } catch (JsonSyntaxException e) {
                    Columbina.LOGGER.error("[Shop] JSON invalide : {}", file, e);
                }
            }
        } catch (IOException e) {
            Columbina.LOGGER.error("[Shop] Erreur lecture shops", e);
        }
    }
    public ShopConfig getConfig(String shopId) {
        return configs.get(shopId);
    }
    public Set<String> getShopIds() {
        return configs.keySet();
    }
}