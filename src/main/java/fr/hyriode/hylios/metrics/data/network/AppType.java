package fr.hyriode.hylios.metrics.data.network;

public enum AppType {

    HYRIAPI("hyriapi"),
    HYGGDRASIL("hyggdrasil");

    private final String name;

    AppType(String name) {
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
