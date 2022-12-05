package net.trustgames.core.managers;

import net.trustgames.core.Core;
import net.trustgames.core.inventories.Hotbar;
import net.trustgames.core.spawn.Spawn;
import net.trustgames.core.stats.StatsListeners;

public class EventsManager{

    private final Core core;

    public EventsManager(Core core) {
        this.core = core;
    }

    public final void registerEvents(){

        // used to register all events
        core.getServer().getPluginManager().registerEvents(new Spawn(core), core);
        core.getServer().getPluginManager().registerEvents(new StatsListeners(core), core);
        core.getServer().getPluginManager().registerEvents(new Hotbar(core), core);
        core.getServer().getPluginManager().registerEvents(new HotbarManager(core), core);
    }
}
