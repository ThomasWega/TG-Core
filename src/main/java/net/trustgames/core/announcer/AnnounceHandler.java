package net.trustgames.core.announcer;

import net.trustgames.core.Core;
import net.trustgames.core.announcer.config.AnnouncerDelayConfig;
import net.trustgames.core.announcer.config.AnnouncerMessagesConfig;

/**
 * Chat messages which are announced to all online players
 * on the server.
 */
public class AnnounceHandler {

    private final Core core;

    public AnnounceHandler(Core core) {
        this.core = core;
    }

    /**
     * Announce a set of messages every x seconds for all
     * the online players on the server. The messages can be configured
     * in the announcer.yml config
     */
    public void announceMessages() {
        AnnouncerMessagesConfig[] msgList = AnnouncerMessagesConfig.values();

        /*
         run every X seconds, every loop, it increases the index by 1, to move to the next message
         if the index is same as the number of messages, go back to the start by setting the index to 0
        */
        core.getServer().getScheduler().runTaskTimerAsynchronously(core, new Runnable() {
            int index = 0;
                    @Override
                    public void run() {
                        if (index == msgList.length){
                            index = 0;
                        }
                        core.getServer().broadcast(msgList[index].getMessage());
                        index++;
                    }
                }, AnnouncerDelayConfig.FIRST.value * 20, AnnouncerDelayConfig.DELAY.value * 20);
    }
}
