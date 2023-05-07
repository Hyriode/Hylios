package fr.hyriode.hylios.api;

public enum HyliosMetricsRedisKey {

    HYRIS("money:hyris"),
    HYODES("money:hyodes"),

    HYRI_PLUS("ranks:hyriplus"),

    REGISTERED_PLAYERS("players:registered"),

    HYRI_API_PACKETS("packets:hyriapi"),
    HYGGDRASIL_PACKETS("packets:hyggdrasil");

    private final String key;

    HyliosMetricsRedisKey(String key) {
        this.key = "hyreos:metrics:" + key;
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public String toString() {
        return this.key;
    }
}
