package net.trustgames.core.command;

import net.kyori.adventure.text.Component;
import net.trustgames.core.config.CommandConfig;
import net.trustgames.core.logger.CoreLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * Custom command implementation, which checks for the supplied permission and by default
 * disables execution by console.
 */
public abstract class TrustCommand implements CommandExecutor {

    private final String permission;
    private final Component message;

    public TrustCommand(String permission) {
        this(permission, null);
    }

    public TrustCommand(String permission, Component message) {
        this.permission = permission;
        this.message = message;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        // Check for @AllowConsole annotation
        boolean consoleAllowed;
        try {
            Method executeMethod = getClass().getMethod("execute", Player.class, String[].class);
            consoleAllowed = executeMethod.isAnnotationPresent(AllowConsole.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // If not allowed and sender is not a player, send error message
        if (!consoleAllowed && !(sender instanceof Player)) {
            CoreLogger.LOGGER.warning(CommandConfig.COMMAND_ONLY_PLAYER.value.toString());
            return true;
        }

        // check for permission
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(CommandConfig.COMMAND_NO_PERM.getText());
            return true;
        }

        execute(sender, args);
        return true;
    }

    /**
     * This method is called on command execution.
     * Annotation can be used on this method
     *
     * @param sender The Console/Player who sent the command
     * @param args What arguments the command had
     */
    public abstract void execute(CommandSender sender, String[] args);

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface AllowConsole {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ArgsSize {
    }
}

