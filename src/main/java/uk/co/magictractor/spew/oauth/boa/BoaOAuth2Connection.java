package uk.co.magictractor.spew.oauth.boa;

import static uk.co.magictractor.spew.api.HttpHeaderNames.ACCEPT;
import static uk.co.magictractor.spew.api.HttpHeaderNames.AUTHORIZATION;
import static uk.co.magictractor.spew.api.HttpHeaderNames.CONTENT_TYPE;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
import uk.co.magictractor.spew.api.SpewConnectionConfiguration;
import uk.co.magictractor.spew.api.SpewHttpResponse;
import uk.co.magictractor.spew.api.SpewOAuth2Configuration;
import uk.co.magictractor.spew.api.connection.AbstractAuthorizationDecoratorConnection;
import uk.co.magictractor.spew.api.connection.SpewConnectionVerificationPendingCache;
import uk.co.magictractor.spew.core.response.parser.SpewParsedResponse;
import uk.co.magictractor.spew.core.response.parser.SpewParsedResponseBuilder;
import uk.co.magictractor.spew.core.verification.AuthVerificationHandler;
import uk.co.magictractor.spew.server.SpewHttpRequest;
import uk.co.magictractor.spew.store.user.UserPropertyStore;
import uk.co.magictractor.spew.util.BrowserUtil;
import uk.co.magictractor.spew.util.ContentTypeUtil;
import uk.co.magictractor.spew.util.RandomUtil;
import uk.co.magictractor.spew.util.spi.SPIUtil;

// https://tools.ietf.org/html/rfc6749
// https://developers.google.com/identity/protocols/OAuth2
public class BoaOAuth2Connection extends AbstractAuthorizationDecoratorConnection<SpewOAuth2Configuration> {

    /**
     * Seconds to remove from expiry to ensure that we refresh if getting close
     * to the server's expiry time
     */
    private static final int EXPIRY_BUFFER = 60;

    /**
     * Default visibility, applications should obtain instances via
     * {@link BoaConnectionFactory#createConnection}, usually indirectly via
     * OAuthConnectionFactory.
     */
    /* default */ BoaOAuth2Connection(SpewOAuth2Configuration configuration) {
        super(configuration);

        UserPropertyStore userPropertyStore = SPIUtil.firstAvailable(UserPropertyStore.class);
    }

    @Override
    protected boolean hasExistingAuthorization() {
        return getConfiguration().getAccessTokenProperty().getValue() != null;
    }

    @Override
    protected void addAuthorization(OutgoingHttpRequest request) {
        request.setHeader(AUTHORIZATION, "Bearer " + getConfiguration().getAccessTokenProperty().getValue());
    }

    @Override
    protected Instant authorizationExpiry() {
        String expiry = getConfiguration().getAccessTokenExpiryProperty().getValue();
        if (expiry == null) {
            return null;
        }

        long expirySeconds = Long.parseLong(expiry);
        return Instant.ofEpochSecond(expirySeconds);
    }

    @Override
    protected boolean refreshAuthorization() {
        boolean hasRefreshToken = getConfiguration().getRefreshTokenProperty().getValue() != null;
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
        SpewOAuth2Configuration configuration = getConfiguration();

        OutgoingHttpRequest request = new OutgoingHttpRequest("GET", configuration.getAuthorizationUri());

        // GitHub returns application/x-www-form-urlencoded content type by default
        request.setHeader(ACCEPT, ContentTypeUtil.JSON_MIME_TYPES.get(0));

        AuthVerificationHandler authHandler = configuration.createAuthVerificationHandler();

        authHandler.preOpenAuthorizationInBrowser();

        request.setQueryStringParam("client_id", configuration.getClientId());
        // Omit for Imgur with "pin"
        request.setQueryStringParam("redirect_uri", authHandler.getRedirectUri());

        // Usually "code".
        // "token" type is more appropriate for OAuth with in a browser.
        // "pin" for Imgur out of band, changed using modifyAuthorizationRequest();
        request.setQueryStringParam("response_type", "code");

        request.setQueryStringParam("scope", configuration.getScope());

        // This gets passed back to the verifier
        String state = hashCode() + "-" + RandomUtil.nextBigPositiveLong();
        request.setQueryStringParam("state", state);

        SpewConnectionVerificationPendingCache.addVerificationPending(
            req -> state.equals(req.getQueryStringParam("state").orElse(null)), this);

        configuration.modifyAuthorizationRequest(request);

        String authUri = request.getUrl();
        BrowserUtil.openBrowserTab(authUri);

        authHandler.postOpenAuthorizationInBrowser();
    }

