package net.trustgames.core.player.data;

import net.trustgames.core.Core;
import net.trustgames.core.config.player_data.PlayerDataType;

import java.util.UUID;
import java.util.function.IntConsumer;

public final class PlayerData {
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
            if (data == null){
                callback.accept(-1);
                return;
            }
            int dataInt = (Integer.parseInt(data));
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
     * Removes the amount from the total data.
     * Makes sure that the data will not be set to less than 0
     * @param decrease The amount of Data to remove from the total amount.
     */
    public void removeData(int decrease) {
        getData(data -> {
            if (decrease >= data){
                setData(0);
                return;
            }
            setData(data - decrease);
        });
    }
}
