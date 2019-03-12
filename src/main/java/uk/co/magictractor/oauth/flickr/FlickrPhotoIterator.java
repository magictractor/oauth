package uk.co.magictractor.oauth.flickr;

import java.util.List;

import uk.co.magictractor.oauth.api.OAuthApplication;
import uk.co.magictractor.oauth.api.OAuthRequest;
import uk.co.magictractor.oauth.api.OAuthResponse;
import uk.co.magictractor.oauth.api.PageCountServiceIterator;
import uk.co.magictractor.oauth.flickr.pojo.FlickrPhoto;
import uk.co.magictractor.oauth.flickr.pojo.FlickrPhotos;

// Use flickr.photos.search rather than flickr.people.getPhotos because it allows sort order to be specified
public class FlickrPhotoIterator extends PageCountServiceIterator<FlickrPhoto> {

	// min_taken_date (Optional)
	// Minimum taken date. Photos with an taken date greater than or equal to this
	// value will be returned. The date can be in the form of a mysql datetime or
	// unix timestamp.

	public FlickrPhotoIterator(OAuthApplication application) {
		super(application);
	}
	
	@Override
	protected List<FlickrPhoto> fetchPage(int pageNumber) {
		OAuthRequest request = new OAuthRequest(Flickr.REST_ENDPOINT);

		request.setParam("method", "flickr.photos.search");

		request.setParam("user_id", "me");
		request.setParam("sort", "date-taken-desc");
		request.setParam("page", pageNumber);
		// default is 100, max is 500
		request.setParam("per_page", 500);

		//request.setParam("min_taken_date", "2018-10-10");
		// request.setParam("min_taken_date", "2018-10-06");

		// machine_tags are no auto tags
		// https://www.flickr.com/groups/51035612836@N01/discuss/72157594497877875/
		request.setParam("extras", "date_upload,date_taken,description,tags,machine_tags");
		// request.setParam("extras", ALL_EXTRAS);
		OAuthResponse response = getConnection().request(request);

		// https://stackoverflow.com/questions/13686284/parsing-jsonobject-to-listmapstring-object-using-gson

		System.err.println(response);

		// works, but want counts too
//		TypeRef<List<Photo>> type = new TypeRef<List<Photo>>() {
//		};
//		List<Photo> photos = response.getObject("photos.photo", type);
		// photo -> ArrayList
		// return new ArrayList<Photo>(photos.values());

		FlickrPhotos photos = response.getObject("photos", FlickrPhotos.class);
		setTotalItemCount(photos.total);
		setTotalPageCount(photos.pages);

		return photos.photo;
	}

	public static void main(String[] args) {
		FlickrPhotoIterator iter = new FlickrPhotoIterator(MyFlickrApp.getInstance());
		while (iter.hasNext()) {
			System.err.println(iter.next().getTitle());
		}
	}
}