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
package uk.co.magictractor.spew.provider.dropbox;

import java.util.Collections;
import java.util.List;

import uk.co.magictractor.spew.api.SpewOAuth2ServiceProvider;
import uk.co.magictractor.spew.core.typeadapter.SpewTypeAdapter;

// https://www.dropbox.com/developers/apps
public class Dropbox implements SpewOAuth2ServiceProvider {

    private Dropbox() {
    }

    @Override
    public String oauth2AuthorizationUri() {
        return "https://www.dropbox.com/oauth2/authorize";
    }

    @Override
    public String oauth2TokenUri() {
        return "https://api.dropboxapi.com/oauth2/token";
    }

    @Override
    public List<SpewTypeAdapter<?>> getTypeAdapters() {
        return Collections.emptyList();
    }

}
