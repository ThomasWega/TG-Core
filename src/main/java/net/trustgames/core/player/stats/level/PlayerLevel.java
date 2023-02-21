package net.trustgames.core.player.stats.level;

import net.trustgames.core.Core;

import java.util.UUID;

public class PlayerLevel {
    private int xp;
    private int level;
    private final UUID uuid;
    private final PlayerLevelFetcher playerLevelFetcher;

    public int getXp() {
        return this.xp;
    }

    public void setXp(int xp) {
        playerLevelFetcher.update(uuid, xp);
        this.xp = xp;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        int xpNeeded = getThreshold(level);
        playerLevelFetcher.update(uuid, xpNeeded);
        this.level = level;
    }

    public PlayerLevel(Core core, UUID uuid) {
        this.uuid = uuid;
        this.playerLevelFetcher = new PlayerLevelFetcher(core);
        this.xp = playerLevelFetcher.fetch(uuid);
        this.level = getLevel(xp);
    }

    public void addXp(UUID uuid, int xp) {
        this.xp += xp;

        if (this.xp >= getThreshold(this.level)) {
            levelUp(1);
        }

        playerLevelFetcher.update(uuid, xp);
    }

    public void levelUp(int value) {
        this.level = level + value;
    }

    public int getLevel(int xp) {
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
        int currentLevelThreshold = getThreshold(getLevel(xp));
        int nextLevelThreshold = getThreshold(getLevel(xp) + 1);

        System.out.println(nextLevelThreshold);

        return (float) (xp - currentLevelThreshold) / (float) (nextLevelThreshold - currentLevelThreshold);
    }
}
