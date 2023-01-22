package net.trustgames.core.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the converting of colors
 */
public class ColorManager {

    /**
     * Translates the colors of the given text
     * and returns the colored text. Supports both normal colors
     * and HEX colors.
     *
     * @param text Text to translate colors on
     * @return Text with translated colors
     */
    public static Component color(@NotNull String text) {
        return LegacyComponentSerializer.legacy('&').deserialize(text);
    }
}
