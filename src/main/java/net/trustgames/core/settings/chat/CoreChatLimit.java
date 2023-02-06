package net.trustgames.core.settings.chat;

public enum CoreChatLimit {
    TITAN(0.1d, 10d),
    LORD(3d, 25d),
    KNIGHT(5d, 45d),
    PRIME(10d, 60d),
    DEFAULT(15d, 120d);

    private final double chatLimitSec;
    private final double chatLimitSameSec;

    CoreChatLimit(double chatLimitSec, double chatLimitSameSec) {
        this.chatLimitSec = chatLimitSec;
        this.chatLimitSameSec = chatLimitSameSec;
    }

    public double getChatLimitSec() {
        return chatLimitSec;
    }

    public double getChatLimitSameSec() {
        return chatLimitSameSec;
    }
}
