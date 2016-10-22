package de.developercity.arcanosradio.services.playback.player;

public interface IPlayer extends IPlayerBase {
    int getState();

    void setVolume(float left, float right);
}
