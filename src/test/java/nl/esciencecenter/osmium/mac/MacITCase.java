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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.osmium.JobLauncherService;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MAC Access Authentication integration test.
 *
 * Starts a http server and performs requests against it.
 *
 *
 * @author Stefan Verhoeven <s.verhoeven@esciencecenter.nl>
 *
 */
public class MacITCase {
    protected static final Logger LOGGER = LoggerFactory.getLogger(MacITCase.class);

    private HttpServer server = null;

    @Before
    public void setUp() throws Exception {
        server = ServerBootstrap.bootstrap().registerHandler("/status", new AuthHandler()).create();
        server.start();
        LOGGER.info("Started webserver on {}", getServerURI());
    }

    public URI getServerURI() throws URISyntaxException {
        InetAddress address = server.getInetAddress();
        return new URI("http", null, address.getHostName(), server.getLocalPort(), "/status", null, null);
    }

    // do lots of testing!

    @After
    public void tearDown() throws Exception {
        LOGGER.info("Stopping webserver on {}", getServerURI());
        server.stop();
    }

    /**
     * Stub which returns AUTHORIZATION header of request as response body when AUTHORIZATION header is in request. Otherwise
     * returns UNAUTHORIZED http status code and MAC as the available authentication schemes.
     *
     * @author verhoes
     *
     */
    static class AuthHandler implements HttpRequestHandler {
        public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
            Header auth = request.getFirstHeader(HttpHeaders.AUTHORIZATION);
            if (auth == null) {
                response.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
                response.addHeader("WWW-Authenticate", "MAC");
            } else {
                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(new StringEntity(auth.getValue(), ContentType.TEXT_PLAIN));
            }
        }

    }

    /**
     * Submit status to a callback server using MAC Access authentication.
     *
     * @throws URISyntaxException
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void test() throws URISyntaxException, ClientProtocolException, IOException {
        URI url = getServerURI();

        // TODO throw better exception than NullPointerException when property can not be found.

        String state = "STOPPED";
        HttpPut request = new HttpPut(url);
        request.setEntity(new StringEntity(state));

        String mac_id =
                "eyJzYWx0IjogIjU3MjY0NCIsICJleHBpcmVzIjogMTM4Njc3MjEwOC4yOTIyNTUsICJ1c2VyaWQiOiAiam9ibWFuYWdlciJ9KBJRMeTW2G9I6jlYwRj6j8koAek=";
        String mac_key = "_B1YfcqEYpZxyTx_-411-QdBOSI=";
        URI scope = new URI(url.getScheme(), null, url.getHost(), url.getPort(), null, null, null);
        MacCredential mac = new MacCredential(mac_id, mac_key, scope);
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(mac.getAuthScope(), mac);

        HttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();
        httpClient = JobLauncherService.macifyHttpClient(httpClient);

        HttpResponse response = httpClient.execute(request);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        assertThat(EntityUtils.toString(response.getEntity())).startsWith("MAC id=\"" + mac_id);
        // asserting rest of request.header[AUTHORIZATION] can not be done due to embedded timestamp and changing hostname/port.
    }
}
