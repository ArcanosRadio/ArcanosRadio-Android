package de.developercity.arcanosradio.services.metadata;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import bolts.Continuation;
import bolts.Task;
import de.developercity.arcanosradio.ArcanosRadioApplication;
import de.developercity.arcanosradio.models.Playlist;
import de.developercity.arcanosradio.services.generics.LocalBinder;
import de.developercity.arcanosradio.services.storage.IStorage;

public class MetadataPoolingService extends Service
        implements IMetadataService {

    private static final String TAG = MetadataPoolingService.class.getSimpleName();

    private IMetadataServiceDelegate delegate;
    private IStorage storage;
    private Playlist currentPlaylist;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            downloadCurrentSong();
            handler.postDelayed(this, 5 * 1000);
        }
    };

    private final Binder binder = new LocalBinder(MetadataPoolingService.this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.storage = ArcanosRadioApplication.getStorage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.delegate = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startScheduledFetch();

        return START_NOT_STICKY;
    }

    @Override
    public boolean stopService(Intent name) {
        stopForeground(true);
        cancelScheduledFetch();

        return super.stopService(name);
    }

    @Override
    public void setDelegate(IMetadataServiceDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void startScheduledFetch() {
        handler.postDelayed(runnable, 100);
    }

    @Override
    public void cancelScheduledFetch() {
        currentPlaylist = null;
        handler.removeCallbacks(runnable);
    }

    private void downloadCurrentSong() {
        storage.getCurrentSong()
                .continueWith(new Continuation<Playlist, Object>() {
                    @Override
                    public Object then(Task<Playlist> task) throws Exception {
                        if (task.getError() != null) {
                            Log.e(TAG, "Could not fetch Playlist class from Parse Server");
                            return task;
                        }

                        Playlist result = task.getResult();

                        if (result == null) {
                            if (currentPlaylist == null) return null;
                            currentPlaylist = null;
                            if (delegate != null) delegate.onSongMetadataFetched(null);
                            return task;
                        }

                        long lastUpdate = currentPlaylist == null ? 0 : currentPlaylist.getUpdatedAt().getTime();

                        if (result.getUpdatedAt().getTime() - lastUpdate < 2000) {
                            // Same song
                            return task;
                        }

                        currentPlaylist = result;

                        if (delegate != null) delegate.onSongMetadataFetched(currentPlaylist);

                        if (currentPlaylist.getSong() == null) return task;

                        downloadAlbumArtAsync();

                        downloadAlbumLyricsAsync();

                        return task;
                    }
                });
    }

    private void downloadAlbumArtAsync() {
        if (currentPlaylist.getSong().getAlbumArt() == null) {
            return;
        }

        storage.getAlbumArtBySong(currentPlaylist.getSong())
                .continueWith(new Continuation<byte[], Object>() {
                                  @Override
                                  public Object then(Task<byte[]> task) throws Exception {
                                      if (delegate != null)
                                          delegate.onSongAlbumArtFetched(task.getResult());
                                      return task;
                                  }
                              }
                );
    }

    private void downloadAlbumLyricsAsync() {
        if (currentPlaylist.getSong().getLyrics() == null) {
            return;
        }

        storage.getLyricsBySong(currentPlaylist.getSong())
                .continueWith(new Continuation<String, Object>() {
                                  @Override
                                  public Object then(Task<String> task) throws Exception {
                                      if (delegate != null)
                                          delegate.onSongLyricsFetched(task.getResult());
                                      return task;
                                  }
                              }
                );
    }
}
