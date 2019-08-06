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
package uk.co.magictractor.spew.util.spi;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import uk.co.magictractor.spew.store.ApplicationPropertyStore;

public class SPIUtilTest {

    @Test
    public void testFirstAvailable_sameInstance() {
        ApplicationPropertyStore a = SPIUtil.firstAvailable(ApplicationPropertyStore.class);
        ApplicationPropertyStore b = SPIUtil.firstAvailable(ApplicationPropertyStore.class);
        Assertions.assertThat(a).isSameAs(b);
    }

}
