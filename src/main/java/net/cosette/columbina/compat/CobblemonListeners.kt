package net.cosette.columbina.compat

import com.cobblemon.mod.common.api.events.CobblemonEvents
import net.cosette.columbina.Columbina
import net.cosette.columbina.ColumbinaConfig
import net.cosette.columbina.team.TeamManager
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text

object CobblemonListeners {

    private var server: MinecraftServer? = null

    fun register(server: MinecraftServer) {
        this.server = server
        CobblemonEvents.POKEMON_CAPTURED.subscribe { event ->
            val player = CobblemonEventWrapper.getPlayer(event) ?: return@subscribe
            val tm = TeamManager.getInstance()
            val team = tm.getTeamOf(player.uuid) ?: return@subscribe
            val cfg = ColumbinaConfig.getInstance()
            val species = CobblemonEventWrapper.getSpeciesName(event)
            val limit = cfg.captureLimitPerSpecies
            if (limit > 0) {
                val overworld = server?.overworld ?: return@subscribe
                val data = CaptureCountSavedData.get(overworld)
                val count = data.getCount(player.uuid, species)
                if (count >= limit) return@subscribe // pas de points, pas de message
                data.increment(player.uuid, species)
            }
            val isShiny = CobblemonEventWrapper.isShiny(event)
            val isLegendary = CobblemonEventWrapper.isLegendary(event)
            val isMythical = CobblemonEventWrapper.isMythical(event)
            var points = cfg.capturePointsBase
            if (isShiny) points += cfg.capturePointsShinyBonus
            if (isLegendary) points += cfg.capturePointsLegendaryBonus
            if (isMythical) points += cfg.capturePointsMythicalBonus
            tm.addPoints(team, points)
            val tags = mutableListOf<String>()
            if (isShiny) tags.add("✨ Shiny")
            if (isLegendary) tags.add("⭐ Légendaire")
            if (isMythical) tags.add("🌟 Mythique")
            val tagStr = if (tags.isEmpty()) "" else " §7(${tags.joinToString(", ")}§7)"
            // Calcul du count après incrément pour l'affichage
            val currentCount = if (limit > 0) {
                CaptureCountSavedData.get(server?.overworld ?: return@subscribe).getCount(player.uuid, species)
            } else 0
            val countStr = if (limit > 0) " §8(${currentCount}/${limit})" else ""
            val msg = cfg.captureMessage
                .replace("{pokemon}", species)
                .replace("{points}", points.toString())
                .replace("{team}", team)
                .replace("{tags}", tagStr)
                .replace("{count}", countStr)
            player.sendMessage(Text.literal(msg), false)
            Columbina.LOGGER.info(
                "[Columbina] {} capture {} → +{} pts à l'équipe {}",
                player.nameForScoreboard, species, points, team
            )
        }
        Columbina.LOGGER.info("[Columbina] Listeners Cobblemon enregistrés.")
    }
}