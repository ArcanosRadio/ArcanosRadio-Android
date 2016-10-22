package de.developercity.arcanosradio.services.playback.remote;

public interface IRemoteControlService {
    void setDelegate(IRemoteControlServiceDelegate delegate);

    void setStatePaused();

    void setStateBuffering();

    void setStatePlaying();
}
