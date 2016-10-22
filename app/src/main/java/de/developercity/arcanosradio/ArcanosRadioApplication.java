package de.developercity.arcanosradio;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import de.developercity.arcanosradio.services.generics.IServiceConnection;
import de.developercity.arcanosradio.services.generics.ServiceConnection;
import de.developercity.arcanosradio.services.storage.IStorage;
import de.developercity.arcanosradio.services.storage.StorageFactory;
import io.fabric.sdk.android.Fabric;

public class ArcanosRadioApplication extends Application {
    private static final String TAG = ArcanosRadioApplication.class.getSimpleName();
    private static ArcanosRadioApplication app;
    private static final Object syncLock = new Object();
    private static IStorage storage = null;
    private static IServiceConnection metadataServiceConnection = null;
    private static IServiceConnection notificationServiceConnection = null;
    private static IServiceConnection remoteControlServiceConnection = null;

    public ArcanosRadioApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;

        Fabric.with(this, new Crashlytics());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static IStorage getStorage() {
        if (storage == null) {
            synchronized (syncLock) {
                if (storage == null) {
                    storage = StorageFactory.createStorage(app.getApplicationContext());
                }
            }
        }
        return storage;
    }

    public static IServiceConnection getMetadataServiceConnection() {
        if (metadataServiceConnection == null) {
            synchronized (syncLock) {
                if (metadataServiceConnection == null) {
                    metadataServiceConnection = new ServiceConnection();
                }
            }
        }
        return metadataServiceConnection;
    }

    public static IServiceConnection getNotificationsServiceConnection() {
        if (notificationServiceConnection == null) {
            synchronized (syncLock) {
                if (notificationServiceConnection == null) {
                    notificationServiceConnection = new ServiceConnection();
                }
            }
        }
        return notificationServiceConnection;
    }

    public static IServiceConnection getRemoteControlServiceConnection() {
        if (remoteControlServiceConnection == null) {
            synchronized (syncLock) {
                if (remoteControlServiceConnection == null) {
                    remoteControlServiceConnection = new ServiceConnection();
                }
            }
        }
        return remoteControlServiceConnection;
    }
}