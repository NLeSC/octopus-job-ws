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
package nl.esciencecenter.osmium;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.setup.Environment;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nl.esciencecenter.osmium.health.JobLauncherHealthCheck;
import nl.esciencecenter.osmium.job.PollConfiguration;
import nl.esciencecenter.osmium.job.SandboxConfiguration;
import nl.esciencecenter.osmium.job.SchedulerConfiguration;
import nl.esciencecenter.osmium.job.XenonConfiguration;
import nl.esciencecenter.osmium.job.XenonManager;
import nl.esciencecenter.osmium.mac.MacCredential;
import nl.esciencecenter.osmium.mac.MacScheme;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class JobLauncherServiceTest {
    private final Environment environment = mock(Environment.class);
    private final JobLauncherService service = new JobLauncherService();

    @Test
    public void testGetName() {
        assertEquals("osmium", service.getName());
    }

    @Test
    public void testRun() throws Exception {
        // FIXME Waiting for https://github.com/NLeSC/xenon/issues/38 to be resolved
        JobLauncherConfiguration config = sampleConfiguration();

        service.run(config, environment);

        verify(environment, times(2)).jersey().register(any(Object.class));
        verify(environment).healthChecks().register("osmium", any(JobLauncherHealthCheck.class));
        verify(environment).lifecycle().manage(any(XenonManager.class));

        // TODO test injection of MAC Credentials into httpClient
        // or fold injection into extented HttpClientBuilder
    }

    private JobLauncherConfiguration sampleConfiguration()
            throws URISyntaxException {
        ImmutableMap<String, String> prefs = ImmutableMap.of(
                "xenon.adaptors.local.queue.multi.maxConcurrentJobs", "1");
        SchedulerConfiguration scheduler = new SchedulerConfiguration("local", null, "multi", null);
        SandboxConfiguration sandbox = new SandboxConfiguration("file", "/", "/tmp/sandboxes", null);
        PollConfiguration pollConf = new PollConfiguration();
        XenonConfiguration xenon = new XenonConfiguration(scheduler, sandbox, "default", prefs, pollConf);
        ImmutableList<MacCredential> macs = ImmutableList.of(new MacCredential(
                "id", "key", new URI("http://localhost")));
        HttpClientConfiguration httpClient = new HttpClientConfiguration();

        JobLauncherConfiguration config = new JobLauncherConfiguration(xenon,
                macs, httpClient);
        return config;
    }

    @Test
    public void testMacifyHttpClient() throws URISyntaxException {
        // FIXME Waiting for https://github.com/NLeSC/xenon/issues/38 to be resolved
        JobLauncherConfiguration config = sampleConfiguration();
        DefaultHttpClient httpClient = new DefaultHttpClient();

        JobLauncherService.macifyHttpClient(httpClient);

//        List<String> schemes = httpClient.getAuthSchemes()
//                .getSchemeNames();
        List<String> schemes = (List<String>) httpClient.getParams().getParameter(AuthPNames.TARGET_AUTH_PREF);
        assertTrue("MAC Registered auth scheme", schemes.contains("mac"));

        MacCredential expected_creds = config.getMacs().get(0);
        AuthScope authscope = expected_creds.getAuthScope();
        Credentials creds = httpClient.getCredentialsProvider().getCredentials(
                authscope);
        assertEquals(expected_creds, creds);

        List<String> authSchemes = Collections
                .unmodifiableList(Arrays.asList(new String[] {
                        MacScheme.SCHEME_NAME, AuthPolicy.SPNEGO,
                        AuthPolicy.KERBEROS, AuthPolicy.NTLM,
                        AuthPolicy.DIGEST, AuthPolicy.BASIC }));
        assertEquals(authSchemes,
                httpClient.getParams()
                        .getParameter(AuthPNames.TARGET_AUTH_PREF));
    }
}
