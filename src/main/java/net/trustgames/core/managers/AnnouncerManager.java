package net.trustgames.core.managers;

import net.trustgames.core.Core;
import net.trustgames.core.config.announcer.AnnouncerConfig;

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
        for (AnnouncerConfig msg : AnnouncerConfig.values()) {
            core.getServer().getScheduler().runTaskLaterAsynchronously(core, () ->
                    core.getServer().broadcast(
                            msg.getMessage()), AnnouncerConfig.DELAY.getDelay());
        }
    }
}
