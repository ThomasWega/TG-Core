package net.trustgames.core.managers;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.trustgames.core.Core;
import net.trustgames.core.chat.ChatDecoration;
import net.trustgames.core.chat.MessageLimiter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/*
This class could be removed and the message limiter can be set with
the lowest event priority. Meaning it will be called first. The ChatDecoration
could have higher priority, meaning it will be called after, and could just check
if the event is cancelled. If it is cancelled, it shouldn't do anything.
 */

/**
 * Handles limiting of the chat and adding a prefix
 * to the players name.
 */
public class ChatManager implements Listener {

    private final MessageLimiter messageLimiter;
    private final ChatDecoration chatDecoration;


    public ChatManager(Core core) {
        messageLimiter = new MessageLimiter(core);
        chatDecoration = new ChatDecoration(core);
    }

    @EventHandler
    private void onPlayerChat(AsyncChatEvent event) {
        messageLimiter.limit(event);
        if (!event.isCancelled()) {
            chatDecoration.decorate(event);
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event){
        messageLimiter.onPlayerQuit(event);
    }
}