package de.developercity.arcanosradio.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Playlist")
public class Playlist extends ParseObject {

    public String getTitle() {
        return getString("title");
    }

    public Playlist setTitle(String value) {
        put("title", value);
        return this;
    }

    public Song getSong() {
        return  (Song)getParseObject("song");
    }

    public Playlist setSong(Song value) {
        put("song", value);
        return this;
    }
}
