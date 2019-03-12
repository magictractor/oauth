package uk.co.magictractor.oauth.google;

import java.util.List;

import com.jayway.jsonpath.TypeRef;

import uk.co.magictractor.oauth.api.OAuthRequest;
import uk.co.magictractor.oauth.api.OAuthResponse;
import uk.co.magictractor.oauth.common.Album;


public class GoogleSharedAlbumsIterator extends GoogleServiceIterator<Album> {

	@Override
	protected OAuthRequest createPageRequest() {
		// This would include unshared albums
		// https://developers.google.com/photos/library/reference/rest/v1/albums/list
		//return new OAuthRequest("https://photoslibrary.googleapis.com/v1/albums");
		
		// https://developers.google.com/photos/library/reference/rest/v1/sharedAlbums/list
		return new OAuthRequest("https://photoslibrary.googleapis.com/v1/sharedAlbums");
	}

	@Override
	protected List<GoogleAlbum> parsePageResponse(OAuthResponse response) {
		return response.getObject("sharedAlbums", new TypeRef<List<GoogleAlbum>>() {
		});
	}

	// https://developers.google.com/photos/library/reference/rest/v1/albums#Album
	public static void main(String[] args) {
		GoogleSharedAlbumsIterator iter = new GoogleSharedAlbumsIterator();
		while (iter.hasNext()) {
			Album album = iter.next();
			System.err.println(
					album.getTitle() + " " + album.getId() + " " + album.getAlbumUrl() + " " + album.getPhotoCount());
		}
	}
}