package uk.co.magictractor.oauth.google;

import uk.co.magictractor.oauth.api.OAuth2Application;
import uk.co.magictractor.oauth.api.OAuth2AuthorizeResponseType;
import uk.co.magictractor.oauth.api.OAuth2ServiceProvider;
import uk.co.magictractor.oauth.processor.properties.ResourceFileProperties;

public class MyGooglePhotosApp implements OAuth2Application {

    private final ResourceFileProperties properties = new ResourceFileProperties(MyGooglePhotosApp.class);

    @Override
    public OAuth2ServiceProvider getServiceProvider() {
        return Google.getInstance();
    }

    @Override
    public String getScope() {
        // return "https://www.googleapis.com/auth/photoslibrary";

        // Do not need https://www.googleapis.com/auth/photoslibrary.sharing
        // just use sharedAlbums/list to get list of shared albums
        // Ah! do want album share info in order to get share Url
        return "https://www.googleapis.com/auth/photoslibrary https://www.googleapis.com/auth/photoslibrary.sharing";

        // just sharing results in permission denied when listing albums
        // return "https://www.googleapis.com/auth/photoslibrary.sharing";
    }

    @Override
    public OAuth2AuthorizeResponseType defaultAuthorizeResponseType() {
        return OAuth2AuthorizeResponseType.CODE;
    }

    @Override
    public String getClientId() {
        return properties.getProperty("client_id");
    }

    @Override
    public String getClientSecret() {
        return properties.getProperty("client_secret");
    }
}
