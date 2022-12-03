package net.trustgames.core.models;

public class PlayerStats {

    /*
    This class is just used as getters and setters for the database
     */

    private String uuid;
    private int kills;
    private int deaths;
    private double coins;

    public PlayerStats(String uuid, int kills, int deaths, double coins) {
        this.uuid = uuid;
        this.kills = kills;
        this.deaths = deaths;
        this.coins = coins;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public double getCoins() {
        return coins;
    }

    public void setCoins(double coins) {
        this.coins = coins;
    }
}
