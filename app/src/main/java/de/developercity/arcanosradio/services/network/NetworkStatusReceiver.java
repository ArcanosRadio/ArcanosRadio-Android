package de.developercity.arcanosradio.services.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import de.developercity.arcanosradio.ArcanosRadioApplication;

public class NetworkStatusReceiver extends BroadcastReceiver {
    private INetworkStatusDelegate delegate;

    private static boolean isConnected;
    private static boolean isWifi;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        hasConnection(context);

        if (delegate != null) {
            if (isConnected && isWifi) {
                delegate.onWifiConnected();
            } else if (isConnected && !isWifi) {
                delegate.onMobileConnected();
            } else {
                delegate.onNetworkDisconnected();
            }
        }
    }

    public void setDelegate(INetworkStatusDelegate delegate) {
        this.delegate = delegate;
    }

    public static boolean hasConnection(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        isConnected = netInfo != null && netInfo.isConnectedOrConnecting();
        isWifi = netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI;

        if (ArcanosRadioApplication.getStorage().getUserPreferences().getMobileDataStreamingEnabled()) {
            return isConnected;
        } else {
            return isWifi;
        }
    }
}
