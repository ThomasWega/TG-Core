package net.trustgames.core.utils;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;

public class NMSUtils {

    /**
     * Convert JSON to IChatBaseComponent
     *
     * @param json JSONElement to convert
     * @return IChatBaseComponent converted from JSON
     */
    public static Component convertFromJSON(JsonElement json) {
        return Component.Serializer.fromJson(json);
    }
}
