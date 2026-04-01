package net.cosette.columbina.compat;

import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import net.minecraft.server.network.ServerPlayerEntity;

public class CobblemonEventWrapper {
    public static ServerPlayerEntity getPlayer(PokemonCapturedEvent event) {
        try {
            Object player = PokemonCapturedEvent.class.getMethod("getPlayer").invoke(event);
            return (ServerPlayerEntity) player;
        } catch (Exception e) {
            return null;
        }
    }
    public static boolean isShiny(PokemonCapturedEvent event) {
        return event.getPokemon().getShiny();
    }
    public static boolean isLegendary(PokemonCapturedEvent event) {
        return event.getPokemon().getSpecies().getLabels()
                .stream()
                .anyMatch(label -> label.toString().contains("legendary"));
    }
    public static boolean isMythical(PokemonCapturedEvent event) {
        return event.getPokemon().getSpecies().getLabels()
                .stream()
                .anyMatch(label -> label.toString().contains("mythical"));
    }
    public static String getSpeciesName(PokemonCapturedEvent event) {
        return event.getPokemon().getSpecies().getName();
    }
}