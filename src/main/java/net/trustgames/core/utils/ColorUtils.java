package net.trustgames.core.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the converting of colors
 */
public class ColorUtils {

    /**
     * Translates the colors of the given string text
     * and returns the colored text. Supports both normal colors
     * and HEX colors.
     *
     * @param text Text to translate colors on
     * @return Component text with translated colors
     */
    public static Component color(@NotNull Object text){
        if (text instanceof String)
            return LegacyComponentSerializer.legacyAmpersand().deserialize(text.toString());
        else if (text instanceof Component)
            return LegacyComponentSerializer.legacyAmpersand().deserialize(
                    LegacyComponentSerializer.legacyAmpersand().serialize(
                            ((Component) text).asComponent()));
        return Component.text(ChatColor.RED + "ERROR: applying colors to text");
    }

    /**
     * Removes the color from the given Component and returns String
     * without the color
     * @param text Component to remove color from
     * @return String without color
     */
    public static String stripColor(@NotNull Component text){
        return PlainTextComponentSerializer.plainText().serialize(
                LegacyComponentSerializer.legacyAmpersand().deserialize(
                        PlainTextComponentSerializer.plainText().serialize(text)));
    }
}
