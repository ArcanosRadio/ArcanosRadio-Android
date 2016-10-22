package de.developercity.arcanosradio.services.playback.manager;

import android.content.Context;

import de.developercity.arcanosradio.services.playback.player.IPlayerBaseDelegate;

public abstract class PlayerManagerFactory {
    public static IPlayerManager createPlayerManager(Context context, IPlayerBaseDelegate delegate) {
        return new PlayerManager(context, delegate);
    }
}
