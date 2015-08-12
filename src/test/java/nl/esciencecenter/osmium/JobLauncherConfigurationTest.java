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


import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.osmium.job.LauncherConfiguration;
import nl.esciencecenter.osmium.job.PollConfiguration;
import nl.esciencecenter.osmium.job.SandboxConfiguration;
import nl.esciencecenter.osmium.job.SchedulerConfiguration;
import nl.esciencecenter.osmium.job.XenonConfiguration;
import nl.esciencecenter.osmium.mac.MacCredential;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.jackson.Jackson;

public class JobLauncherConfigurationTest {
	private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    /**
     *
     * @return Configuration with local job and file adaptor configured.
     * @throws URISyntaxException
     */
    public XenonConfiguration getSampleXenonConfiguration() throws URISyntaxException {
        SchedulerConfiguration scheduler = new SchedulerConfiguration("local", null, "multi", null);
        SandboxConfiguration sandbox = new SandboxConfiguration("file", null, "/tmp/sandboxes", null);
        ImmutableMap<String, String> prefs = ImmutableMap.of("xenon.adaptors.local.queue.multi.maxConcurrentJobs", "4");
        PollConfiguration pollConf = new PollConfiguration(10, 50, 100);
        XenonConfiguration xenonConf = new XenonConfiguration(scheduler, sandbox, "local", prefs, pollConf);
        return xenonConf;
    }

    @Test
    public void testJobLauncherConfiguration() throws URISyntaxException {
        XenonConfiguration xenonConf = getSampleXenonConfiguration();
        ImmutableList<MacCredential> macs = ImmutableList.of(new MacCredential("id", "key", new URI("http://localhost")));
        HttpClientConfiguration httpClient = new HttpClientConfiguration();

        JobLauncherConfiguration conf = new JobLauncherConfiguration(xenonConf, macs, httpClient);

        assertThat(conf.getXenonConfiguration()).isEqualTo(xenonConf);
        assertThat(conf.getMacs()).isEqualTo(macs);
        assertThat(conf.getHttpClientConfiguration()).isEqualTo(httpClient);
        assertThat(conf.isUseInsecureSSL()).isFalse();
    }

    @Test
    public void testJobLauncherConfiguration_NoArgs() {
        JobLauncherConfiguration conf = new JobLauncherConfiguration();

        assertEquals(new XenonConfiguration(), conf.getXenonConfiguration());
        assertNotNull(conf.getHttpClientConfiguration());
        ImmutableList<MacCredential> macs = ImmutableList.of();
        assertEquals(macs, conf.getMacs());
        assertThat(conf.isUseInsecureSSL()).isFalse();
    }

    @Test
    public void testJobLauncherConfiguration_UseInsecureSSL() throws URISyntaxException {
        XenonConfiguration xenonConf = getSampleXenonConfiguration();
        ImmutableList<MacCredential> macs = ImmutableList.of(new MacCredential("id", "key", new URI("http://localhost")));
        HttpClientConfiguration httpClient = new HttpClientConfiguration();

        JobLauncherConfiguration conf = new JobLauncherConfiguration(xenonConf, macs, httpClient, true);

        assertThat(conf.isUseInsecureSSL()).isTrue();
    }

    @Test
    public void deserializesFromJSON() throws IOException, URISyntaxException {
        JobLauncherConfiguration conf = MAPPER.readValue(fixture("fixtures/joblauncher.config.json"), JobLauncherConfiguration.class);

        XenonConfiguration xenonConf = getSampleXenonConfiguration();
        xenonConf.setPoll(new PollConfiguration());
        ImmutableList<MacCredential> macs = ImmutableList.of(new MacCredential("id", "key", new URI("http://localhost")));
        ImmutableMap<String, String> emptyProps = ImmutableMap.of();
        xenonConf.setLaunchers(ImmutableMap.of("local",
                new LauncherConfiguration(
                        new SchedulerConfiguration("local", null, "multi", emptyProps),
                        new SandboxConfiguration("file", "/", "/tmp/sandboxes", emptyProps)
                )));

        assertThat(conf.getXenonConfiguration()).isEqualTo(xenonConf);
        assertThat(conf.getMacs()).isEqualTo(macs);
        assertThat(conf.isUseInsecureSSL()).isFalse();
    }

    @Test
    public void testHashCode() throws URISyntaxException {
        XenonConfiguration xenonConf = getSampleXenonConfiguration();

        int hashcode = xenonConf.hashCode();

        assertThat(hashcode).isEqualTo(625144505);
    }

    @Test
    public void testToString() throws URISyntaxException {
        XenonConfiguration xenonConf = getSampleXenonConfiguration();

        String self = xenonConf.toString();

        String expected = "XenonConfiguration{{local=LauncherConfiguration{SchedulerConfiguration{local, multi, null, null}, SandboxConfiguration{file, /tmp/sandboxes, null, null}}}, local, {xenon.adaptors.local.queue.multi.maxConcurrentJobs=4}, PollConfiguration{10, 50, 100}}";
        assertThat(self).isEqualTo(expected);

    }
}
