package de.developercity.arcanosradio.services.playback.remote;

public abstract class RemoteControlFactory {

    private static Class serviceClass = RemoteControlService.class;

    public static Class getServiceType() {
        return serviceClass;
    }
}
