package net.trustgames.core.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
    public static Component colorString(@NotNull String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    /**
     * Translates the colors of the given string text
     * and returns the colored text. Supports both normal colors
     * and HEX colors.
     *
     * @param text Text to translate colors on
     * @return String text with translated colors
     */
    public static String colorComponent(@NotNull Component text){
        return LegacyComponentSerializer.legacyAmpersand().serialize(text);
    }

    public static String stripColor(@NotNull Component text){
        return PlainTextComponentSerializer.plainText().serialize(
                LegacyComponentSerializer.legacyAmpersand().deserialize(
                        PlainTextComponentSerializer.plainText().serialize(text)));
    }
}
