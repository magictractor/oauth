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

import java.util.Arrays;
import java.util.Collection;

// TODO! check whether this is a subset of UrlConnection, and perhaps bin it (plus dependency)
public class TestJavaxActivationContentTypeFromResourceName extends AbstractTestContentTypeFromResourceName {

    private static final Collection<String> UNSUPPORTED = Arrays.asList(
        "css", "js", "mp3", "doc", "pdf", "xml", "ico", "ttf", "zip", "7z", "oga", "fits");

    private static JavaxActivationContentTypeFromResourceName TESTEE = new JavaxActivationContentTypeFromResourceName();

    @Override
    protected String determineContentType(String resourceName) {
        return TESTEE.determineContentType(resourceName);
    }

    @Override
    protected boolean isSupportedExtension(String extension) {
        return !UNSUPPORTED.contains(extension);
    }

}
