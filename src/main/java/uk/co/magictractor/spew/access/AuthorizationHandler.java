/**
 * Copyright 2019 Ken Dobson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.magictractor.spew.access;

import uk.co.magictractor.spew.api.SpewApplication;
import uk.co.magictractor.spew.api.VerificationFunction;

/**
 * <p>
 * Interface for capturing verification codes when a user permits the
 * application to call the service provider's API.
 * </p>
 * <p>
 * Two implementations are provided, but others may be added. The
 * implementations provided can capture values pasted into the console or fire
 * up a lightweight web server to capture verification codes.
 * </p>
 */
public interface AuthorizationHandler {

    VerificationFunction verificationFunction();

    void preOpenAuthorizationInBrowser(SpewApplication application);

    void postOpenAuthorizationInBrowser(SpewApplication application);

    /**
     * <p>
     * The oauth_callback value to be used in the request. May be a URL for a
     * callback or a special value indicating that a code is to be displayed and
     * passed back to the application in another manner such as pasting it into
     * the console.
     * </p>
     * <p>
     * This method is called after preOpenAuthorizationInBrowser() which might
     * fire up a local server allowing the URL for the callback value to be
     * requested from the server.
     * </p>
     */
    String getCallbackValue();

}