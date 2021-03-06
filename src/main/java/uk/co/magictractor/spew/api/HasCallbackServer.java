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
package uk.co.magictractor.spew.api;

import java.util.Arrays;
import java.util.List;

import uk.co.magictractor.spew.server.ApplicationValuesRequestHandler;
import uk.co.magictractor.spew.server.AuthVerificationRequestHandler;
import uk.co.magictractor.spew.server.CallbackServer;
import uk.co.magictractor.spew.server.ConnectionValuesRequestHandler;
import uk.co.magictractor.spew.server.GlobalValuesRequestHandler;
import uk.co.magictractor.spew.server.RequestHandler;
import uk.co.magictractor.spew.server.ResourceRequestHandler;
import uk.co.magictractor.spew.server.ShutdownRequestHandler;
import uk.co.magictractor.spew.server.TemplateRequestHandler;

/**
 * Additional interface implemented by Applications which may use a callback
 * server to capture authorizations from service providers.
 */
public interface HasCallbackServer {

    /**
     * Request handlers which determine how callbacks from the server provider
     * are handled, plus perhaps static pages for redirecting to success or
     * failure messages after authorization plus supporting CSS files etc.
     */
    default List<RequestHandler> getServerRequestHandlers() {
        return Arrays.asList(
            new GlobalValuesRequestHandler(),
            new ApplicationValuesRequestHandler(),
            new ConnectionValuesRequestHandler(),
            new AuthVerificationRequestHandler(),
            new ShutdownRequestHandler("/js/shutdownNow.js"),
            new TemplateRequestHandler(serverResourcesRelativeToClass()),
            new ResourceRequestHandler(serverResourcesRelativeToClass()));
    }

    /**
     * <p>
     * The class relative to which resources for static web pages and templates
     * are found.
     * </p>
     * <p>
     * Applications may choose to override this and copy and modify the
     * resources in the new location. They may preserve their names to work with
     * the existing list of RequestHandlers, or getServerRequestHandlers() may
     * be overridden too.
     * </p>
     */
    default Class<?> serverResourcesRelativeToClass() {
        return CallbackServer.class;
    }

    default String protocol() {
        return "http";
    }

    default String host() {
        // TODO! default to this or localhost??
        return "127.0.0.1";
    }

    default int port() {
        return 8080;
    }

    default String uri() {
        return protocol() + "://" + host() + ":" + port();
    }

}
