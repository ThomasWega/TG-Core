package net.trustgames.core.utils;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ComponentUtils {

    /**
     * Converts the Component to String.
     * Will preserve color codes, but no events
     *
     * @param component Component to convert
     * @return String from Component with unformatted color codes and no events
     */
    public static String toString(Component component){
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }

    /**
     * Convert component to JSON
     * Will preserve colors and events
     *
     * @param component Component to convert
     * @return JSONElement from Component
     */
    public static JsonElement toJson(Component component){
        return GsonComponentSerializer.gson().serializeToTree(component);
    }
}
