package net.trustgames.core.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.trustgames.core.skin.SkinData;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Handles getting the skin texture and signature from the mojang servers
 * and also caching it to save mojang api calls.
 */
public class SkinManager {

    /**
     * Cache that holds the SkinData. It expires after 3 days.
     */
    private static final LoadingCache<String, SkinData> skinCache = CacheBuilder.newBuilder()
            .expireAfterWrite(3, TimeUnit.DAYS)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull SkinData load(@NotNull String playerName) {
                    return fetchSkin(playerName);
                }
            });


    /**
     * Get the data of the skin
     *
     * @param playerName Name of the player with the skin
     * @return Data of the player's skin
     */
    public static SkinData getSkin(String playerName) {
        try{
            return (skinCache.get(playerName));
        } catch (ExecutionException e){
            return new SkinData("", "");
        }
    }

    /**
     * Used to retrieve the skin data from the mojang servers.
     * Note that there is a limit for the api calls
     */
    private static SkinData fetchSkin(String playerName) {
        try {
            // get the UUID of the player by his name
            HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://api.mojang.com/users/profiles/minecraft/%s", playerName)).openConnection();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                ArrayList<String> lines = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                reader.lines().forEach(lines::add);

                String reply = String.join(" ", lines);
                String uuid = reply.substring(7, 39);

                // get the skin data by player's UUID
                connection = (HttpsURLConnection) new URL(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s", uuid)).openConnection();
                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    lines = new ArrayList<>();
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    reader.lines().forEach(lines::add);

                    reply = String.join(" ", lines);
                    int indexOfValue = reply.indexOf("\"value\": \"");
                    int indexOfSignature = reply.indexOf("\"signature\": \"");
                    String texture = reply.substring(indexOfValue + 10, reply.indexOf("\"", indexOfValue + 10));
                    String signature = reply.substring(indexOfSignature + 14, reply.indexOf("\"", indexOfSignature + 14));

                    return new SkinData(texture, signature);
                }
                else {
                    Bukkit.getConsoleSender().sendMessage("Connection could not be opened when fetching player skin (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
                }
            }
            else {
                Bukkit.getConsoleSender().sendMessage("Connection could not be opened when fetching player profile (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SkinData("", "");
    }
}