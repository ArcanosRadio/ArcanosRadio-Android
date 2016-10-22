package de.developercity.arcanosradio.services.metadata;

public abstract class MetadataFactory {

    private static Class serviceClass = MetadataPoolingService.class;

    public static Class getServiceType() {
        return serviceClass;
    }
}
