/**
 * Copyright 2015-2019 Ken Dobson
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

import static uk.co.magictractor.spew.api.HttpHeaderNames.CACHE_CONTROL;
import static uk.co.magictractor.spew.api.HttpHeaderNames.CONTENT_TYPE;
import static uk.co.magictractor.spew.api.HttpHeaderNames.LAST_MODIFIED;

import java.nio.file.Path;
import java.time.Instant;

import uk.co.magictractor.spew.util.ContentTypeUtil;
import uk.co.magictractor.spew.util.HttpMessageUtil;
import uk.co.magictractor.spew.util.PathUtil;

/**
 *
 */
// TODO! last modified time - available via Files
public class OutgoingStaticResponse extends OutgoingResponse {

    private final Path bodyPath;
    private String contentType;

    public OutgoingStaticResponse(Path bodyPath) {
        super(bodyPath);
        if (bodyPath == null) {
            throw new IllegalArgumentException("bodyPath must not be null");
        }
        this.bodyPath = bodyPath;
        Instant lastModified = PathUtil.getLastModified(bodyPath);
        // max-age is in seconds, see https://www.keycdn.com/blog/http-cache-headers
        setHeader(CACHE_CONTROL, "public,max-age=3600");
        //addHeader("Cache-Control", "public, must-revalidate, max-age=10000");
        //addHeader("Cache-Control", "public, max-age=0");
        setHeader(LAST_MODIFIED, lastModified);
        // addHeader("Expires", Instant.ofEpochMilli(10000 + System.currentTimeMillis()));

        setHeader(CONTENT_TYPE, ContentTypeUtil.fromResourceName(bodyPath.getFileName().toString()));
    }

    // bin??
    //    public String getContentType() {
    //        if (contentType == null) {
    //            contentType = determineContentType();
    //        }
    //        return contentType;
    //    }

    // TODO! split out code for determining content type from file name (perhaps with SPI)
    //    private String determineContentType() {
    //        return ContentTypeUtil.fromResourceName(resourceName);
    //    }

    @Override
    public String toString() {
        return HttpMessageUtil.toStringHelper(this)
                .add("bodyPath", bodyPath)
                .toString();
    }

}
