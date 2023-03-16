package net.trustgames.core.player.data;

import net.trustgames.core.Core;
import net.trustgames.core.cache.UUIDCache;
import net.trustgames.core.player.data.config.PlayerDataType;

import java.util.UUID;

public final class PlayerData {
    private final PlayerDataFetcher dataFetcher;
    private final UUID uuid;

    public PlayerData(Core core, UUID uuid, PlayerDataType dataType) {
        if (dataType == PlayerDataType.UUID) {
            throw new RuntimeException(this.getClass().getName() + " can't be used to retrieve UUID. " +
                    "Use the " + UUIDCache.class.getName() + " instead!");
        }
        this.uuid = uuid;
        this.dataFetcher = new PlayerDataFetcher(core, dataType);
    }

    /**
     * Add Data to the player and update it in the database
     *
     * @param increase Amount of Data to add to the current amount
     */
    public void addData(int increase) {
        dataFetcher.fetch(uuid, data -> {
            data += increase;
            dataFetcher.update(uuid, data);
        });
    }

    /**
     * @param target The final amount of Data the player will have
     */
    public void setData(int target) {
        dataFetcher.update(uuid, target);
    }

    /**
     * Removes the amount from the total data.
     * Makes sure that the data will not be set to less than 0
     *
     * @param decrease The amount of Data to remove from the total amount.
     */
    public void removeData(int decrease) {
        dataFetcher.fetch(uuid, data -> {
            int intData = Integer.parseInt(data);
            if (decrease >= intData) {
                setData(0);
                return;
            }
            setData(intData - decrease);
        });
    }
}
