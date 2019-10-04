package uk.co.magictractor.spew.api;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import uk.co.magictractor.spew.core.response.parser.SpewParsedResponseBuilder;
import uk.co.magictractor.spew.core.typeadapter.SpewTypeAdapter;
import uk.co.magictractor.spew.core.verification.AuthorizationHandler;
import uk.co.magictractor.spew.core.verification.VerificationFunction;
import uk.co.magictractor.spew.server.LocalServerAuthorizationHandler;

// Base interface for OAuth1 and OAuth2. Some methods likely to move here from OAuth1 when (and if) OAuth2 support is added.
// https://stackoverflow.com/questions/4113934/how-is-oauth-2-different-from-oauth-1
// imgur and google photos uses OAuth2 https://api.imgur.com/#oauthendpoints
// imgur responses also give information about how many calls by follow in the same hour
// google photos uses "next page" tokens
// interesting! https://hueniverse.com/oauth-2-0-and-the-road-to-hell-8eec45921529
//
// https://en.wikipedia.org/wiki/List_of_OAuth_providers
public interface SpewServiceProvider {

    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * <p>
     * May be overridden to ensure that every request has standard parameters.
     * This can be used, for example, to set a parameter which specifies that
     * responses should be in JSON format.
     * </p>
     * <p>
     * Default implementation does nothing.
     * </p>
     */
    default void prepareRequest(ApplicationRequest request) {
        // Do nothing
    }

    /**
     * <p>
     * May be overridden for service provides which misreport content types in
     * the HTTP header.
     * </p>
     */
    //default String getContentType(SpewHttpResponse response) {
    //    return ContentTypeUtil.fromHeader(response);
    //}

    default List<SpewTypeAdapter<?>> getTypeAdapters() {
        return Collections.emptyList();
    }

    /**
     * Check that the response has an "ok" status (if it contains a status) and
     * no error message (if it can contain an error message). TODO! what should
     * this throw?
     *
     * @throws BadResponseException if the response contains an error message or
     *         has a status other than "ok".
     */
    // TODO! force all service providers to implement this
    // void verifyResponse(SpewResponse response);

    // TODO! after response parsers are added ensure verifyResponse is still called
    //default void verifyResponse(SpewHttpMessageBodyReader response) {
    //}

    // TODO! no default, every service provider should have a non-empty impl of this method
    default void buildParsedResponse(SpewParsedResponseBuilder parsedResponseBuilder) {
    }

    default AuthorizationHandler getDefaultAuthorizationHandler(
            Supplier<VerificationFunction> verificationFunctionSupplier) {
        return new LocalServerAuthorizationHandler(verificationFunctionSupplier);
        //return new PasteVerificationCodeHandler(verificationFunctionSupplier);
    }

    /**
     * The out-of-band URI (sometimes referred to as "oob") is a special value
     * used in the redirect_url to tell the server to display a verification
     * code rather than perform a callback.
     */
    String getOutOfBandUri();

    default String appManagementUrl() {
        throw new UnsupportedOperationException("Not recorded for this site");
    }

}
