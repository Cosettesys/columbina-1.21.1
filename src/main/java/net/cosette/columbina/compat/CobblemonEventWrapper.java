package net.cosette.columbina.compat;

import com.cobblemon.mod.common.api.events.entity.SpawnEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.events.pokemon.RidePokemonEvent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;


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
    public static ServerPlayerEntity getPlayerRIDE(RidePokemonEvent event) {
        try {
            Object player = RidePokemonEvent.class.getMethod("getPlayer").invoke(event);
            return (ServerPlayerEntity) player;
        } catch (Exception e) {
            return null;
        }
    }
    public static ServerWorld getSpawnWorld(Object event) {
        try {
            Object pos = event.getClass().getMethod("getSpawnablePosition").invoke(event);
            return (ServerWorld) pos.getClass().getMethod("getWorld").invoke(pos);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isPokemonEntity(Object event) {
        try {
            Object entity = event.getClass().getMethod("getEntity").invoke(event);
            return entity.getClass().getName().contains("PokemonEntity");
        } catch (Exception e) {
            return false;
        }
    }
}