package uk.co.magictractor.oauth.flickr;

import uk.co.magictractor.oauth.api.OAuth1Application;
import uk.co.magictractor.oauth.api.OAuth1ServiceProvider;
import uk.co.magictractor.oauth.processor.properties.ResourceFileProperties;

public class MyFlickrApp implements OAuth1Application {

    private final ResourceFileProperties properties = new ResourceFileProperties(MyFlickrApp.class);

    @Override
    public OAuth1ServiceProvider getServiceProvider() {
        return Flickr.getInstance();
    }

    @Override
    public String getAppToken() {
        return properties.getProperty("app_token");
    }

    @Override
    public String getAppSecret() {
        return properties.getProperty("app_secret");
    }
}
