package uk.co.magictractor.spew.processor.common;

import java.util.Collection;
import java.util.LinkedHashMap;

import uk.co.magictractor.spew.photo.Media;
import uk.co.magictractor.spew.processor.ProcessorContext;

public class MediaProcessorContext implements ProcessorContext<Media, MutableMedia> {

    private LinkedHashMap<String, MutableAlbum> albums = new LinkedHashMap<>();

    @Override
    public MutableMedia beforeElement(Media photo) {
        return new MutableMedia(photo);
    }

    @Override
    public void afterElement(MutableMedia photo) {
    }

    public MutableAlbum getAlbum(String albumTitle) {
        MutableAlbum album = albums.get(albumTitle);
        if (album == null) {
            album = new MutableAlbum(albumTitle);
            albums.put(albumTitle, album);
        }
        return album;
    }

    public boolean hasAlbum(String title) {
        return albums.containsKey(title);
    }

    public Collection<MutableAlbum> getAlbums() {
        return albums.values();
    }

}
