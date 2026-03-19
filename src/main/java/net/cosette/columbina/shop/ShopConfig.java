package net.cosette.columbina.shop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;

public class ShopConfig {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public List<Page> buyPages = new ArrayList<>();
    public List<Page> sellPages = new ArrayList<>();
    public static class Page {
        public boolean enabled = true;
        public List<Entry> items = new ArrayList<>();
    }
    public static class Entry {
        public String item = "minecraft:stick";  // ID Minecraft de l'item
        public int buyPrice = -1;   // -1 = pas achetable
        public int sellPrice = -1;  // -1 = pas vendable
        public int stock = -1;      // -1 = illimité
    }
}