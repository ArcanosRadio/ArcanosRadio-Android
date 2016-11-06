package de.developercity.arcanosradio.presenters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.SeekBar;

import de.developercity.arcanosradio.ArcanosRadioApplication;
import de.developercity.arcanosradio.R;
import de.developercity.arcanosradio.helpers.ImageHelper;
import de.developercity.arcanosradio.models.Playlist;
import de.developercity.arcanosradio.models.Song;
import de.developercity.arcanosradio.services.generics.IServiceConnectionDelegate;
import de.developercity.arcanosradio.services.metadata.IMetadataService;
import de.developercity.arcanosradio.services.metadata.IMetadataServiceDelegate;
import de.developercity.arcanosradio.services.metadata.MetadataFactory;
import de.developercity.arcanosradio.services.network.NetworkStatusReceiver;
import de.developercity.arcanosradio.services.notification.INotificationService;
import de.developercity.arcanosradio.services.notification.INotificationServiceDelegate;
import de.developercity.arcanosradio.services.notification.NotificationFactory;
import de.developercity.arcanosradio.services.playback.manager.IPlayerManager;
import de.developercity.arcanosradio.services.playback.manager.PlayerManagerFactory;
import de.developercity.arcanosradio.services.playback.player.IPlayerBaseDelegate;
import de.developercity.arcanosradio.services.playback.remote.IRemoteControlService;
import de.developercity.arcanosradio.services.playback.remote.IRemoteControlServiceDelegate;
import de.developercity.arcanosradio.services.playback.remote.RemoteControlFactory;

import static de.developercity.arcanosradio.Constants.CUSTOM_INTENT_FINISH_NOW_PLAYING;

