package net.trustgames.core.tablist;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;

public abstract class Tablist {

    @Getter
    private static final Scoreboard tablistScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
}
