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
package uk.co.magictractor.spew.server;

import static uk.co.magictractor.spew.api.HttpHeaderNames.SET_COOKIE;

import uk.co.magictractor.spew.util.RandomUtil;

// TODO! incomplete and unused
// If no session found this could: set a cookie AND a query string parameter with the session id then redirect to the same page.
// When session is found, if the session id is in the query string but not in a cookie, then cookies are disabled in the browser.
// If the session id is in the query string AND the cookie, then could redirect a second time, this time without the query param.
// Note: could redirects cause problems for OAuth callbacks?
public class SessionRequestHandler implements RequestHandler {

    @Override
    public void handleRequest(SpewHttpRequest request, OutgoingResponseBuilder responseBuilder) {
        String cookies = request.getHeaderValue("Cookie");
        System.err.println("cookies: " + cookies + " (" + request.getPath() + ")");

        if (cookies == null) {
            String sessionId = System.currentTimeMillis() + ":" + RandomUtil.nextBigPositiveLong();
            responseBuilder.withHeader(SET_COOKIE, "JSESSIONID=" + sessionId);
        }

    }

}
