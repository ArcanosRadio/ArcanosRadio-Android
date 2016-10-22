package de.developercity.arcanosradio.services.generics;

public interface IServiceConnectionDelegate<T> {
    void onConnect(T service);

    void onDisconnect(T service);
}
