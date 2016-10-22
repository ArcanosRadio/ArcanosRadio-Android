package de.developercity.arcanosradio.services.notification;

public abstract class NotificationFactory {
    private static Class serviceClass = NotificationService.class;

    public static Class getServiceType() {
        return serviceClass;
    }
}
