package de.developercity.arcanosradio.services.generics;

import android.content.ServiceConnection;

import java.util.List;

public interface IServiceConnection extends ServiceConnection {
    List<IServiceConnectionDelegate> getDelegates();

    void addDelegate(IServiceConnectionDelegate delegate);

    void removeDelegate(IServiceConnectionDelegate delegate);
}
