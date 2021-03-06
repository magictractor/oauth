package uk.co.magictractor.spew.example.google.pojo;

import uk.co.magictractor.spew.photo.Album;

// https://developers.google.com/photos/library/reference/rest/v1/albums#Album
public class GoogleAlbum implements Album {

    private String id;
    // TODO! private??
    private String title;
    // meh - this is not the same as the share link - can the API fetch a share link?
    private String productUrl;
    private int mediaItemsCount;
    private String coverPhotoBaseUrl;
    private String coverPhotoMediaItemId;
    // TODO! shareInfo

    @Override
    public String getServiceProviderId() {
        return id;
    }

    // TODO! Lombok?
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getAlbumUrl() {
        return productUrl;
    }

    @Override
    public String getCoverPhotoBaseUrl() {
        return coverPhotoBaseUrl;
    }

    @Override
    public int getPhotoCount() {
        // TODO! might not all be photos (media item includes video too)
        return mediaItemsCount;
    }

}
