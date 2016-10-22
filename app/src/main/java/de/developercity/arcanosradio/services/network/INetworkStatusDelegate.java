package de.developercity.arcanosradio.services.network;

public interface INetworkStatusDelegate {
    void onNetworkDisconnected();

    void onMobileConnected();

    void onWifiConnected();
}
