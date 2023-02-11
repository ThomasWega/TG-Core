package net.trustgames.core.disguise;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class DisguiseCommand implements CommandExecutor {
  private final DisguiseManager disguiseManager;

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] args) {
    if (!(commandSender instanceof Player player)) return true;
    if (args.length == 0) {
      player.sendMessage("/disguise [player name | clear]");
      return true;
    }

    String playerName = args[0];
    if (playerName.equalsIgnoreCase("clear")) {
      disguiseManager.deleteDisguise(player);
      player.sendMessage(ChatColor.GOLD + "Undisguised.");
      return true;
    }

    player.sendMessage(ChatColor.GOLD + "Disguising...");
    disguiseManager.loadDisguiseInfo(playerName, ((texture, signature) -> {
      if (texture == null || signature == null) {
        player.sendMessage(ChatColor.RED + "Failed to find \"" + playerName + "\"'s skin.");
        return;
      }

      disguiseManager.applyDisguise(player, playerName, texture, signature);
      player.sendMessage(ChatColor.GOLD + "Disguised as \"" + playerName + "\"!");
    }));

    return true;
  }
}
