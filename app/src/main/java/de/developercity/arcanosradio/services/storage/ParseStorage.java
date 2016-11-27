package de.developercity.arcanosradio.services.storage;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.http.ParseHttpRequest;
import com.parse.http.ParseHttpResponse;
import com.parse.http.ParseNetworkInterceptor;

import java.io.IOException;

import bolts.Continuation;
import bolts.Task;
import de.developercity.arcanosradio.Constants;
import de.developercity.arcanosradio.models.Artist;
import de.developercity.arcanosradio.models.Playlist;
import de.developercity.arcanosradio.models.Song;
import de.developercity.arcanosradio.models.UserPreferences;

public class ParseStorage implements IStorage {

    static void configure(Context context) {
        ParseObject.registerSubclass(Artist.class);
        ParseObject.registerSubclass(Song.class);
        ParseObject.registerSubclass(Playlist.class);
        ParseObject.registerSubclass(UserPreferences.class);

        Parse.Configuration parseConfig = new Parse.Configuration.Builder(context)
                .addNetworkInterceptor(new ParseNetworkInterceptor() {
                    @Override
                    public ParseHttpResponse intercept(Chain chain) throws IOException {
                        ParseHttpRequest req = chain.getRequest();
                        if (req.getUrl().startsWith("http://")) {

                            req = new ParseHttpRequest.Builder(req)
                                    .setUrl(req.getUrl()
                                        .replace("http://", "https://"))
                                    .build();
                        }
                        return chain.proceed(req);
                    }
                })
                .applicationId(Constants.PARSE_APP)
                .clientKey(Constants.PARSE_CLIENT_KEY)
                .server(Constants.PARSE_URL)
                .enableLocalDataStore()
                .build();

        Parse.initialize(parseConfig);
        try {
            ParseConfig.get();
        } catch (ParseException e) {
            Crashlytics.logException(e);
        }
    }

    public Task<Playlist> getCurrentSong() {
        ParseQuery<Playlist> query =
                ParseQuery.getQuery(Playlist.class)
                        .orderByDescending("createdAt")
                        .include("song.artist")
                        .setLimit(1);

        return query.getFirstInBackground();
    }

    public Task<Artist> getArtistByName(String name) {
        ParseQuery<Artist> query =
                ParseQuery.getQuery(Artist.class)
                        .whereEqualTo("artistName", name)
                        .setLimit(1);

        return query.getFirstInBackground();
    }

    public Task<Artist> getArtistByTag(String tag) {
        ParseQuery<Artist> query =
                ParseQuery.getQuery(Artist.class)
                        .whereContains("tags", tag)
                        .setLimit(1);

        return query.getFirstInBackground();
    }

    public Task<Song> getSongByName(String name) {
        ParseQuery<Song> query =
                ParseQuery.getQuery(Song.class)
                        .whereEqualTo("songName", name)
                        .setLimit(1);

        return query.getFirstInBackground();
    }

    public Task<Song> getSongByTag(String tag) {
        ParseQuery<Song> query =
                ParseQuery.getQuery(Song.class)
                        .whereContains("tags", tag)
                        .setLimit(1);

        return query.getFirstInBackground();
    }

    public Task<byte[]> getAlbumArtBySong(Song song) {
        return song.getAlbumArt().getDataInBackground()
                .continueWith(new Continuation<byte[], byte[]>() {
                    @Override
                    public byte[] then(Task<byte[]> task) throws Exception {
                        return task.getResult();
                    }
                });
    }

    public Task<String> getLyricsBySong(Song song) {
        return song.getLyrics().getDataInBackground()
                .continueWith(new Continuation<byte[], String>() {
                    @Override
                    public String then(Task<byte[]> task) throws Exception {
                        byte[] lyricsData = task.getResult();
                        String lyricsText = new String(lyricsData);
                        return lyricsText;
                    }
                });
    }

    @Override
    public UserPreferences getUserPreferences() {
        try {
            return ParseQuery.getQuery(UserPreferences.class)
                    .fromLocalDatastore()
                    .getFirst();
        } catch (ParseException e) {
            UserPreferences newPreferences = new UserPreferences();
            newPreferences.setMobileDataStreamingEnabled(true);
            newPreferences.setKeepScreenOnEnabled(false);

            return setUserPreferences(newPreferences);
        }
    }

    @Override
    public UserPreferences setUserPreferences(UserPreferences preferences) {
        try {
            preferences.pin();
            return getUserPreferences();
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public <T> T readConfig(String key) {
        return (T)ParseConfig.getCurrentConfig().get(key);
    }
}
