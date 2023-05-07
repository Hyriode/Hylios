package fr.hyriode.hylios.metrics.data.players;

public enum PlayerRank {

    /** Default ranks */
    PLAYER("player"),
    VIP("vip"),
    VIP_PLUS("vip+"),
    EPIC("epic"),

    /** Content creator */
    PARTNER("partner");

    private final String name;

    PlayerRank(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
