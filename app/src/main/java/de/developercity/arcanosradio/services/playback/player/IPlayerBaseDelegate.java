package de.developercity.arcanosradio.services.playback.player;

public interface IPlayerBaseDelegate {
    void onPlayerPlay();

    void onPlayerBuffer();

    void onPlayerPause();

    void onPlaybackInterrupted();

    void onPlaybackRestored();
}
