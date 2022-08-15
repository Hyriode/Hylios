package fr.hyriode.hylios.api.host;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hylios.api.host.packet.HostCreatePacket;

import java.util.UUID;

/**
 * Created by AstFaster
 * on 29/07/2022 at 20:13
 */
public class HostRequest {

    private HostType hostType;
    private UUID owner;
    private String game;
    private String gameType;

    public HostRequest(HostType hostType, UUID owner, String game, String gameType) {
        this.hostType = hostType;
        this.owner = owner;
        this.game = game;
        this.gameType = gameType;
    }

    public HostRequest() {}

    public HostType getHostType() {
        return this.hostType;
    }

    public HostRequest withHostType(HostType hostType) {
        this.hostType = hostType;
        return this;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public HostRequest withOwner(UUID owner) {
        this.owner = owner;
        return this;
    }

    public String getGame() {
        return this.game;
    }

    public HostRequest withGame(String game) {
        this.game = game;
        return this;
    }

    public String getGameType() {
        return this.gameType;
    }

    public HostRequest withGameType(String gameType) {
        this.gameType = gameType;
        return this;
    }

    public void send() {
        HyriAPI.get().getPubSub().send(HostAPI.CHANNEL, new HostCreatePacket(this));
    }

}
