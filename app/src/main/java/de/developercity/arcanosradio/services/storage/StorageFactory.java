package de.developercity.arcanosradio.services.storage;

import android.content.Context;

public abstract class StorageFactory {

    public static IStorage createStorage(Context context) {
        ParseStorage.configure(context);
        return new ParseStorage();
    }
}