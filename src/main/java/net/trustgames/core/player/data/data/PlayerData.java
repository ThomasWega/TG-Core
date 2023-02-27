package net.trustgames.core.player.data.data;

import net.trustgames.core.Core;
import net.trustgames.core.config.database.player_data.PlayerDataType;
import net.trustgames.core.player.data.PlayerDataFetcher;

import java.util.UUID;
import java.util.function.IntConsumer;

public class PlayerData {
    private final PlayerDataFetcher playerDataFetcher;
    private final PlayerDataType dataType;

    public PlayerData(Core core, UUID uuid, PlayerDataType dataType) {
        this.playerDataFetcher = new PlayerDataFetcher(core, uuid);
        this.dataType = dataType;
    }

    /**
     * Add Data to the player and update it in the database
     *
     * @param increase Amount of Data to add to the current amount
     */
    public void addData(int increase) {
        getData(data -> {
            data += increase;
            playerDataFetcher.update(dataType, data);
        });
    }

    /**
     * Retrieve Data from the database and save the result in the callback
     *
     * @param callback Callback where the result will be saved
     */
    public void getData(IntConsumer callback) {
        playerDataFetcher.fetch(dataType, data -> {
            int dataInt = ((int) data);
            callback.accept(dataInt);
        });
    }

    /**
     * @param target The final amount of Data the player will have
     */
    public void setData(int target) {
        playerDataFetcher.update(dataType, target);
    }

    /**
     * @param decrease The amount of Data to remove from the total amount
     */
    public void removeData(int decrease) {
        getData(data -> setData(data - decrease));
    }
}
