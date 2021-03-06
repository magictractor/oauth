package uk.co.magictractor.spew.provider.google;

import java.util.Iterator;
import java.util.List;

import uk.co.magictractor.spew.api.ApplicationRequest;
import uk.co.magictractor.spew.api.SpewApplication;
import uk.co.magictractor.spew.core.response.parser.SpewParsedResponse;
import uk.co.magictractor.spew.example.google.MyGooglePhotosApp;
import uk.co.magictractor.spew.example.google.pojo.GoogleAlbum;
import uk.co.magictractor.spew.photo.Album;

public class GoogleSharedAlbumsIterator<E> extends GoogleServiceIterator<E> {

    private GoogleSharedAlbumsIterator() {
    }

    @Override
    protected ApplicationRequest createPageRequest() {
        // This would include unshared albums
        // https://developers.google.com/photos/library/reference/rest/v1/albums/list
        // return new OAuthRequest("https://photoslibrary.googleapis.com/v1/albums");

        // https://developers.google.com/photos/library/reference/rest/v1/sharedAlbums/list
        return createGetRequest("https://photoslibrary.googleapis.com/v1/sharedAlbums");
    }

    @Override
    protected List<E> parsePageResponse(SpewParsedResponse response) {
        return response.getList("sharedAlbums", getElementType());
    }

    public static class GoogleSharedAlbumsIteratorBuilder<E> extends
            GoogleServiceIteratorBuilder<E, GoogleSharedAlbumsIterator<E>, GoogleSharedAlbumsIteratorBuilder<E>> {

        public GoogleSharedAlbumsIteratorBuilder(SpewApplication<Google> application, Class<E> elementType) {
            super(application, elementType, new GoogleSharedAlbumsIterator<>());
        }

    }

    // https://developers.google.com/photos/library/reference/rest/v1/albums#Album
    public static void main(String[] args) {
        Iterator<GoogleAlbum> iter = new GoogleSharedAlbumsIteratorBuilder<>(MyGooglePhotosApp.get(), GoogleAlbum.class)
                .build();
        while (iter.hasNext()) {
            Album album = iter.next();
            System.err.println(
                album.getTitle() + " " + album.getServiceProviderId() + " " + album.getAlbumUrl() + " "
                        + album.getPhotoCount());
        }
    }
}
