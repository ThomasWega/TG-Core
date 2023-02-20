package net.trustgames.core.stats.level;

public class PlayerLevel {
    public int xp;
    public int level;

    public void addXp(int xp) {
        this.xp += xp;
        if (this.xp >= getThreshold(level)) {
            levelUp(1);
        }
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
        // calculate the experience required for the next level using the experience curve formula

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
