package net.trustgames.core.managers;

import net.trustgames.core.Core;
import net.trustgames.core.database.player_activity.ActivityListener;
import net.trustgames.core.inventories.HotbarListeners;
import net.trustgames.core.spawn.Spawn;

public class EventsManager {

    private final Core core;

    public EventsManager(Core core) {
        this.core = core;
    }

    // used to register all events
    public final void registerEvents() {

        // spawn
        core.getServer().getPluginManager().registerEvents(new Spawn(core), core);

        // hotbar
        core.getServer().getPluginManager().registerEvents(new HotbarListeners(core), core);

        // database
        core.getServer().getPluginManager().registerEvents(new ActivityListener(core), core);
    }
}
