package de.developercity.arcanosradio.services.metadata;

public interface IMetadataService {
    void setDelegate(IMetadataServiceDelegate delegate);

    void startScheduledFetch();

    void cancelScheduledFetch();
}
