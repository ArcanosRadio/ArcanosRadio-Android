package de.developercity.arcanosradio.services.playback.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.media.session.PlaybackStateCompat;

import com.crashlytics.android.Crashlytics;

import de.developercity.arcanosradio.ArcanosRadioApplication;
import de.developercity.arcanosradio.Constants;

class Player implements IPlayer,
        MediaPlayer.OnPreparedListener {

    private static final String TAG = Player.class.getSimpleName();

    private MediaPlayer mp;
    private IPlayerBaseDelegate delegate;

    private int state = PlaybackStateCompat.STATE_NONE;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mp != null) {
                if (mp.getCurrentPosition() > 0) {
                    notifyPlayerPlaying();
                    handler.removeCallbacks(this);
                } else {
                    notifyPlayerBuffer();
                    handler.postDelayed(this, 500);
                }
            }
        }
    };

    Player(IPlayerBaseDelegate delegate) {
        this.mp = new MediaPlayer();
        this.delegate = delegate;

        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnPreparedListener(this);
    }

    @Override
    public boolean play() {
        if (!isPlaying()) {
            try {
                mp.reset();
                String url = ArcanosRadioApplication.getStorage().readConfig(Constants.STREAMING_URL_CONFIG_KEY);
                mp.setDataSource(url);
                mp.prepareAsync();

                handler.postDelayed(runnable, 100);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }

        return false;
    }

    @Override
    public boolean pause() {
        if (mp != null) {
            mp.stop();
            notifyPlayerPause();
            return true;
        }
        return false;
    }

    @Override
    public boolean isBuffering() {
        return mp != null && state == PlaybackStateCompat.STATE_BUFFERING;
    }

    @Override
    public boolean isPlaying() {
        return mp != null && state == PlaybackStateCompat.STATE_PLAYING;
    }

    @Override
    public void releasePlayer() {
        if (mp != null) {
            mp.reset();
            mp.release();
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public int getState() {
        return this.state;
    }

    @Override
    public void setVolume(float left, float right) {
        mp.setVolume(left, right);
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        if (state == PlaybackStateCompat.STATE_NONE
                || state == PlaybackStateCompat.STATE_PAUSED
                || state == PlaybackStateCompat.STATE_BUFFERING) {
            player.start();
        }
    }

    private void notifyPlayerPlaying() {
        state = PlaybackStateCompat.STATE_PLAYING;
        if (delegate != null) delegate.onPlayerPlay();
    }

    private void notifyPlayerBuffer() {
        state = PlaybackStateCompat.STATE_BUFFERING;
        if (delegate != null) delegate.onPlayerBuffer();
    }

    private void notifyPlayerPause() {
        state = PlaybackStateCompat.STATE_PAUSED;
        if (delegate != null) delegate.onPlayerPause();
    }
}
