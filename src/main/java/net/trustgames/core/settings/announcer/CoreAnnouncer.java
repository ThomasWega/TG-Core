package net.trustgames.core.settings.announcer;

public enum CoreAnnouncer {
    DELAY(120L),
    MESSAGE_WEBSITE(
            "<newline><yellow>1111</yellow>"
            + "<hover:show_text:'<green>CLICK TO OPEN</green>'><gold><bold><click:open_url:'http://www.trustgames.net'>OPEN URL</gold><bold></hover> non CLICK"
            + "<yellow>1111</yellow>"
            + "<newline>"
    ),
    MESSAGE_STORE(
            "<newline><yellow>1111</yellow>"
                    + "<hover:show_text:'<green>CLICK TO OPEN</green>'><gold><bold><click:open_url:'http://www.trustgames.net'>OPEN URL</gold><bold></hover> non CLICK"
                    + "<yellow>1111</yellow>"
                    + "<newline>"
    );

    private final Object value;

    CoreAnnouncer(Object value) {
        this.value = value;
    }
    public String getMessage() {
        return value.toString();
    }

    public long getDelay(){
        return Long.parseLong(value.toString());
    }
}
