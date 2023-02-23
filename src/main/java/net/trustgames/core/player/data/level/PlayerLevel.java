package net.trustgames.core.player.data.level;

import net.trustgames.core.Core;

import java.util.UUID;

public class PlayerLevel {
    private final UUID uuid;
    private final PlayerLevelFetcher playerLevelFetcher;

    public int getXp() {
        return playerLevelFetcher.fetch(uuid);
    }

    public void setXp(int xp) {
        playerLevelFetcher.update(uuid, xp);
    }

    public int getLevel() {
        return getLevelByXp(getXp());
    }

    public void setLevel(int level) {
        int xpNeeded = getThreshold(level);
        playerLevelFetcher.update(uuid, xpNeeded);
    }

    public PlayerLevel(Core core, UUID uuid) {
        this.uuid = uuid;
        this.playerLevelFetcher = new PlayerLevelFetcher(core);
    }

    public void addXp(UUID uuid, int xpIncrease) {
        int xp = getXp();
        xp += xpIncrease;
        int level = getLevelByXp(xp);

        if (xp >= getThreshold(level)) {
            levelUp(1);
        }

        playerLevelFetcher.update(uuid, xp);
    }

    public void levelUp(int value) {
        int level = getLevelByXp(getXp());
        level = level + value;
        int xp = getThreshold(level);
        playerLevelFetcher.update(uuid, xp);
    }

    public int getLevelByXp(int xp) {
        int level = 1;
        while (xp >= getThreshold(level)) {
            level++;
        }
        return level - 1;
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
