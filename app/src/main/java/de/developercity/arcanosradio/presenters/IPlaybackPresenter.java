package de.developercity.arcanosradio.presenters;

import android.widget.SeekBar;

public interface IPlaybackPresenter {
    void onCreate();

    void onPause();

    void onResume();

    void onDestroy();

    void handlePlayPauseClick();

    void handleVolumeSeekBar(SeekBar seekBar);

    boolean createShareIntent();

    boolean createOpenBrowserIntent();

    void castContent();
}
