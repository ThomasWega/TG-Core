package net.trustgames.core.managers;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trustgames.core.Core;
import net.trustgames.core.settings.announcer.CoreAnnouncer;

/**
 * Chat messages which are announced to all online players
 * on the server.
 */
public class AnnouncerManager {

    private final Core core;

    public AnnouncerManager(Core core) {
        this.core = core;
    }

    /**
     * Announce a set of messages every x seconds for all
     * the online players on the server. The messages can be configured
     * in the announcer.yml config
     */
    public void announceMessages() {
        for (CoreAnnouncer msg : CoreAnnouncer.values()) {
            core.getServer().getScheduler().runTaskLaterAsynchronously(core, () ->
                    core.getServer().broadcast(
                            MiniMessage.miniMessage().deserialize(msg.getMessage())), CoreAnnouncer.DELAY.getDelay());
        }
    }
}
