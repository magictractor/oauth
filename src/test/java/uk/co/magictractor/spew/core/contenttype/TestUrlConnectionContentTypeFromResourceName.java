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
package uk.co.magictractor.spew.core.contenttype;

import org.junit.jupiter.api.BeforeAll;

public class TestUrlConnectionContentTypeFromResourceName extends AbstractTestContentTypeFromResourceName {

    private static URLConnectionContentTypeFromResourceName TESTEE = new URLConnectionContentTypeFromResourceName();

    @BeforeAll
    public static void setUpUnsupported() {
        unsupported("css", "js", "doc", "ico", "ttf", "7z", "fits", "octet-stream");
        unsupportedRaw();
    }

    @Override
    protected String determineContentType(String resourceName) {
        return TESTEE.determineContentType(resourceName);
    }

    @Override
    protected String unknownContentType() {
        return null;
    }

}
