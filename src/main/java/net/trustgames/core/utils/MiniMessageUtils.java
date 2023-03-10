package net.trustgames.core.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.trustgames.core.config.database.player_data.PlayerDataType;
import net.trustgames.core.managers.LuckPermsManager;
import org.bukkit.Bukkit;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.UUID;

public final class MiniMessageUtils {

    /**
     * MiniMessage instance, which replaces
     * various tags in the message with values of the player
     * Some tags work only for offline players or online players!
     *
     * @param uuid UUID of Player to replace the tags with info of
     * @return new MiniMessage with formatter ready
     */
    public static MiniMessage format(UUID uuid) {
        Component prefix = LuckPermsManager.getPlayerPrefix(uuid);
        if (!prefix.equals(Component.text(""))) {
            prefix = prefix.append(Component.text(" "));
        }
        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(StandardTags.defaults())
                        .resolver(TagResolver.resolver("prefix", Tag.selfClosingInserting(prefix)))
                        .resolver(TagResolver.resolver("player_name", Tag.selfClosingInserting(Component.text(Objects.requireNonNull(
                                Bukkit.getOfflinePlayer(uuid).getName())))))
                        .resolver(TagResolver.resolver("player_display_name", Tag.selfClosingInserting(Objects.requireNonNull(
                                Bukkit.getPlayer(uuid)).displayName())))
                        .build()
                )
                .build();
    }

    /**
     * MiniMessage instance, which replaces
     * the id tag in the message with the string ID
     *
     * @param id ID to replace the tag with
     * @return new MiniMessage with formatter ready
     */
    public static MiniMessage addId(String id) {
        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(StandardTags.defaults())
                        .resolver(TagResolver.resolver("int", Tag.selfClosingInserting(Component.text(id))))
                        .build()
                )
                .build();
    }

    /**
     * MiniMessage instance, which replaces
     * the sec tag in the message with the double seconds.
     * The seconds will have only one decimal number
     *
     * @param seconds double to replace the tag with
     * @return new MiniMessage with formatter ready
     */
    public static MiniMessage addSeconds(double seconds) {
        // limit the decimal number to 1.
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(1);
        df.setMinimumFractionDigits(1);

        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(StandardTags.defaults())
                        .resolver(TagResolver.resolver("sec", Tag.selfClosingInserting(Component.text(df.format(seconds)))))
                        .build()
                )
                .build();
    }

    /**
     * MiniMessage instance, which replaces
     * the player_name tag in the message with the component name
     *
     * @param name Component to replace the tag with
     * @return new MiniMessage with formatter ready
     */
    public static MiniMessage addName(Component name) {
        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(StandardTags.defaults())
                        .resolver(TagResolver.resolver("player_name", Tag.selfClosingInserting(name)))
                        .build()
                )
                .build();
    }

    /**
     * MiniMessage instance, which replaces
     * various currency tags in the message with
     * data values of the player
     *
     * @param uuid UUID of Player to replace the tags with info of
     * @return new MiniMessage with formatter ready
     */
    public static MiniMessage playerData(UUID uuid, PlayerDataType dataType, String value) {
        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(StandardTags.defaults())
                        .resolver(TagResolver.resolver("player_name", Tag.selfClosingInserting(Component.text(Objects.requireNonNull(
                                Bukkit.getOfflinePlayer(uuid).getName())))))
                        .resolver(TagResolver.resolver("player_data", Tag.selfClosingInserting(
                                Component.text(dataType.name().toLowerCase()))))
                        .resolver(TagResolver.resolver("value", Tag.selfClosingInserting(Component.text(value))))
                        .build()
                )
                .build();
    }
}
