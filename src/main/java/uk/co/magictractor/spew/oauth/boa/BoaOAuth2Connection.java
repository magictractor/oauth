package uk.co.magictractor.spew.oauth.boa;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.QueryStringDecoder;
import uk.co.magictractor.spew.api.OutgoingHttpRequest;
import uk.co.magictractor.spew.api.SpewConnection;
import uk.co.magictractor.spew.api.SpewHttpResponse;
import uk.co.magictractor.spew.api.SpewOAuth2Application;
import uk.co.magictractor.spew.api.SpewOAuth2ServiceProvider;
import uk.co.magictractor.spew.api.connection.AbstractAuthorizationDecoratorConnection;
import uk.co.magictractor.spew.core.response.parser.SpewParsedResponse;
import uk.co.magictractor.spew.core.response.parser.SpewParsedResponseBuilder;
import uk.co.magictractor.spew.core.verification.AuthorizationHandler;
import uk.co.magictractor.spew.core.verification.VerificationFunction;
import uk.co.magictractor.spew.core.verification.VerificationInfo;
import uk.co.magictractor.spew.store.EditableProperty;
import uk.co.magictractor.spew.store.UserPropertyStore;
import uk.co.magictractor.spew.util.BrowserUtil;
import uk.co.magictractor.spew.util.ContentTypeUtil;
import uk.co.magictractor.spew.util.spi.SPIUtil;

