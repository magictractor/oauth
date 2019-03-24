package uk.co.magictractor.oauth.google;

import java.util.List;

import uk.co.magictractor.oauth.api.OAuthRequest;
import uk.co.magictractor.oauth.api.OAuthResponse;
import uk.co.magictractor.oauth.api.PageTokenServiceIterator;

public abstract class GoogleServiceIterator<E> extends PageTokenServiceIterator<E> {

	@Override
	protected final PageAndNextToken<E> fetchPage(String pageToken) {
		OAuthRequest request = createPageRequest();

		request.setParam("pageToken", pageToken);

		// OAuthResponse response = new OAuth2Connection(MyGooglePhotosApp.getInstance()).request(request);
		
		// TODO! must not have hard coded app in the middle of the iterator!
		OAuthResponse response = MyGooglePhotosApp.getInstance().getConnection().request(request);

		List<? extends E> page = parsePageResponse(response);
		String nextToken = response.getString("nextPageToken");

		return new PageAndNextToken(page, nextToken);
	}
	
	protected abstract OAuthRequest createPageRequest();
	
	protected abstract List<? extends E> parsePageResponse(OAuthResponse response);
	
}
