package net.trustgames.core.settings.tablist;

public enum CoreTablist {
    TABLIST_HEADER("&e&lTRUSTGAMES &f- &7Chillin' on the hub"),
    TABLIST_FOOTER("&astore.trustgames.net");

    private final String value;

    CoreTablist(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
