/**
 * Copyright 2015-2019 Ken Dobson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *g
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.magictractor.spew.core.response.parser.jayway;

import uk.co.magictractor.spew.api.SpewConnectionConfiguration;
import uk.co.magictractor.spew.api.SpewHttpMessage;
import uk.co.magictractor.spew.core.response.parser.SpewHttpMessageBodyReader;
import uk.co.magictractor.spew.core.response.parser.SpewHttpMessageBodyReaderFactory;
import uk.co.magictractor.spew.util.ContentTypeUtil;

/**
 *
 */
public class JaywayResponseFactory implements SpewHttpMessageBodyReaderFactory {

    @Override
    public SpewHttpMessageBodyReader instanceFor(SpewConnectionConfiguration connectionConfiguration,
            SpewHttpMessage httpMessage) {
        if (ContentTypeUtil.isJson(ContentTypeUtil.fromHeader(httpMessage))) {
            return new JaywayHttpMessageBodyReader(connectionConfiguration, httpMessage);
        }
        return null;
    }

}
