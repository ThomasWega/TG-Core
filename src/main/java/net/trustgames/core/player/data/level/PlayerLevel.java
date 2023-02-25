package net.trustgames.core.player.data.level;

import net.trustgames.core.Core;
import net.trustgames.core.config.database.player_data.PlayerDataTypes;
import net.trustgames.core.player.data.PlayerDataFetcher;

import java.util.UUID;
import java.util.function.IntConsumer;

public class PlayerLevel {
    private final PlayerDataFetcher playerDataFetcher;

    public PlayerLevel(Core core, UUID uuid) {
        this.playerDataFetcher = new PlayerDataFetcher(core, uuid);
    }

    /**
     * Add the xp to the player and update it in the database
     *
     * @param xpIncrease Amount of XP to add to the current XP amount
     */
    public void addXp(int xpIncrease) {
        getXp(xp -> {
            xp += xpIncrease;
            playerDataFetcher.update(PlayerDataTypes.LEVEL_EXP, xp);
        });
    }

    /**
     * Retrieve the XP from the database and save the result in the callback
     *
     * @param callback Callback where the result will be saved
     */
    public void getXp(IntConsumer callback) {
        playerDataFetcher.fetch(PlayerDataTypes.LEVEL_EXP, xp -> {
            int xpInt = ((int) xp);
            callback.accept(xpInt);
        });
    }

    /**
     * @param targetXp The final amount of XP the player will have
     */
    public void setXp(int targetXp) {
        playerDataFetcher.update(PlayerDataTypes.LEVEL_EXP, targetXp);
    }

    /**
     * @param xpDecrease The amount of XP to remove from the total XP
     */
    public void removeXp(int xpDecrease) {
        getXp(xp -> setXp(xp - xpDecrease));
    }

    /**
     * @param levelIncrease The amount of levels to add to the current level
     */
    public void addLevel(int levelIncrease) {
        getXp(currentXp -> getLevel(currentLevel -> {
            int newLevel = currentLevel + levelIncrease;
            int newThreshold = getThreshold(newLevel);
            float progress = getProgress(currentXp);
            int newXP = (int) Math.floor(newThreshold + ((getThreshold(newLevel + 1) - newThreshold) * progress));

            playerDataFetcher.update(PlayerDataTypes.LEVEL_EXP, newXP);
        }));
    }

    /**
     * Loops through all the levels and checks
     * if the threshold is smaller. If false, that's
     * the current level
     *
     * @param xp Amount of XP to check the level for
     * @return The level the XP reaches
     */
    public int getLevelByXp(int xp) {
        int level = 1;
        while (xp >= getThreshold(level)) {
            level++;
        }
        return level - 1;
    }

    /**
     * Retrieve the player's level by getting his
     * XP and calculating the level by the XP.
     * The result is saved in the callback
     *
     * @param callback Callback where the result will be saved
     */
    public void getLevel(IntConsumer callback) {
        getXp(xp -> {
            int level = getLevelByXp(xp);
            callback.accept(level);
        });
    }

    /**
     * @param targetLevel The final level the player will have
     */
    public void setLevel(int targetLevel) {
        int xpNeeded = getThreshold(targetLevel);
        playerDataFetcher.update(PlayerDataTypes.LEVEL_EXP, xpNeeded);
    }

    /**
     * @param levelDecrease The amount of levels to remove from the total level
     */
    public void removeLevel(int levelDecrease) {
        getXp(currentXp -> getLevel(currentLevel -> {
            int newLevel = currentLevel - levelDecrease;
            int newThreshold = getThreshold(newLevel);
            float progress = getProgress(currentXp);
            int newXP = (int) Math.floor(newThreshold + ((getThreshold(newLevel + 1) - newThreshold) * progress));

            playerDataFetcher.update(PlayerDataTypes.LEVEL_EXP, newXP);
        }));
    }

    /**
     * Calculate the needed xp to reach the given level
     * using a formula.
     *
     * @param level Level to get the threshold for
     * @return Needed XP to reach the level
     */
    public int getThreshold(int level) {
        // return the experience required for the next level
        // the base amount of experience required for level 1
        int base = 100;
        // the exponent that determines the experience curve
        double exponent = 1.2;
        return (int) Math.floor(base * Math.pow(level, exponent));
    }

    /**
     * Get the progress from 0.0 to 1.0 that
     * the player has towards the next level.
     * The given XP will be converted to current level
     * and then a percentage to reach the next threshold
     * will be calculated.
     *
     * @param xp XP to get the progress for
     * @return 0.0 to 1.0 progress to next level
     */
    public float getProgress(int xp) {
        int currentLevelThreshold = getThreshold(getLevelByXp(xp));
        int nextLevelThreshold = getThreshold(getLevelByXp(xp) + 1);

        return (float) (xp - currentLevelThreshold) / (float) (nextLevelThreshold - currentLevelThreshold);
    }
}
