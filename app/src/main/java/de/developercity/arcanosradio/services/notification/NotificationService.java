package de.developercity.arcanosradio.services.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import de.developercity.arcanosradio.ArcanosRadioApplication;
import de.developercity.arcanosradio.R;
import de.developercity.arcanosradio.models.Song;
import de.developercity.arcanosradio.services.generics.LocalBinder;
import de.developercity.arcanosradio.views.activities.NowPlayingActivity;

public class NotificationService extends Service
        implements INotificationService {

    private static final String ACTION_PLAY_TOGGLE = "de.developercity.arcanosradio.ACTION.PLAY_TOGGLE";
    private static final String ACTION_STOP_SERVICE = "de.developercity.arcanosradio.ACTION.STOP_SERVICE";

    private static final int NOTIFICATION_ID_PLAYER = 1;
    private static final int NOTIFICATION_ID_NETWORK = 2;

    private RemoteViews mContentViewBig, mContentViewSmall;

    private INotificationServiceDelegate delegate;

    private Song currentSong;
    private Bitmap currentAlbumArt;
    private int currentNotificationDrawableId = R.drawable.ic_hourglass_empty_white_36dp;

    private final Binder binder = new LocalBinder(NotificationService.this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.delegate = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ACTION_PLAY_TOGGLE.equals(action)) {
            if (delegate != null) {
                delegate.onNotificationActionPlayToggle();
            }
        } else if (ACTION_STOP_SERVICE.equals(action)) {
            if (delegate != null) {
                delegate.onNotificationActionStopService();
            }
            stopForeground(true);
        }
        return START_NOT_STICKY;
    }

    @Override
    public boolean stopService(Intent name) {
        stopForeground(true);
        return super.stopService(name);
    }

    @Override
    public void setDelegate(INotificationServiceDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setDefaultMetadata() {
        currentSong = null;
        currentAlbumArt = null;
        showNotification();
    }

    @Override
    public void updateNotificationMetadata(Song song) {
        currentSong = song;
        showNotification();
    }

    @Override
    public void updateNotificationAlbumArt(Bitmap albumArt) {
        currentAlbumArt = albumArt;
        showNotification();
    }

    @Override
    public void updateNotificationTogglePause() {
        currentNotificationDrawableId = R.drawable.ic_pause_white_36dp;
        showNotification();
    }

    @Override
    public void updateNotificationToggleBuffer() {
        currentNotificationDrawableId = R.drawable.ic_hourglass_empty_white_36dp;
        showNotification();
    }

    @Override
    public void updateNotificationTogglePlay() {
        currentNotificationDrawableId = R.drawable.ic_play_arrow_white_36dp;
        showNotification();
    }

    @Override
    public void updateNotificationToggleNoConnection() {
        if (ArcanosRadioApplication.getStorage().getUserPreferences().getMobileDataStreamingEnabled()) {
            currentNotificationDrawableId = R.drawable.ic_no_cellular_connection_white_36dp;
        } else {
            currentNotificationDrawableId = R.drawable.ic_no_wifi_connection_white_36dp;
        }
    }

    @Override
    public void sendNetworkOutageNotification() {
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, NowPlayingActivity.class), 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_radio_white_24dp)
                .setContentIntent(contentIntent)
                .setContentTitle(getString(R.string.notification_no_network_title))
                .setContentText(getString(R.string.notification_no_network_text))
                .build();

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID_NETWORK, notification);
    }

    @Override
    public void removeNetworkOutageNotification() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID_NETWORK);
    }

    private void showNotification() {
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, NowPlayingActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_radio_white_24dp)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(contentIntent)
                .setContent(getSmallContentView())
                .setOngoing(true)
                .build();

        // Set custom expanded view
        notification.bigContentView = getBigContentView();

        // Send the notification.
        startForeground(NOTIFICATION_ID_PLAYER, notification);
    }

    private RemoteViews getSmallContentView() {
        if (mContentViewSmall == null) {
            mContentViewSmall = new RemoteViews(getPackageName(), R.layout.remote_view_music_player_small);
            setUpRemoteView(mContentViewSmall);
        }
        updateRemoteViews(mContentViewSmall);
        return mContentViewSmall;
    }

    private RemoteViews getBigContentView() {
        if (mContentViewBig == null) {
            mContentViewBig = new RemoteViews(getPackageName(), R.layout.remote_view_music_player);
            setUpRemoteView(mContentViewBig);
        }
        updateRemoteViews(mContentViewBig);
        return mContentViewBig;
    }

    private void setUpRemoteView(RemoteViews remoteView) {
        remoteView.setImageViewResource(R.id.image_view_close, R.drawable.ic_close_white_24dp);
        remoteView.setOnClickPendingIntent(R.id.button_close, getPendingIntent(ACTION_STOP_SERVICE));
        remoteView.setOnClickPendingIntent(R.id.button_play_toggle, getPendingIntent(ACTION_PLAY_TOGGLE));
    }

    private void updateRemoteViews(RemoteViews remoteView) {
        if (currentSong != null && currentSong.getArtist() != null) {
            remoteView.setTextViewText(R.id.text_view_name, currentSong.getSongName());
            remoteView.setTextViewText(R.id.text_view_artist, currentSong.getArtist().getArtistName());
        } else {
            remoteView.setTextViewText(R.id.text_view_name, getString(R.string.arcanos_web_rock));
            remoteView.setTextViewText(R.id.text_view_artist, "");
        }

        remoteView.setImageViewResource(R.id.image_view_play_toggle, currentNotificationDrawableId);

        if (currentAlbumArt != null) {
            remoteView.setImageViewBitmap(R.id.image_view_album, currentAlbumArt);
        } else {
            remoteView.setImageViewResource(R.id.image_view_album, R.drawable.arcanos);
        }
    }

    private PendingIntent getPendingIntent(String action) {
        return PendingIntent.getService(this, 0, new Intent(action), 0);
    }
}
