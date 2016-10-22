package de.developercity.arcanosradio.services.playback.player;

public abstract class PlayerFactory {
    public static IPlayer createPlayer(IPlayerBaseDelegate delegate) {
        return new Player(delegate);
    }
}
