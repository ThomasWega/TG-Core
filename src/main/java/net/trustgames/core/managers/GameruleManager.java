package net.trustgames.core.managers;

import net.trustgames.core.Core;
import net.trustgames.core.debug.DebugColors;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;

import java.util.Objects;

public class GameruleManager {

    private final Core core;

    public GameruleManager(Core core) {
        this.core = core;
    }

    public void setGamerules() {
        World world = Bukkit.getWorld("world");

        if (world != null) {
            // lobby settings
            if (Objects.requireNonNull(core.getConfig().getString("settings.server-type")).equalsIgnoreCase("LOBBY")) {

                world.setDifficulty(Difficulty.PEACEFUL);

                // Gamerules
                world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
                world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
                world.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
                world.setGameRule(GameRule.DISABLE_RAIDS, true);
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
                world.setGameRule(GameRule.DO_FIRE_TICK, false);
                world.setGameRule(GameRule.DO_INSOMNIA, false);
                world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
                world.setGameRule(GameRule.DO_MOB_LOOT, false);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
                world.setGameRule(GameRule.DO_TILE_DROPS, false);
                world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
                world.setGameRule(GameRule.DROWNING_DAMAGE, false);
                world.setGameRule(GameRule.FALL_DAMAGE, false);
                world.setGameRule(GameRule.FIRE_DAMAGE, false);
                world.setGameRule(GameRule.FORGIVE_DEAD_PLAYERS, false);
                world.setGameRule(GameRule.FREEZE_DAMAGE, false);
                world.setGameRule(GameRule.KEEP_INVENTORY, false);
                world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, true);
                world.setGameRule(GameRule.MAX_COMMAND_CHAIN_LENGTH, 65536);
                world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0);
                world.setGameRule(GameRule.MOB_GRIEFING, false);
                world.setGameRule(GameRule.NATURAL_REGENERATION, true);
                world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 100);
                world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
                world.setGameRule(GameRule.REDUCED_DEBUG_INFO, true);
                world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);
                world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
                world.setGameRule(GameRule.SPAWN_RADIUS, 1);
                world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
                world.setGameRule(GameRule.UNIVERSAL_ANGER, false);

                // game settings
            } else if (Objects.requireNonNull(core.getConfig().getString("settings.server-type")).equalsIgnoreCase("GAME")) {
                // TODO game gamerules
            } else {
                core.getLogger().info(DebugColors.RED + "Invalid server-type in config.yml!");
            }
        }
    }
    // game settings
}