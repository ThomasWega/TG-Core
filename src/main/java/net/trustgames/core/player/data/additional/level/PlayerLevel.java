package net.trustgames.core.player.data.additional.level;

import net.trustgames.core.Core;
import net.trustgames.core.config.database.player_data.PlayerDataType;
import net.trustgames.core.player.data.PlayerData;
import net.trustgames.core.player.data.PlayerDataFetcher;

import java.util.UUID;
import java.util.function.IntConsumer;

import static net.trustgames.core.utils.LevelUtils.*;

/**
 * Additional class to PlayerData, which is used to get, calculate or modify
 * player's level by amount of xp he holds
 */
public final class PlayerLevel {
    private final PlayerDataFetcher playerDataFetcher;
    private final PlayerData playerData;

    public PlayerLevel(Core core, UUID uuid) {
        this.playerDataFetcher = new PlayerDataFetcher(core, uuid);
        this.playerData = new PlayerData(core, uuid, dataType);
    }

    private final PlayerDataType dataType = PlayerDataType.XP;

    /**
     * @param levelIncrease The amount of levels to add to the current level
     */
    public void addLevel(int levelIncrease) {
        playerData.getData(currentXp -> getLevel(currentLevel -> {
            int newLevel = currentLevel + levelIncrease;
            int newThreshold = getThreshold(newLevel);
            float progress = getProgress(currentXp);
            int newXP = (int) Math.floor(newThreshold + ((getThreshold(newLevel + 1) - newThreshold) * progress));

            playerDataFetcher.update(dataType, newXP);
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
        playerData.getData(xp -> {
            int level = getLevelByXp(xp);
            callback.accept(level);
        });
    }

    /**
     * @param targetLevel The final level the player will have
     */
    public void setLevel(int targetLevel) {
        int xpNeeded = getThreshold(targetLevel);
        playerDataFetcher.update(dataType, xpNeeded);
    }

    /**
     * @param levelDecrease The amount of levels to remove from the total level
     */
    public void removeLevel(int levelDecrease) {
        playerData.getData(currentXp -> getLevel(currentLevel -> {
            int newLevel = currentLevel - levelDecrease;
            int newThreshold = getThreshold(newLevel);
            float progress = getProgress(currentXp);
            int newXP = (int) Math.floor(newThreshold + ((getThreshold(newLevel + 1) - newThreshold) * progress));

            playerDataFetcher.update(dataType, newXP);
        }));
    }
}
