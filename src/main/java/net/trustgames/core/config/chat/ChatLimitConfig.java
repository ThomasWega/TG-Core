package net.trustgames.core.config.chat;

public enum ChatLimitConfig {
    TITAN(0.1d, 10d),
    LORD(3d, 25d),
    KNIGHT(5d, 45d),
    PRIME(10d, 60d),
    DEFAULT(15d, 120d);

    private final double chatLimitSec;
    private final double chatLimitSameSec;

    ChatLimitConfig(double chatLimitSec, double chatLimitSameSec) {
        this.chatLimitSec = chatLimitSec;
        this.chatLimitSameSec = chatLimitSameSec;
    }

    /**
     * @return Chat message limit of the message for the enum rank
     */
    public double getChatLimitSec() {
        return chatLimitSec;
    }

    /**
     * @return Same chat message limit of the message for the enum rank
     */
    public double getChatLimitSameSec() {
        return chatLimitSameSec;
    }
}