// https://tools.ietf.org/html/rfc6749
// https://developers.google.com/identity/protocols/OAuth2
public class BoaOAuth2Connection<SP extends SpewOAuth2ServiceProvider>
        extends AbstractAuthorizationDecoratorConnection<SpewOAuth2Application<SP>, SP> {

    /*
     * milliseconds to remove from expiry to ensure that we refresh if getting
     * close to the server's expiry time
     */
    private static final int EXPIRY_BUFFER = 60 * 1000;

    private final EditableProperty accessToken;
    // milliseconds since start of epoch
    private final EditableProperty accessTokenExpiry;
    private final EditableProperty refreshToken;

    /**
     * Default visibility, applications should obtain instances via
     * {@link BoaConnectionFactory#createConnection}, usually indirectly via
     * OAuthConnectionFactory.
     */
    /* default */ BoaOAuth2Connection(SpewOAuth2Application<SP> application) {
        super(application);

        UserPropertyStore userPropertyStore = SPIUtil.firstAvailable(UserPropertyStore.class);
        this.accessToken = userPropertyStore.getProperty(application, "access_token");
        this.accessTokenExpiry = userPropertyStore.getProperty(application, "access_token_expiry");
        this.refreshToken = userPropertyStore.getProperty(application, "refresh_token");
    }

    @Override
    protected boolean hasExistingAuthorization() {
        return accessToken.getValue() != null;
    }

    @Override
    protected void addAuthorization(OutgoingHttpRequest request) {
        request.addHeader("Authorization", "Bearer " + accessToken.getValue());
    }

    @Override
    protected Instant authorizationExpiry() {
        String expiry = accessTokenExpiry.getValue();
        if (expiry == null) {
            return null;
        }

        long expiryMillis = Long.parseLong(expiry);
        //  return Instant.ofEpochSecond(expirySeconds);
        return Instant.ofEpochMilli(expiryMillis);
    }

    @Override
    protected boolean refreshAuthorization() {
        boolean hasRefreshToken = refreshToken.getValue() != null;
        if (!hasRefreshToken) {
            return false;
        }

        SpewParsedResponse response = fetchRefreshedAccessToken();
        boolean refreshed = response.getStatus() == 200;
        if (!refreshed) {
            getLogger().warn("Failed to refresh the access token: {}", response);
        }

        return refreshed;
    }

    // https://developers.google.com/photos/library/guides/authentication-authorization
    @Override
    public void obtainAuthorization() {
        SpewOAuth2Application<?> application = getApplication();

        OutgoingHttpRequest request = new OutgoingHttpRequest("GET", getServiceProvider().getAuthorizationUri());

        // GitHub returns application/x-www-form-urlencoded content type by default
        request.addHeader(ContentTypeUtil.ACCEPT_HEADER_NAME, ContentTypeUtil.JSON_MIME_TYPES.get(0));

        // A bit mucky. The callback value comes from the handler but is also used in the verification function.
        AuthorizationHandler[] authHandlerHolder = new AuthorizationHandler[1];
        AuthorizationHandler authHandler = application
                .getAuthorizationHandler(
                    () -> new BoaOAuth2VerificationFunction(authHandlerHolder[0].getCallbackValue()));
        authHandlerHolder[0] = authHandler;

        authHandler.preOpenAuthorizationInBrowser(application);

        request.setQueryStringParam("client_id", application.getClientId());
        // Omit for Imgur with "pin"
        request.setQueryStringParam("redirect_uri", authHandler.getCallbackValue());

        // Always "code".
        // "token" type is more appropriate for client-side OAuth.
        // "pin" for Imgur
        request.setQueryStringParam("response_type", "code");

        request.setQueryStringParam("scope", application.getScope());

        application.modifyAuthorizationRequest(request);

        String authUri = request.getUrl();
        BrowserUtil.openBrowserTab(authUri);

        authHandler.postOpenAuthorizationInBrowser(application);
    }

    // TODO! set and check randomised "state" value : https://developers.google.com/identity/protocols/OpenIDConnect#server-flow
    private boolean verify(VerificationInfo verificationInfo, String callback) {
        boolean verified = false;
        try {
            fetchAccessAndRefreshToken(verificationInfo.getVerificationCode(), callback);
            verified = true;
        }
        catch (Exception e) {
            // Do nothing. verified is false.
            System.err.println("verification failed");
            e.printStackTrace(System.err);
        }

        return verified;
    }

    private void fetchAccessAndRefreshToken(String code, String callback) {
        SpewOAuth2Application<?> application = getApplication();

        // ah! needed to be POST else 404 (Google)
        OutgoingHttpRequest request = new OutgoingHttpRequest("POST", getServiceProvider().getTokenUri());

        // GitHub returns application/x-www-form-urlencoded content type by default
        request.addHeader(ContentTypeUtil.ACCEPT_HEADER_NAME, ContentTypeUtil.JSON_MIME_TYPES.get(0));

        HashMap<String, String> bodyData = new HashMap<>();
        bodyData.put("code", code);
        bodyData.put("client_id", application.getClientId());
        bodyData.put("client_secret", application.getClientSecret());
        bodyData.put("grant_type", "authorization_code");
        bodyData.put("redirect_uri", callback);

        application.modifyTokenRequest(request);

        SpewParsedResponse response = authRequest(request, bodyData);

        // TODO! only get a refresh_token if auth request included access_type=offline
        // add that or not? perhaps make it configurable in application
        String refreshTokenValue = response.getString("refresh_token");
        if (refreshTokenValue != null) {
            refreshToken.setValue(refreshTokenValue);
        }

        // accessToken.setValue(response.getString("access_token"));
        // System.err.println("access_token set to " + accessToken.getValue());
        setAccessToken((key) -> response.getString(key));
    }

    // TODO! handle invalid/expired refresh tokens
    // https://developers.google.com/identity/protocols/OAuth2InstalledApp#offline
    private SpewParsedResponse fetchRefreshedAccessToken() {
        OutgoingHttpRequest request = new OutgoingHttpRequest("POST", getServiceProvider().getTokenUri());

        HashMap<String, String> bodyData = new LinkedHashMap<>();
        bodyData.put("refresh_token", refreshToken.getValue());
        bodyData.put("client_id", getApplication().getClientId());
        bodyData.put("client_secret", getApplication().getClientSecret());
        bodyData.put("grant_type", "refresh_token");

        SpewParsedResponse response = authRequest(request, bodyData);

        System.err.println("fetchRefreshedAccessToken(): " + response);

        setAccessToken((key) -> response.getString(key));

        return response;
    }

    private void setAccessToken(FullHttpMessage httpMessage) {
        ByteBuf content = httpMessage.content();
        // new QueryStringDecoder()

        String s = content.toString(Charsets.UTF_8);
        System.err.println("content: " + s);

        QueryStringDecoder d = new QueryStringDecoder(s, Charsets.UTF_8, false);
        Map<String, List<String>> parameters = d.parameters();
        System.err.println(parameters);

        setAccessToken((key) -> Iterables.getOnlyElement(parameters.get(key)));
    }

    private void setAccessToken(Function<String, String> valueMap) {
        accessToken.setValue(valueMap.apply("access_token"));

        // typically 3600 for one hour
        String expiresInSecondsString = valueMap.apply("expires_in");
        if (expiresInSecondsString != null) {
            int expiresInSeconds = Integer.parseInt(valueMap.apply("expires_in"));
            // TODO! store seconds not millis
            long expiresInMilliseconds = expiresInSeconds * 1000;
            long expiry = System.currentTimeMillis() + expiresInMilliseconds - EXPIRY_BUFFER;
            accessTokenExpiry.setValue(Long.toString(expiry));

            long diff = expiry - new Date().getTime();
        }

        // some service providers update the refresh token, others do not
        String newRefreshToken = valueMap.apply("refresh_token");
        if (newRefreshToken != null) {
            refreshToken.setValue(newRefreshToken);
        }

        System.err.println("accessToken set: " + accessToken.getValue() + " expires " + accessTokenExpiry.getValue());
    }

    //  {
    //        "access_token": "ya29.GltcBrSQ1GX2N6sN57ktc1smgmocYpP1MKgn_wPkJRpu0KcTxgDNW7r4UBg3w3rK0J6B3tQI-OjIgFuHDXBmY3a4--7Jj3qy6saDIYbrdYobv3jVxrMA4B3hEdGn",
    //        "expires_in": 3600,
    //        "refresh_token": "1/gLkG1sNlUr3U3T-TVWtX37jOe40f6eQvgoFLG_26mfs",
    //        "scope": "https://www.googleapis.com/auth/photoslibrary https://www.googleapis.com/auth/photoslibrary.sharing",
    //        "token_type": "Bearer"
    //      }

    private SpewParsedResponse authRequest(OutgoingHttpRequest request, HashMap<String, String> bodyData) {
        request.addHeader(ContentTypeUtil.CONTENT_TYPE_HEADER_NAME, ContentTypeUtil.FORM_MIME_TYPE);

        String body = Joiner.on('&').withKeyValueSeparator('=').join(bodyData);
        request.setBody(body.getBytes(StandardCharsets.UTF_8));

        SpewHttpResponse response = request(request);
        return new SpewParsedResponseBuilder(getApplication(), response).build();
    }

    private class BoaOAuth2VerificationFunction implements VerificationFunction {

        private final String callback;

        public BoaOAuth2VerificationFunction(String callback) {
            this.callback = callback;
        }

        @Override
        public Boolean apply(VerificationInfo info) {
            return BoaOAuth2Connection.this.verify(info, callback);
        }

        @Override
        public SpewConnection getConnection() {
            return BoaOAuth2Connection.this;
        }
    }

}
