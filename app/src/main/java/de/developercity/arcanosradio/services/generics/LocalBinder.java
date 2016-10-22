package de.developercity.arcanosradio.services.generics;

import android.app.Service;
import android.os.Binder;

public class LocalBinder extends Binder {
    private Service instance;

    public LocalBinder(Service instance) {
        this.instance = instance;
    }

    public Service getService() {
        return instance;
    }
}
