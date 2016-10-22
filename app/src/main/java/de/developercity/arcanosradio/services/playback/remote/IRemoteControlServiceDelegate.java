package de.developercity.arcanosradio.services.playback.remote;

public interface IRemoteControlServiceDelegate {
    void onRemotePlay();

    void onRemotePause();

    void onRemoteStop();
}
