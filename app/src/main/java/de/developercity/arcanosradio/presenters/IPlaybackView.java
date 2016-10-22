package de.developercity.arcanosradio.presenters;

import android.graphics.Bitmap;

import de.developercity.arcanosradio.models.Song;

public interface IPlaybackView {
    void setPresenter(IPlaybackPresenter presenter);

    void setPlayToggleDrawablePause();

    void setPlayToggleDrawableBuffer();

    void setPlayToggleDrawablePlay();

    void setPlayToggleDrawableNoConnection();

    void alertNoConnection();

    void setDefaultMetadata();

    void setSongMetadata(final Song song);

    void setAlbumArt(final Bitmap albumArt);

    void setLyrics(final String lyrics);
}
