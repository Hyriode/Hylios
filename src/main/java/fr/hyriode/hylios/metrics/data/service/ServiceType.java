package fr.hyriode.hylios.metrics.data.service;

public enum ServiceType {

    LIMBO("limbo"),
    PROXY("proxy"),
    SERVER("server");

    private final String name;

    ServiceType(String name) {
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
