package net.trustgames.core.player.data.level;

import net.trustgames.core.Core;

import java.util.UUID;

public class PlayerLevel {
    private final UUID uuid;
    private final PlayerLevelFetcher playerLevelFetcher;

    public void addXp(UUID uuid, int xpIncrease) {
        int xp = getXp();
        xp += xpIncrease;

        playerLevelFetcher.update(uuid, xp);
    }

    public int getXp() {
        return playerLevelFetcher.fetch(uuid);
    }

    public void setXp(int targetXp) {
        playerLevelFetcher.update(uuid, targetXp);
    }

    public void removeXp(int xpDecrease) {
        setXp(getXp() - xpDecrease);
    }

    public void addLevel(int levelIncrease) {
        int currentXp = getXp();
        int currentLevel = getLevel();
        int newLevel = currentLevel + levelIncrease;
        int newThreshold = getThreshold(newLevel);
        float progress = getProgress(currentXp);
        int newXP = (int) Math.floor(newThreshold + ((getThreshold(newLevel + 1) - newThreshold) * progress));

        playerLevelFetcher.update(uuid, newXP);
    }

    public int getLevelByXp(int xp) {
        int level = 1;
        while (xp >= getThreshold(level)) {
            level++;
        }
        return level - 1;
    }

    public int getLevel() {
        return getLevelByXp(getXp());
    }

    public void setLevel(int targetLevel) {
        int xpNeeded = getThreshold(targetLevel);
        playerLevelFetcher.update(uuid, xpNeeded);
    }

    public void removeLevel(int levelDecrease) {
        int currentXp = getXp();
        int currentLevel = getLevel();
        int newLevel = currentLevel - levelDecrease;
        int newThreshold = getThreshold(newLevel);
        float progress = getProgress(currentXp);
        int newXP = (int) Math.floor(newThreshold + ((getThreshold(newLevel + 1) - newThreshold) * progress));

        playerLevelFetcher.update(uuid, newXP);
    }

    public PlayerLevel(Core core, UUID uuid) {
        this.uuid = uuid;
        this.playerLevelFetcher = new PlayerLevelFetcher(core);
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
