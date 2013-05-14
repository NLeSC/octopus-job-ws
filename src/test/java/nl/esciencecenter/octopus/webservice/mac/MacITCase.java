package nl.esciencecenter.octopus.webservice.mac;

/*
 * #%L
 * Octopus Job Webservice
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

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import nl.esciencecenter.octopus.webservice.JobLauncherService;
import nl.esciencecenter.octopus.webservice.mac.MacCredential;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * MAC Access Authentication integration test.
 *
 * Required properties file called "integration.props" in src/test/resources with following keys:
 * <ol>
 * <li>integration.callback.url, url to submit status to, must have MAC Access authentication</li>
 * <li>integration.callback.id, MAC identifier to use for url</li>
 * <li>integration.callback.key, MAC key to use for url</li>
 * </ol>
 *
 * @author stefanv
 *
 */
public class MacITCase {

    /**
     * Submit status to a callback server using MAC Access authentication.
     *
     * @throws URISyntaxException
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void test() throws URISyntaxException, ClientProtocolException, IOException {
        Properties props = new Properties();
        props.load(MacITCase.class.getClassLoader().getResourceAsStream("integration.props"));

        URI url = new URI(props.getProperty("integration.callback.url"));

        // TODO throw better exception than NullPointerException when property can not be found.

        String state = "STOPPED";
        HttpPut request = new HttpPut(url);
        request.setEntity(new StringEntity(state));

        String mac_id = props.getProperty("integration.callback.id");
        String mac_key = props.getProperty("integration.callback.key");
        URI scope = new URI(url.getScheme(), null, url.getHost(), url.getPort(), null, null, null);
        ImmutableList<MacCredential> macs = ImmutableList.of(new MacCredential(mac_id, mac_key, scope));
        HttpClient httpClient = new DefaultHttpClient();
        httpClient = JobLauncherService.macifyHttpClient((AbstractHttpClient) httpClient, macs);

        HttpResponse response = httpClient.execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
    }

}
