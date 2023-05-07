package fr.hyriode.hylios.api;

public enum MetricsRedisKey {

    HYRIS("money:hyris"),
    HYODES("money:hyodes"),

    RANKS("ranks:%s"),
    HYRI_PLUS("ranks:hyriplus"),

    REGISTERED_PLAYERS("players:registered"),

    HYRIAPI_PACKETS("packets:hyriapi"),
    HYGGDRASIL_PACKETS("packets:hyggdrasil");

    private final String key;

    MetricsRedisKey(String key) {
        this.key = "hylios:metrics:" + key;
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public String toString() {
        return this.key;
    }
}
