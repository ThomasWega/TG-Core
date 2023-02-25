package net.trustgames.core.player.data.level;

import net.trustgames.core.Core;

import java.util.UUID;
import java.util.function.IntConsumer;

public class PlayerLevel {
    private final UUID uuid;
    private final PlayerLevelFetcher playerLevelFetcher;

    public PlayerLevel(Core core, UUID uuid) {
        this.uuid = uuid;
        this.playerLevelFetcher = new PlayerLevelFetcher(core);
    }

    public void addXp(int xpIncrease) {
        getXp(xp -> {
            xp += xpIncrease;
            playerLevelFetcher.update(uuid, xp);
        });
    }

    public void getXp(IntConsumer callback) {
        playerLevelFetcher.fetch(uuid, callback);
    }

    public void setXp(int targetXp) {
        playerLevelFetcher.update(uuid, targetXp);
    }

    public void removeXp(int xpDecrease) {
        getXp(xp -> setXp(xp - xpDecrease));
    }

    public void addLevel(int levelIncrease) {
        getXp(currentXp -> getLevel(currentLevel -> {
            int newLevel = currentLevel + levelIncrease;
            int newThreshold = getThreshold(newLevel);
            float progress = getProgress(currentXp);
            int newXP = (int) Math.floor(newThreshold + ((getThreshold(newLevel + 1) - newThreshold) * progress));

            playerLevelFetcher.update(uuid, newXP);
        }));
    }

    public int getLevelByXp(int xp) {
        int level = 1;
        while (xp >= getThreshold(level)) {
            level++;
        }
        return level - 1;
    }

    public void getLevel(IntConsumer callback) {
        getXp(xp -> {
            int level = getLevelByXp(xp);
            callback.accept(level);
        });
    }

    public void setLevel(int targetLevel) {
        int xpNeeded = getThreshold(targetLevel);
        playerLevelFetcher.update(uuid, xpNeeded);
    }

    public void removeLevel(int levelDecrease) {
        getXp(currentXp -> getLevel(currentLevel -> {
            int newLevel = currentLevel - levelDecrease;
            int newThreshold = getThreshold(newLevel);
            float progress = getProgress(currentXp);
            int newXP = (int) Math.floor(newThreshold + ((getThreshold(newLevel + 1) - newThreshold) * progress));

            playerLevelFetcher.update(uuid, newXP);
        }));
    }

    public int getThreshold(int level) {
        // return the experience required for the next level
        // the base amount of experience required for level 1
        int base = 100;
        // the exponent that determines the experience curve
        double exponent = 1.2;
        return (int) Math.floor(base * Math.pow(level, exponent));
    }

    public float getProgress(int xp) {
        int currentLevelThreshold = getThreshold(getLevelByXp(xp));
        int nextLevelThreshold = getThreshold(getLevelByXp(xp) + 1);

        return (float) (xp - currentLevelThreshold) / (float) (nextLevelThreshold - currentLevelThreshold);
    }
}
