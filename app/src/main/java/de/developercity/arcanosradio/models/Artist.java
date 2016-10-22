package de.developercity.arcanosradio.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import java.util.List;

@ParseClassName("Artist")
public class Artist extends ParseObject {

    public String getArtistName() {
        return getString("artistName");
    }

    public Artist setArtistName(String value) {
        put("artistName", value);
        return this;
    }

    public String getUrl() {
        return getString("url");
    }

    public Artist setUrl(String value) {
        put("url", value);
        return this;
    }

    public String getText() {
        return getString("text");
    }

    public Artist setText(String value) {
        put("text", value);
        return this;
    }

    public List<String> getTags() {
        return getList("tags");
    }

    public Artist setTags(List<String> value) {
        put("tags", value);
        return this;
    }
}
