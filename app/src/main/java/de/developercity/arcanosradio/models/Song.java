package de.developercity.arcanosradio.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import java.util.List;

@ParseClassName("Song")
public class Song extends ParseObject {

    public String getSongName() {
        return getString("songName");
    }

    public Song setSongName(String value) {
        put("songName", value);
        return this;
    }

    public Artist getArtist() {
        return  (Artist)getParseObject("artist");
    }

    public Song setArtist(Artist value) {
        put("artist", value);
        return this;
    }

    public ParseFile getAlbumArt() {
        return getParseFile("albumArt");
    }

    public Song setAlbumArt(ParseFile value) {
        put("albumArt", value);
        return this;
    }

    public ParseFile getLyrics() {
        return getParseFile("lyrics");
    }

    public Song setLyrics(ParseFile value) {
        put("lyrics", value);
        return this;
    }

    public List<String> getTags() {
        return getList("tags");
    }

    public Song setTags(List<String> value) {
        put("tags", value);
        return this;
    }
}
