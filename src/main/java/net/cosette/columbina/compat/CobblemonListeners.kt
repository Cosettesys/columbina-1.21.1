package net.cosette.columbina.compat

import com.cobblemon.mod.common.api.events.CobblemonEvents
import net.cosette.columbina.Columbina
import net.cosette.columbina.ColumbinaConfig
import net.cosette.columbina.team.TeamManager
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
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
            val currentCount: Int
            if (limit > 0) {
                val overworld = server?.overworld ?: return@subscribe
                val data = CaptureCountSavedData.get(overworld)
                val count = data.getCount(player.uuid, species)
                if (count >= limit) return@subscribe
                data.increment(player.uuid, species)
                currentCount = count + 1
            } else {
                currentCount = 0
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
        CobblemonEvents.RIDE_EVENT_PRE.subscribe { event ->
            val player = CobblemonEventWrapper.getPlayerRIDE(event) ?: return@subscribe
            val cfg = ColumbinaConfig.getInstance()
            if (cfg.poketopiaRideAllowed) return@subscribe
            val dimensionValue = player.world.registryKey.value
            if (dimensionValue.namespace == "columbina" && dimensionValue.path == "poketopia") {
                event.cancel()
                player.sendMessage(Text.literal("§cVous ne pouvez pas monter un Pokémon dans cette dimension."), false)
            }
        }
        Columbina.LOGGER.info("[Columbina] Listeners Cobblemon enregistrés.")
    }
}