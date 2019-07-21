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
package uk.co.magictractor.spew.api.boa;

import uk.co.magictractor.spew.api.OAuth1Application;
import uk.co.magictractor.spew.api.OAuth2Application;
import uk.co.magictractor.spew.api.SpewApplication;
import uk.co.magictractor.spew.api.SpewConnection;
import uk.co.magictractor.spew.api.connection.SpewConnectionInit;

/**
 *
 */
public class BoaConnectionInit implements SpewConnectionInit {

    @Override
    public SpewConnection createConnection(SpewApplication application) {
        if (application instanceof OAuth1Application) {
            return new BoaOAuth1Connection((OAuth1Application) application);
        }
        else if (application instanceof OAuth2Application) {
            return new BoaOAuth2Connection((OAuth2Application) application);
        }
        else {
            return null;
        }
    }

}