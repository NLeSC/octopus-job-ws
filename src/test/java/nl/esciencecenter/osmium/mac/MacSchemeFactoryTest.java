/*
 * #%L
 * Osmium
 * %%
 * Copyright (C) 2013 Nederlands eScience Center
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package nl.esciencecenter.osmium.mac;

import static org.junit.Assert.*;
import nl.esciencecenter.osmium.mac.MacScheme;
import nl.esciencecenter.osmium.mac.MacSchemeFactory;

import org.apache.http.auth.AuthScheme;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

public class MacSchemeFactoryTest {

    @Test
    public void testCreate() {
        MacSchemeFactory factory = new MacSchemeFactory();
        HttpContext context = new BasicHttpContext();
		AuthScheme scheme = factory.create(context);

        assertEquals(new MacScheme(), scheme);
    }

}
