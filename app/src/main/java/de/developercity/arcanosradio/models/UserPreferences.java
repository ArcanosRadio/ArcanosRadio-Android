package de.developercity.arcanosradio.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("UserPreferences")
public class UserPreferences extends ParseObject {

    public boolean getMobileDataStreamingEnabled() {
        return getBoolean("mobileDataStreamingEnabled");
    }

    public UserPreferences setMobileDataStreamingEnabled(Boolean value) {
        put("mobileDataStreamingEnabled", value);
        return this;
    }

    public boolean getKeepScreenOnEnabled() {
        return getBoolean("KeepScreenOnEnabled");
    }

    public UserPreferences setKeepScreenOnEnabled(Boolean value) {
        put("KeepScreenOnEnabled", value);
        return this;
    }
}
