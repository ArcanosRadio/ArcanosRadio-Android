package de.developercity.arcanosradio.services.playback.player;

public interface IPlayerBase {
    boolean play();

    boolean pause();

    boolean isBuffering();

    boolean isPlaying();

    void releasePlayer();
}
