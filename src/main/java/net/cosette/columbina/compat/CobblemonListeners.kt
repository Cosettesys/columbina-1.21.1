package net.cosette.columbina.compat

import com.cobblemon.mod.common.api.events.CobblemonEvents
import net.cosette.columbina.Columbina
import net.cosette.columbina.team.TeamManager

object CobblemonListeners {

    fun register() {
        CobblemonEvents.POKEMON_CAPTURED.subscribe { event ->
            val player = CobblemonEventWrapper.getPlayer(event) ?: return@subscribe
            val tm = TeamManager.getInstance()
            val team = tm.getTeamOf(player.uuid) ?: return@subscribe
            val isShiny = CobblemonEventWrapper.isShiny(event)
            val isLegendary = CobblemonEventWrapper.isLegendary(event)
            var points = 1
            if (isShiny) points += 5
            if (isLegendary) points += 10
            tm.addPoints(team, points)
            Columbina.LOGGER.info(
                "[Columbina] {} capture {} → +{} pts à l'équipe {}",
                player.nameForScoreboard,
                CobblemonEventWrapper.getSpeciesName(event),
                points,
                team
            )
        }
        Columbina.LOGGER.info("[Columbina] Listeners Cobblemon enregistrés.")
    }
}