    @Override
    public boolean verifyAuthorization(SpewHttpRequest request) {
        String verificationCode = request.getQueryStringParam("code").get();

        return verifyAuthorization(verificationCode);
    }

    @Override
    public boolean verifyAuthorization(String verificationCode) {
        SpewOAuth2Configuration configuration = getConfiguration();

        // TODO! perhaps encode the redirectUri with the state so that it can be extracted from callbacks.
        String redirectUri = configuration.createAuthVerificationHandler().getRedirectUri();

        // ah! needed to be POST else 404 (Google)
        OutgoingHttpRequest request = new OutgoingHttpRequest("POST", configuration.getTokenUri());

        // GitHub returns application/x-www-form-urlencoded content type by default
        request.setHeader(ACCEPT, ContentTypeUtil.JSON_MIME_TYPES.get(0));

        HashMap<String, String> bodyData = new HashMap<>();
        bodyData.put("code", verificationCode);
        bodyData.put("client_id", configuration.getClientId());
        bodyData.put("client_secret", configuration.getClientSecret());
        // Hmm. Twitter only supports "client_credentials".
        // https://developer.twitter.com/en/docs/basics/authentication/api-reference/token
        bodyData.put("grant_type", "authorization_code");
        bodyData.put("redirect_uri", redirectUri);

        configuration.modifyTokenRequest(request, bodyData);

        SpewParsedResponse response = authRequest(request, bodyData);

        boolean verified = response.getStatus() == 200;
        if (!verified) {
            return false;
        }

        // TODO! only get a refresh_token if auth request included access_type=offline
        // add that or not? perhaps make it configurable in application
        String refreshTokenValue = response.getString("refresh_token");
        if (refreshTokenValue != null) {
            getConfiguration().getRefreshTokenProperty().setValue(refreshTokenValue);
        }

        // accessToken.setValue(response.getString("access_token"));
        // System.err.println("access_token set to " + accessToken.getValue());
        setAccessToken((key) -> response.getString(key));

        return true;
    }

    // TODO! handle invalid/expired refresh tokens
    // https://developers.google.com/identity/protocols/OAuth2InstalledApp#offline
    private SpewParsedResponse fetchRefreshedAccessToken() {
        OutgoingHttpRequest request = new OutgoingHttpRequest("POST", getConfiguration().getTokenUri());

        HashMap<String, String> bodyData = new LinkedHashMap<>();
        bodyData.put("refresh_token", getConfiguration().getRefreshTokenProperty().getValue());
        bodyData.put("client_id", getConfiguration().getClientId());
        bodyData.put("client_secret", getConfiguration().getClientSecret());
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
        getConfiguration().getAccessTokenProperty().setValue(valueMap.apply("access_token"));

        // typically 3600 for one hour
        String expiresInString = valueMap.apply("expires_in");
        if (expiresInString != null) {
            int expiresIn = Integer.parseInt(valueMap.apply("expires_in"));
            long now = System.currentTimeMillis() / 1000;
            long expiry = now + expiresIn - EXPIRY_BUFFER;
            getConfiguration().getAccessTokenExpiryProperty().setValue(Long.toString(expiry));
        }

        // some service providers update the refresh token, others do not
        String newRefreshToken = valueMap.apply("refresh_token");
        if (newRefreshToken != null) {
            getConfiguration().getRefreshTokenProperty().setValue(newRefreshToken);
        }
    }

    //  {
    //        "access_token": "ya29.GltcBrSQ1GX2N6sN57ktc1smgmocYpP1MKgn_wPkJRpu0KcTxgDNW7r4UBg3w3rK0J6B3tQI-OjIgFuHDXBmY3a4--7Jj3qy6saDIYbrdYobv3jVxrMA4B3hEdGn",
    //        "expires_in": 3600,
    //        "refresh_token": "1/gLkG1sNlUr3U3T-TVWtX37jOe40f6eQvgoFLG_26mfs",
    //        "scope": "https://www.googleapis.com/auth/photoslibrary https://www.googleapis.com/auth/photoslibrary.sharing",
    //        "token_type": "Bearer"
    //      }

    private SpewParsedResponse authRequest(OutgoingHttpRequest request, Map<String, String> bodyData) {
        request.setHeader(CONTENT_TYPE, ContentTypeUtil.FORM_MIME_TYPE);

        String body = Joiner.on('&').withKeyValueSeparator('=').join(bodyData);
        request.setBody(body.getBytes(StandardCharsets.UTF_8));

        SpewHttpResponse response = request(request);
        return new SpewParsedResponseBuilder(SpewConnectionConfiguration.AUTH, response).withoutVerification().build();
    }

}