public class PlaybackPresenter implements IPlaybackPresenter,
        IPlayerManager,
        IPlayerBaseDelegate,
        INotificationServiceDelegate,
        IMetadataServiceDelegate,
        IRemoteControlServiceDelegate {

    private Context context;
    private IPlaybackView view;
    private IPlayerManager playerManager;
    private IMetadataService metadataService;
    private INotificationService notificationService;
    private IRemoteControlService remoteControlService;

    private Song currentSong;

    private boolean hasConnectionToStream;

    private PlaybackPresenter(Context context, IPlaybackView view) {
        this.context = context;
        this.view = view;
        this.view.setPresenter(this);
        this.playerManager = PlayerManagerFactory.createPlayerManager(context, this);
        this.onCreate();
    }

    public static PlaybackPresenter createPresenter(Context context, IPlaybackView view) {
        return new PlaybackPresenter(context, view);
    }

    @Override
    public void onCreate() {
        startServices();

        hasConnectionToStream = NetworkStatusReceiver.hasConnection(context);

        if (hasConnectionToStream) {
            play();
        }
    }

    @Override
    public void onPause() {
        unbindServices();
    }

    @Override
    public void onResume() {
        bindServices();
    }

    @Override
    public void onDestroy() {
        stopServices();
        releasePlayer();
        playerManager.onDestroy();

        this.context = null;
        this.view = null;
        this.playerManager = null;
        this.metadataService = null;
        this.notificationService = null;
    }

    @Override
    public boolean play() {
        if (metadataService != null) metadataService.startScheduledFetch();
        return playerManager.play();
    }

    @Override
    public boolean pause() {
        if (metadataService != null) metadataService.cancelScheduledFetch();
        if (notificationService != null) notificationService.setDefaultMetadata();
        if (view != null) view.setDefaultMetadata();

        return playerManager.pause();
    }

    @Override
    public boolean isPlaying() {
        return playerManager.isPlaying();
    }

    @Override
    public boolean isBuffering() {
        return playerManager.isBuffering();
    }

    @Override
    public void releasePlayer() {
        playerManager.releasePlayer();
    }

    @Override
    public int getStreamMaxVolume() {
        return playerManager.getStreamMaxVolume();
    }

    @Override
    public int getStreamVolume() {
        return playerManager.getStreamVolume();
    }

    @Override
    public void setStreamVolume(int volume) {
        playerManager.setStreamVolume(volume);
    }

    @Override
    public void onPlayerPlay() {
        if (view != null) view.setPlayToggleDrawablePause();
        if (notificationService != null) notificationService.updateNotificationTogglePause();
        if (remoteControlService != null) remoteControlService.setStatePlaying();
    }

    @Override
    public void onPlayerBuffer() {
        if (view != null) view.setPlayToggleDrawableBuffer();
        if (notificationService != null) notificationService.updateNotificationToggleBuffer();
        if (remoteControlService != null) remoteControlService.setStateBuffering();
    }

    @Override
    public void onPlayerPause() {
        if (view != null) view.setPlayToggleDrawablePlay();
        if (notificationService != null) {
            if (hasConnectionToStream) {
                notificationService.updateNotificationTogglePlay();
            } else {
                notificationService.updateNotificationToggleNoConnection();
            }
        }
        if (remoteControlService != null) remoteControlService.setStatePaused();
    }

    @Override
    public void onPlaybackInterrupted() {
        hasConnectionToStream = false;

        if (view != null) view.setPlayToggleDrawableNoConnection();
        if (notificationService != null) {
            notificationService.updateNotificationToggleNoConnection();
            notificationService.sendNetworkOutageNotification();
        }
    }

    @Override
    public void onPlaybackRestored() {
        hasConnectionToStream = true;

        if (notificationService != null) notificationService.removeNetworkOutageNotification();
    }

    @Override
    public void onNotificationActionPlayToggle() {
        togglePlay();
    }

    @Override
    public void onNotificationActionStopService() {
        context.sendBroadcast(new Intent(CUSTOM_INTENT_FINISH_NOW_PLAYING));
    }

    @Override
    public void onSongMetadataFetched(final Playlist playlist) {
        currentSong = playlist != null ? playlist.getSong() : null;
        if (view != null) view.setSongMetadata(currentSong);
        if (notificationService != null)
            notificationService.updateNotificationMetadata(currentSong);
    }

    @Override
    public void onSongAlbumArtFetched(final byte[] albumArt) {
        Bitmap bitmap;

        if (albumArt != null) {
            bitmap = ImageHelper.decodeAndRescaleBitmap(albumArt, 800, 480);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.arcanos);
        }

        if (view != null) view.setAlbumArt(bitmap);
        if (notificationService != null) notificationService.updateNotificationAlbumArt(bitmap);
    }

    @Override
    public void onSongLyricsFetched(final String lyrics) {
        if (view != null) view.setLyrics(lyrics);
    }

    @Override
    public void onRemotePlay() {
        play();
    }

    @Override
    public void onRemotePause() {
        pause();
    }

    @Override
    public void onRemoteStop() {
        pause();
    }

    @Override
    public void handlePlayPauseClick() {
        togglePlay();
    }

    @Override
    public void handleVolumeSeekBar(SeekBar seekBar) {
        try {
            seekBar.setMax(getStreamMaxVolume());
            seekBar.setProgress(getStreamVolume());

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    setStreamVolume(progress);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean createShareIntent() {
        String songName = currentSong != null
                && currentSong.getSongName() != null ? currentSong.getSongName() : "";

        String artistName = currentSong != null
                && currentSong.getArtist() != null
                && currentSong.getArtist().getArtistName() != null ? currentSong.getArtist().getArtistName() : "";

        if (!songName.isEmpty() && !artistName.isEmpty()) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.now_playing_msg_share, songName, artistName));

            Intent chooser = Intent.createChooser(intent, context.getString(R.string.now_playing_msg_share_title));
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(chooser);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean createOpenBrowserIntent() {
        String artistUrl =
                currentSong != null
                && currentSong.getArtist() != null
                && currentSong.getArtist().getUrl() != null ? currentSong.getArtist().getUrl() : "";

        if (artistUrl != null && !artistUrl.isEmpty()) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(artistUrl));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void castContent() {

    }

    private void togglePlay() {
        if (hasConnectionToStream) {
            if (isPlaying()) {
                pause();
            } else {
                play();
            }
        } else {
            if (view != null) view.alertNoConnection();
        }
    }

    private void startServices() {
        context.startService(new Intent(context, NotificationFactory.getServiceType()));

        ArcanosRadioApplication.getNotificationsServiceConnection()
                .addDelegate(new IServiceConnectionDelegate<INotificationService>() {
                    @Override
                    public void onConnect(INotificationService service) {
                        notificationService = service;
                        notificationService.setDelegate(PlaybackPresenter.this);
                    }

                    @Override
                    public void onDisconnect(INotificationService service) {
                        service.setDelegate(null);
                    }
                });


        context.startService(new Intent(context, MetadataFactory.getServiceType()));

        ArcanosRadioApplication.getMetadataServiceConnection()
                .addDelegate(new IServiceConnectionDelegate<IMetadataService>() {
                    @Override
                    public void onConnect(IMetadataService service) {
                        metadataService = service;
                        metadataService.setDelegate(PlaybackPresenter.this);
                    }

                    @Override
                    public void onDisconnect(IMetadataService service) {
                        service.setDelegate(null);
                    }
                });

        context.startService(new Intent(context, RemoteControlFactory.getServiceType()));

        ArcanosRadioApplication.getRemoteControlServiceConnection()
                .addDelegate(new IServiceConnectionDelegate<IRemoteControlService>() {
                    @Override
                    public void onConnect(IRemoteControlService service) {
                        remoteControlService = service;
                        remoteControlService.setDelegate(PlaybackPresenter.this);
                    }

                    @Override
                    public void onDisconnect(IRemoteControlService service) {
                        service.setDelegate(null);
                    }
                });
    }

    private void bindServices() {
        context.bindService(new Intent(context, NotificationFactory.getServiceType()),
                ArcanosRadioApplication.getNotificationsServiceConnection(),
                Context.BIND_AUTO_CREATE);
        context.bindService(new Intent(context, MetadataFactory.getServiceType()),
                ArcanosRadioApplication.getMetadataServiceConnection(),
                Context.BIND_AUTO_CREATE);
        context.bindService(new Intent(context, RemoteControlFactory.getServiceType()),
                ArcanosRadioApplication.getRemoteControlServiceConnection(),
                Context.BIND_AUTO_CREATE);
    }

    private void unbindServices() {
        context.unbindService(ArcanosRadioApplication.getNotificationsServiceConnection());
        context.unbindService(ArcanosRadioApplication.getMetadataServiceConnection());
        context.unbindService(ArcanosRadioApplication.getRemoteControlServiceConnection());
    }

    private void stopServices() {
        context.stopService(new Intent(context, NotificationFactory.getServiceType()));
        context.stopService(new Intent(context, MetadataFactory.getServiceType()));
        context.stopService(new Intent(context, RemoteControlFactory.getServiceType()));
    }
}
