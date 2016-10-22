package de.developercity.arcanosradio.services.generics;

import android.app.Service;
import android.content.ComponentName;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

public class ServiceConnection implements IServiceConnection {
    private Service service;
    private List<IServiceConnectionDelegate> delegates = new ArrayList<>();

    public List<IServiceConnectionDelegate> getDelegates() {
        return delegates;
    }

    public void addDelegate(IServiceConnectionDelegate delegate) {
        delegates.add(delegate);
    }

    public void removeDelegate(IServiceConnectionDelegate delegate) {
        delegates.remove(delegate);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.service = ((LocalBinder) service).getService();
        for (IServiceConnectionDelegate delegate : delegates) {
            delegate.onConnect(this.service);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        for (IServiceConnectionDelegate delegate : delegates) {
            delegate.onDisconnect(service);
        }
    }
}
