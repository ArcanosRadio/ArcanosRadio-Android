package de.developercity.arcanosradio.services.notification;

import android.graphics.Bitmap;

import de.developercity.arcanosradio.models.Song;

public interface INotificationService {
    void setDelegate(INotificationServiceDelegate delegate);

    void setDefaultMetadata();

    void updateNotificationMetadata(Song song);

    void updateNotificationAlbumArt(Bitmap albumArt);

    void updateNotificationTogglePause();

    void updateNotificationToggleBuffer();

    void updateNotificationTogglePlay();

    void updateNotificationToggleNoConnection();

    void sendNetworkOutageNotification();

    void removeNetworkOutageNotification();
}
