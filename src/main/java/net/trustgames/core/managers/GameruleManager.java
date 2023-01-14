package net.trustgames.core.managers;

import net.trustgames.core.Core;
import net.trustgames.core.debug.DebugColors;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;

/**
 * Handles the gamerules of world
 */
public class GameruleManager {

    private final Core core;

    public GameruleManager(Core core) {
        this.core = core;
    }

    /**
     * Set needed gamerules to the given world
     *
     * @param worldName World to set the gamerules on
     */
    public void setGamerules(String worldName) {

        World world = Bukkit.getWorld(worldName);

        if (world != null) {
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
            world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, true);
            world.setGameRule(GameRule.MAX_COMMAND_CHAIN_LENGTH, 65536);
            world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0);
            world.setGameRule(GameRule.REDUCED_DEBUG_INFO, false);
            world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);
            world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
            world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        }
        else{
            core.getLogger().info(DebugColors.YELLOW + "Invalid world. Not settings the gamerules!");
        }
    }
}
