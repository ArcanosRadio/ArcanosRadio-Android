package de.developercity.arcanosradio.services.playback.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import de.developercity.arcanosradio.ArcanosRadioApplication;
import de.developercity.arcanosradio.R;
import de.developercity.arcanosradio.services.network.INetworkStatusDelegate;
import de.developercity.arcanosradio.services.network.NetworkStatusReceiver;
import de.developercity.arcanosradio.services.playback.player.IPlayer;
import de.developercity.arcanosradio.services.playback.player.IPlayerBaseDelegate;
import de.developercity.arcanosradio.services.playback.player.PlayerFactory;

class PlayerManager implements IPlayerManager,
        IPlayerBaseDelegate,
        INetworkStatusDelegate,
        AudioManager.OnAudioFocusChangeListener {

    private static final float VOLUME_DUCK = 0.2f;
    private static final float VOLUME_NORMAL = 1.0f;

    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    private static final int AUDIO_FOCUSED = 2;

    private static final int CONNECTION_OFFLINE = 0;
    private static final int CONNECTION_MOBILE = 1;
    private static final int CONNECTION_WIFI = 2;

    private final Context context;
    private final IPlayerBaseDelegate delegate;
    private final AudioManager audioManager;
    private final IPlayer player;
    private final WifiManager.WifiLock wifiLock;

    private boolean playOnFocusGain;
    private int audioFocus = AUDIO_NO_FOCUS_NO_DUCK;
    private int currentConnection = CONNECTION_OFFLINE;

    private NetworkStatusReceiver networkStatusReceiver;
    private volatile boolean networkReceiverRegistered;
    private volatile boolean audioNoisyReceiverRegistered;

    private final BroadcastReceiver audioNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
                pause();
        }
    };

    PlayerManager(Context context, IPlayerBaseDelegate delegate) {
        this.context = context;
        this.delegate = delegate;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.player = PlayerFactory.createPlayer(this);
        this.wifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "arcanos_lock");

        onCreate();
    }

    @Override
    public void onCreate() {
        registerNetworkReceiver();
    }

    @Override
    public void onDestroy() {
        unregisterNetworkReceiver();
        player.releasePlayer();
    }

    @Override
    public boolean play() {
        if (currentConnection == CONNECTION_MOBILE
                && !ArcanosRadioApplication.getStorage().getUserPreferences().getMobileDataStreamingEnabled()) {

            Toast.makeText(context, R.string.notification_mobile_data_disabled, Toast.LENGTH_LONG).show();
            onPlayerPause();

            return false;
        }

        tryToGetAudioFocus();
        registerAudioNoisyReceiver();
        wifiLock.acquire();

        return player.play();
    }

    @Override
    public boolean pause() {
        return player.pause();
    }

    @Override
    public boolean isBuffering() {
        return player.isBuffering();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying() || player.isBuffering();
    }

    @Override
    public void releasePlayer() {
        giveUpAudioFocus();
        unregisterAudioNoisyReceiver();
        if (wifiLock.isHeld()) wifiLock.release();
    }

    @Override
    public int getStreamMaxVolume() {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public int getStreamVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void setStreamVolume(int volume) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    @Override
    public void onPlayerPlay() {
        if (this.delegate != null) this.delegate.onPlayerPlay();
    }

    @Override
    public void onPlayerBuffer() {
        if (this.delegate != null) this.delegate.onPlayerBuffer();
    }

    @Override
    public void onPlayerPause() {
        if (this.delegate != null) this.delegate.onPlayerPause();
        releasePlayer();
    }

    @Override
    public void onPlaybackInterrupted() {
        if (this.delegate != null) this.delegate.onPlaybackInterrupted();
        playOnFocusGain = true;
    }

    @Override
    public void onPlaybackRestored() {
        if (this.delegate != null) this.delegate.onPlaybackRestored();
    }

    @Override
    public void onNetworkDisconnected() {
        currentConnection = CONNECTION_OFFLINE;
        pause();
        onPlaybackInterrupted();
    }

    @Override
    public void onMobileConnected() {
        currentConnection = CONNECTION_MOBILE;
        if (!ArcanosRadioApplication.getStorage().getUserPreferences().getMobileDataStreamingEnabled()) {
            pause();
            onPlaybackInterrupted();
        } else {
            onPlaybackRestored();
            configMediaPlayerState();
        }
    }

    @Override
    public void onWifiConnected() {
        currentConnection = CONNECTION_WIFI;
        onPlaybackRestored();
        configMediaPlayerState();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            audioFocus = AUDIO_FOCUSED;
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            boolean canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
            audioFocus = canDuck ? AUDIO_NO_FOCUS_CAN_DUCK : AUDIO_NO_FOCUS_NO_DUCK;

            if (isPlaying() && !canDuck) {
                playOnFocusGain = true;
            }
        }

        configMediaPlayerState();
    }

    private void registerAudioNoisyReceiver() {
        if (!audioNoisyReceiverRegistered) {
            context.registerReceiver(audioNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            audioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (audioNoisyReceiverRegistered) {
            context.unregisterReceiver(audioNoisyReceiver);
            audioNoisyReceiverRegistered = false;
        }
    }

    private void registerNetworkReceiver() {
        if (!networkReceiverRegistered) {
            networkStatusReceiver = new NetworkStatusReceiver();
            networkStatusReceiver.setDelegate(this);
            context.registerReceiver(networkStatusReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            networkReceiverRegistered = true;
        }
    }

    private void unregisterNetworkReceiver() {
        if (networkReceiverRegistered) {
            context.unregisterReceiver(networkStatusReceiver);
            networkReceiverRegistered = false;
        }
    }

    private void configMediaPlayerState() {
        tryToGetAudioFocus();

        if (audioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
            if (player.getState() != PlaybackStateCompat.STATE_PAUSED) {
                pause();
            }
        } else {
            if (audioFocus == AUDIO_NO_FOCUS_CAN_DUCK) {
                player.setVolume(VOLUME_DUCK, VOLUME_DUCK);
            } else {
                player.setVolume(VOLUME_NORMAL, VOLUME_NORMAL);
            }

            if (playOnFocusGain) {
                play();
                playOnFocusGain = false;
            }
        }
    }

    private void tryToGetAudioFocus() {
        if (player != null && audioFocus != AUDIO_FOCUSED) {
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocus = AUDIO_FOCUSED;
            }
        }
    }

    private void giveUpAudioFocus() {
        if (player != null && audioFocus == AUDIO_FOCUSED) {
            if (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocus = AUDIO_NO_FOCUS_NO_DUCK;
            }
        }
    }
}
