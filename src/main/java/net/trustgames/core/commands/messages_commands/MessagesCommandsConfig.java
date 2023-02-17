package net.trustgames.core.commands.messages_commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum MessagesCommandsConfig {
    WARN_MESSAGES_LIMIT_SEC(0.5d),
    WEBSITE("<newline>" +
            "<color:#5757cf>You can visit our website by clicking </color><hover:show_text:'<yellow>Click to join</yellow>'><click:open_url:'http://www.trustgames.net'><color:#ffda73>HERE</color></hover>" +
            "<newline>"
    ),
    STORE("<newline>" +
            "<color:#cf9117>You can check benefits on our store by clicking </color><hover:show_text:'<yellow>Click to join</yellow>'><click:open_url:'http://discord.trustgames.net'><color:#ffda73>HERE</color></hover>" +
            "<newline>"
    ),
    DISCORD("<newline>" +
            "<color:#99a3ff>You can join our discord server by clicking </color><hover:show_text:'<yellow>Click to join</yellow>'><click:open_url:'http://discord.trustgames.net'><color:#ffda73>HERE</color></hover>" +
            "<newline>"
    );

    private final Object value;

    MessagesCommandsConfig(Object value) {
        this.value = value;
    }

    /**
     * @return double value of the enum
     */
    public double getDouble() {
        return (double) value;
    }

    /**
     * @return Formatted component message
     */
    public Component getMessage() {
        return MiniMessage.miniMessage().deserialize(value.toString());
    }
}
