package de.developercity.arcanosradio.services.storage;

import bolts.Task;
import de.developercity.arcanosradio.models.Artist;
import de.developercity.arcanosradio.models.Playlist;
import de.developercity.arcanosradio.models.Song;
import de.developercity.arcanosradio.models.UserPreferences;

public interface IStorage {

    Task<Playlist> getCurrentSong();

    Task<Artist> getArtistByName(String name);

    Task<Artist> getArtistByTag(String tag);

    Task<Song> getSongByName(String name);

    Task<Song> getSongByTag(String tag);

    Task<byte[]> getAlbumArtBySong(Song song);

    Task<String> getLyricsBySong(Song song);

    UserPreferences getUserPreferences();

    UserPreferences setUserPreferences(UserPreferences preferences);

    <T> T readConfig(String key);
}
