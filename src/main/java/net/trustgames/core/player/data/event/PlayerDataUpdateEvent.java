package net.trustgames.core.player.data.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerDataUpdateEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final UUID uuid;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public PlayerDataUpdateEvent(UUID uuid){
        this.uuid = uuid;
    }
}
