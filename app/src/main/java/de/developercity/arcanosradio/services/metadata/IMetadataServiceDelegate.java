package de.developercity.arcanosradio.services.metadata;

import de.developercity.arcanosradio.models.Playlist;

public interface IMetadataServiceDelegate {
    void onSongMetadataFetched(final Playlist playlist);

    void onSongAlbumArtFetched(final byte[] albumArt);

    void onSongLyricsFetched(final String lyrics);
}
