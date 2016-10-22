package de.developercity.arcanosradio.services.playback.manager;

import de.developercity.arcanosradio.services.playback.player.IPlayerBase;

public interface IPlayerManager extends IPlayerBase {
    void onCreate();

    void onDestroy();

    int getStreamMaxVolume();

    int getStreamVolume();

    void setStreamVolume(int volume);
}
