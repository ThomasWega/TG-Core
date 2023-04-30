package net.trustgames.core.command;

import net.trustgames.toolkit.config.CommandConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static net.trustgames.core.Core.LOGGER;

/**
 * Custom command implementation, which checks for the supplied permission and by default
 * disables execution by console.
 */
public abstract class TrustCommand implements CommandExecutor {

    private final String permission;

    /**
     * Instantiates a new Trust command.
     *
     * @param permission the permission
     */
    public TrustCommand(@Nullable String permission) {
        this.permission = permission;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Check for @AllowConsole annotation
        boolean consoleAllowed = false;
        try {
            Method executeMethod = getClass().getMethod("execute", CommandSender.class, String[].class, String.class);
            AllowConsole allowConsoleAnnotation = executeMethod.getAnnotation(AllowConsole.class);
            if (allowConsoleAnnotation != null) {
                consoleAllowed = true;
            }
        } catch (NoSuchMethodException ignored) {
        }


        // If not allowed and sender is not a player, send error message
        if (!consoleAllowed && !(sender instanceof Player)) {
            LOGGER.warning(CommandConfig.COMMAND_PLAYER_ONLY.value.toString());
            return true;
        }

        // check for permission
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(CommandConfig.COMMAND_NO_PERM.getText());
            return true;
        }

        execute(sender, command, label, args);
        return true;

    }

    /**
     * This method is called on command execution.
     * Annotation can be used on this method
     *
     * @param sender  The Console/Player who sent the command
     * @param command the command
     * @param label   Alias of the command used
     * @param args    What arguments the command had
     */
    public abstract void execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args);

    /**
     * The interface Allow console.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface AllowConsole {
    }
}

