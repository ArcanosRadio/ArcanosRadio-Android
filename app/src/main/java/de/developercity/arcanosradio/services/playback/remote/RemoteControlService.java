package de.developercity.arcanosradio.services.playback.remote;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import de.developercity.arcanosradio.services.generics.LocalBinder;

public class RemoteControlService extends Service
        implements IRemoteControlService {

    private static final String TAG = RemoteControlService.class.getSimpleName();

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat currentState;
    private IRemoteControlServiceDelegate delegate;

    private final Binder binder = new LocalBinder(RemoteControlService.this);
    private final BroadcastReceiver mediaButtonReceiver = new MediaButtonReceiver();
    private final IntentFilter intentFilterMediaButton = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ComponentName componentName = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
        mediaSession = new MediaSessionCompat(this, TAG, componentName, null);

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                setState(PlaybackStateCompat.STATE_PLAYING);
                if (delegate != null) delegate.onRemotePlay();
            }

            @Override
            public void onPause() {
                super.onPause();
                setState(PlaybackStateCompat.STATE_PAUSED);
                if (delegate != null) delegate.onRemotePause();
            }

            @Override
            public void onStop() {
                super.onStop();
                setState(PlaybackStateCompat.STATE_STOPPED);
                if (delegate != null) delegate.onRemoteStop();
            }
        });

        setState(PlaybackStateCompat.STATE_NONE);
        registerReceiver(mediaButtonReceiver, intentFilterMediaButton);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.delegate = null;
        mediaSession.setActive(false);
        mediaSession.release();
        unregisterReceiver(mediaButtonReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean stopService(Intent name) {
        stopForeground(true);
        return super.stopService(name);
    }

    @Override
    public void setDelegate(IRemoteControlServiceDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setStatePaused() {
        setState(PlaybackStateCompat.STATE_PAUSED);
    }

    @Override
    public void setStateBuffering() {
        setState(PlaybackStateCompat.STATE_BUFFERING);
    }

    @Override
    public void setStatePlaying() {
        setState(PlaybackStateCompat.STATE_PLAYING);
    }

    public void setState(int state) {
        currentState = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY
                                | PlaybackStateCompat.ACTION_PAUSE
                                | PlaybackStateCompat.ACTION_PLAY_PAUSE
                                | PlaybackStateCompat.ACTION_STOP)

                .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f, SystemClock.elapsedRealtime())
                .build();

        mediaSession.setPlaybackState(currentState);
        mediaSession.setActive(state != PlaybackStateCompat.STATE_NONE && state != PlaybackStateCompat.STATE_STOPPED);
    }
}
