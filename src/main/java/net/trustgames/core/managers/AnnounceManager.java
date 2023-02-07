package net.trustgames.core.managers;

import net.trustgames.core.Core;
import net.trustgames.core.config.announcer.AnnouncerMessagesConfig;

/**
 * Chat messages which are announced to all online players
 * on the server.
 */
public class AnnounceManager {

    private final Core core;

    public AnnounceManager(Core core) {
        this.core = core;
    }

    /**
     * Announce a set of messages every x seconds for all
     * the online players on the server. The messages can be configured
     * in the announcer.yml config
     */
    public void announceMessages() {
        for (AnnouncerMessagesConfig msg : AnnouncerMessagesConfig.values()) {
            core.getServer().getScheduler().runTaskLaterAsynchronously(core, () ->
                    core.getServer().broadcast(
                            msg.getMessage()), 120L);
        }
    }
}
