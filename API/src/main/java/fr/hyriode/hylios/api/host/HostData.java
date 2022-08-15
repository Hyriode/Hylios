package fr.hyriode.hylios.api.host;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by AstFaster
 * on 29/07/2022 at 19:58
 */
public class HostData {

    private final HostType type;
    private final UUID owner;
    private final String game;
    private final String gameType;

    private String name;
    private boolean whitelisted = true;
    private boolean spectatorsAllowed = true;

    private final List<UUID> whitelistedPlayers = new ArrayList<>();
    private final List<UUID> secondaryHosts = new ArrayList<>();

    public HostData(HostType type, UUID owner, String game, String gameType, String name) {
        this.type = type;
        this.owner = owner;
        this.game = game;
        this.gameType = gameType;
        this.name = name;
    }

    public HostType getType() {
        return this.type;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public String getGame() {
        return this.game;
    }

    public String getGameType() {
        return this.gameType;
    }

    public boolean isWhitelisted() {
        return this.whitelisted;
    }

    public void setWhitelisted(boolean whitelisted) {
        this.whitelisted = whitelisted;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSpectatorsAllowed() {
        return this.spectatorsAllowed;
    }

    public void setSpectatorsAllowed(boolean spectatorsAllowed) {
        this.spectatorsAllowed = spectatorsAllowed;
    }

    public List<UUID> getWhitelistedPlayers() {
        return this.whitelistedPlayers;
    }

    public void addWhitelistedPlayer(UUID playerId) {
        this.whitelistedPlayers.add(playerId);
    }

    public void removeWhitelistedPlayer(UUID playerId) {
        this.whitelistedPlayers.remove(playerId);
    }

    public List<UUID> getSecondaryHosts() {
        return this.secondaryHosts;
    }

    public void addSecondaryHost(UUID playerId) {
        this.secondaryHosts.add(playerId);
    }

    public void removeSecondaryHost(UUID playerId) {
        this.secondaryHosts.remove(playerId);
    }

}
