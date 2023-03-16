package net.trustgames.core.player.data.level;

import net.trustgames.core.Core;
import net.trustgames.core.cache.PlayerDataCache;
import net.trustgames.core.player.data.config.PlayerDataType;
import net.trustgames.core.player.data.PlayerDataFetcher;

import java.util.UUID;
import java.util.function.IntConsumer;

import static net.trustgames.core.utils.LevelUtils.*;

/**
 * Additional class to PlayerData, which is used to get, calculate or modify
 * player's level by amount of xp he holds
 */
public final class PlayerLevel {
    private final PlayerDataFetcher dataFetcher;
    private final PlayerDataCache dataCache;
    private final UUID uuid;

    public PlayerLevel(Core core, UUID uuid) {
        this.uuid = uuid;
        this.dataFetcher = new PlayerDataFetcher(core, PlayerDataType.XP);
        this.dataCache = new PlayerDataCache(core, uuid, PlayerDataType.XP);
    }

    /**
     * @param levelIncrease The amount of levels to add to the current level
     */
    public void addLevel(int levelIncrease) {
        dataCache.get(currentXp -> getLevel(currentLevel -> {
            int intCurrentXp = Integer.parseInt(currentXp);
            int newLevel = currentLevel + levelIncrease;
            int newThreshold = getThreshold(newLevel);
            float progress = getProgress(intCurrentXp);
            int newXP = (int) Math.floor(newThreshold + ((getThreshold(newLevel + 1) - newThreshold) * progress));

            dataFetcher.update(uuid, newXP);
        }));
    }

    /**
     * Retrieve the player's level by getting his
     * XP and calculating the level by the XP.
     * The result is saved in the callback
     *
     * @param callback Callback where the result will be saved
     */
    public void getLevel(IntConsumer callback) {
        dataCache.get(xp -> {
            int intXp = Integer.parseInt(xp);
            int level = getLevelByXp(intXp);
            callback.accept(level);
        });
    }

    /**
     * @param targetLevel The final level the player will have
     */
    public void setLevel(int targetLevel) {
        int xpNeeded = getThreshold(targetLevel);
        dataFetcher.update(uuid, xpNeeded);
    }

    /**
     * @param levelDecrease The amount of levels to remove from the total level
     */
    public void removeLevel(int levelDecrease) {
        dataCache.get(currentXp -> getLevel(currentLevel -> {
            int intCurrentXp = Integer.parseInt(currentXp);
            int newLevel = currentLevel - levelDecrease;
            int newThreshold = getThreshold(newLevel);
            float progress = getProgress(intCurrentXp);
            int newXP = (int) Math.floor(newThreshold + ((getThreshold(newLevel + 1) - newThreshold) * progress));

            dataFetcher.update(uuid, newXP);
        }));
    }
}
