package net.trustgames.core.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ComponentUtils {

    /**
     * Converts the Component to String.
     * Will strip any colors or events
     *
     * @param component Component to convert
     * @return String from Component
     */
    public static String stripToString(Component component){
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * Converts the Component to String.
     * Will preserve colors.
     *
     * @param component Component to convert
     * @return String from Component
     */
    public static String convertToString(Component component){
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }
}
