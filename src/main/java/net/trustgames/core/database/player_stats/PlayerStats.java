package net.trustgames.core.database.player_stats;

import java.sql.Timestamp;

public class PlayerStats {

    /*
    This class is just used as getters and setters for the database player_stats
     */

    private String uuid;
    private int kills;
    private int deaths;
    private int games_played;
    private int playtime;
    private double level_exp;
    private double golds;
    private double rubies;
    private Timestamp last_join;

    public PlayerStats(String uuid, int kills, int deaths, int games_played, int playtime, double level_exp, double golds, double rubies, Timestamp last_join) {
        this.uuid = uuid;
        this.kills = kills;
        this.deaths = deaths;
        this.games_played = games_played;
        this.playtime = playtime;
        this.level_exp = level_exp;
        this.golds = golds;
        this.rubies = rubies;
        this.last_join = last_join;
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

    public int getGames_played() {
        return games_played;
    }

    public void setGames_played(int games_played) {
        this.games_played = games_played;
    }

    public int getPlaytime() {
        return playtime;
    }

    public void setPlaytime(int playtime) {
        this.playtime = playtime;
    }

    public double getLevel_exp() {
        return level_exp;
    }

    public void setLevel_exp(double level_exp) {
        this.level_exp = level_exp;
    }

    public double getGolds() {
        return golds;
    }

    public void setGolds(double golds) {
        this.golds = golds;
    }

    public double getRubies() {
        return rubies;
    }

    public void setRubies(double rubies) {
        this.rubies = rubies;
    }

    public Timestamp getLast_join() {
        return last_join;
    }
}
