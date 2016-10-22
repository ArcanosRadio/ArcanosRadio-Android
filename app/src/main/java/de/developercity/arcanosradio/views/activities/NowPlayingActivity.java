package de.developercity.arcanosradio.views.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.developercity.arcanosradio.ArcanosRadioApplication;
import de.developercity.arcanosradio.R;
import de.developercity.arcanosradio.helpers.ImageHelper;
import de.developercity.arcanosradio.models.Song;
import de.developercity.arcanosradio.presenters.IPlaybackPresenter;
import de.developercity.arcanosradio.presenters.IPlaybackView;
import de.developercity.arcanosradio.presenters.PlaybackPresenter;

import static de.developercity.arcanosradio.Constants.CUSTOM_INTENT_FINISH_NOW_PLAYING;

public class NowPlayingActivity extends AppCompatActivity
        implements IPlaybackView,
        ViewTreeObserver.OnScrollChangedListener,
        PopupMenu.OnMenuItemClickListener {

    private static final String TAG = NowPlayingActivity.class.getSimpleName();
    private Context context;
    private IPlaybackPresenter presenter;
    private Drawable pauseDrawable, playDrawable, bufferDrawable, noWifiDrawable, noCellularDrawable;

    //region Layout bindings
    // Containers
    @BindView(R.id.layout_root)
    CoordinatorLayout layout;

    @BindView(R.id.layout_header_now_playing)
    FrameLayout nowPlayingHeader;

    @BindView(R.id.layout_header_metadata)
    LinearLayout headerMetadataLayout;

    @BindView(R.id.scroll_view_now_playing)
    ScrollView scrollView;

    // Album art
    @BindView(R.id.image_view_album_art)
    ImageView albumArtImageView;

    @BindView(R.id.image_view_album_art_blur)
    ImageView blurAlbumArtImageView;

    @BindView(R.id.image_view_album_art_mini)
    ImageView miniAlbumArtImageView;

    // Song and artist name
    @BindView(R.id.text_view_song)
    TextView songTextView;

    @BindView(R.id.text_view_song_header)
    TextView songHeaderTextView;

    @BindView(R.id.text_view_artist)
    TextView artistTextView;

    @BindView(R.id.text_view_artist_header)
    TextView artistHeaderTextView;

    // Lyrics
    @BindView(R.id.text_view_lyrics)
    TextView lyricsTextView;

    // Media control
    @BindView(R.id.seek_volume)
    SeekBar volumeSeekBar;

    @BindView(R.id.fab)
    FloatingActionButton fabPlayPause;
    //endregion

    private final BroadcastReceiver finisher = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        PlaybackPresenter.createPresenter(context, this);

        setUpLayout();

        registerReceiver(finisher, new IntentFilter(CUSTOM_INTENT_FINISH_NOW_PLAYING));
    }

    @Override
    protected void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (ArcanosRadioApplication.getStorage().getUserPreferences().getKeepScreenOnEnabled()) {
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        presenter.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(finisher);
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // This shall be overridden if we want to do things when coming from the notification
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startSettingsActivity();
                return true;
            case R.id.menu_about:
                startAboutActivity();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpLayout() {
        setContentView(R.layout.activity_now_playing);
        ButterKnife.bind(this);

        pauseDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pause_white_36dp);
        bufferDrawable = ContextCompat.getDrawable(context, R.drawable.ic_hourglass_empty_white_36dp);
        playDrawable = ContextCompat.getDrawable(context, R.drawable.ic_play_arrow_white_36dp);
        noWifiDrawable = ContextCompat.getDrawable(context, R.drawable.ic_no_wifi_connection_white_36dp);
        noCellularDrawable = ContextCompat.getDrawable(context, R.drawable.ic_no_cellular_connection_white_36dp);

        setUpFab();
        setUpVolumeSlider();

        scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
        calculateLyricsPadding();
    }

    @Override
    public void onScrollChanged() {
        int margin = getResources().getDimensionPixelSize(R.dimen.margin_smaller);
        int minHeaderHeight = getResources().getDimensionPixelSize(R.dimen.mini_album_art_size)
                + 2 * margin;
        int maxHeaderHeight = this.albumArtImageView.getWidth(); // width, because max should show a square
        int calculatedHeight = maxHeaderHeight - scrollView.getScrollY();
        int newHeaderSize = Math.max(minHeaderHeight, calculatedHeight);

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) nowPlayingHeader.getLayoutParams();
        lp.height = newHeaderSize;
        nowPlayingHeader.setLayoutParams(lp);

        // newHeaderSize = minHeaderHeight => progress = 100%
        // newHeaderSize = maxHeaderHeight => progress = 0%
        float progress = Math.min(1.0f, (float) (maxHeaderHeight - newHeaderSize) / (float) minHeaderHeight);
        miniAlbumArtImageView.setAlpha(progress);
        blurAlbumArtImageView.setAlpha(progress);

        int songNameTopMargin = getResources().getDimensionPixelSize(R.dimen.margin_large);
        int mainSongNameHiddenProgress = Math.min(0, calculatedHeight + margin + songNameTopMargin - minHeaderHeight);

        int headerMetadataStart = minHeaderHeight / 2 + margin * 2;
        int headerMetadataCurrent = Math.max(0, headerMetadataStart + mainSongNameHiddenProgress);
        headerMetadataLayout.setAlpha(mainSongNameHiddenProgress == 0 ? 0 : 1);
        headerMetadataLayout.setTranslationY(headerMetadataCurrent);
    }

    private void setUpFab() {
        fabPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.handlePlayPauseClick();
            }
        });
    }

    @OnClick(R.id.button_share)
    public void shareCurrentSong() {
        if (!presenter.createShareIntent()) {
            Snackbar.make(layout, getString(R.string.generic_msg_error), Snackbar.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.button_open_in_browser)
    public void openBrowser() {
        if (!presenter.createOpenBrowserIntent()) {
            Snackbar.make(layout, getString(R.string.now_playing_msg_artist_url_not_available), Snackbar.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.button_cast)
    public void castContent() {

    }

    @OnClick(R.id.button_settings)
    public void showSettingsMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.getMenuInflater().inflate(R.menu.menu_now_playing, popup.getMenu());
        popup.show();
    }

    private void setUpVolumeSlider() {
        presenter.handleVolumeSeekBar(volumeSeekBar);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int index = volumeSeekBar.getProgress();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                volumeSeekBar.setProgress(index + 1);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                volumeSeekBar.setProgress(index - 1);
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void setPresenter(IPlaybackPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setPlayToggleDrawablePause() {
        fabPlayPause.setImageDrawable(pauseDrawable);
    }

    @Override
    public void setPlayToggleDrawableBuffer() {
        fabPlayPause.setImageDrawable(bufferDrawable);
    }

    @Override
    public void setPlayToggleDrawablePlay() {
        fabPlayPause.setImageDrawable(playDrawable);
    }

    @Override
    public void setPlayToggleDrawableNoConnection() {
        if (ArcanosRadioApplication.getStorage().getUserPreferences().getMobileDataStreamingEnabled()) {
            fabPlayPause.setImageDrawable(noCellularDrawable);
        } else {
            fabPlayPause.setImageDrawable(noWifiDrawable);
        }
    }

    @Override
    public void alertNoConnection() {
        Snackbar.make(layout, getString(R.string.now_playing_connection_warning), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void setDefaultMetadata() {
        NowPlayingActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.arcanos);
                setAlbumArt(drawable.getBitmap());
                songTextView.setText(getString(R.string.arcanos_web_rock));
                songHeaderTextView.setText(getString(R.string.arcanos_web_rock));
                artistTextView.setText("");
                artistHeaderTextView.setText("");
            }
        });

        setLyrics("");
    }

    @Override
    public void setSongMetadata(final Song song) {
        if (song == null) {
            setDefaultMetadata();
            return;
        }

        NowPlayingActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String songName = song.getSongName();
                String artistName = song.getArtist() != null ? song.getArtist().getArtistName() : "";

                BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.arcanos);
                setAlbumArt(drawable.getBitmap());
                songTextView.setText(songName);
                songHeaderTextView.setText(songName);
                artistTextView.setText(artistName);
                artistHeaderTextView.setText(artistName);
            }
        });

        setLyrics("");
    }

    @Override
    public void setAlbumArt(final Bitmap albumArt) {
        NowPlayingActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int bigSquareSize = albumArtImageView.getWidth();
                int smallSquareSize = miniAlbumArtImageView.getWidth();

                Bitmap bigAlbumBitmap = Bitmap.createScaledBitmap(albumArt, bigSquareSize, bigSquareSize, false);
                Bitmap blurAlbumBitmap = ImageHelper.blur(NowPlayingActivity.this, bigAlbumBitmap, 22f);
                Bitmap smallAlbumBitmap = Bitmap.createScaledBitmap(albumArt, smallSquareSize, smallSquareSize, false);

                albumArtImageView.setImageBitmap(bigAlbumBitmap);
                blurAlbumArtImageView.setImageBitmap(blurAlbumBitmap);
                miniAlbumArtImageView.setImageBitmap(smallAlbumBitmap);

                int songNameTopMargin = getResources().getDimensionPixelSize(R.dimen.margin_large);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) songTextView.getLayoutParams();
                lp.topMargin = bigSquareSize + songNameTopMargin;
                songTextView.setLayoutParams(lp);
            }
        });
    }

    @Override
    public void setLyrics(final String lyrics) {
        NowPlayingActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lyricsTextView.setText(lyrics);
                calculateLyricsPadding();
            }
        });
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void calculateLyricsPadding() {
        lyricsTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                lyricsTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float deviceHeight = context.getResources().getDisplayMetrics().heightPixels;
                int margin = getResources().getDimensionPixelSize(R.dimen.margin_smaller);
                int minHeaderHeight = getResources().getDimensionPixelSize(R.dimen.mini_album_art_size)
                        + 2 * margin;
                float lyricsHeight = lyricsTextView.getText() != "" ?
                        lyricsTextView.getHeight()
                        : 0;

                int calculatedPadding = Math.round(
                        Math.max(getResources().getDimensionPixelSize(R.dimen.lyrics_padding),
                                deviceHeight - lyricsHeight - minHeaderHeight / 2));

                lyricsTextView.setPadding(0, 0, 0, calculatedPadding);
            }
        });
    }
}