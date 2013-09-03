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
package nl.esciencecenter.octopus.webservice;


import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;
import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.octopus.webservice.JobLauncherConfiguration;
import nl.esciencecenter.octopus.webservice.job.OctopusConfiguration;
import nl.esciencecenter.octopus.webservice.job.PollConfiguration;
import nl.esciencecenter.octopus.webservice.job.SandboxConfiguration;
import nl.esciencecenter.octopus.webservice.job.SchedulerConfiguration;
import nl.esciencecenter.octopus.webservice.mac.MacCredential;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.yammer.dropwizard.client.HttpClientConfiguration;

public class JobLauncherConfigurationTest {
    /**
     *
     * @return Configuration with local job and file adaptor configured.
     * @throws URISyntaxException
     */
    public OctopusConfiguration getSampleOctopusConfiguration() throws URISyntaxException {
        SchedulerConfiguration scheduler = new SchedulerConfiguration("local", null, "multi", null);
        SandboxConfiguration sandbox = new SandboxConfiguration("file", null, "/tmp/sandboxes", null);
        ImmutableMap<String, String> prefs = ImmutableMap.of("octopus.adaptors.local.queue.multi.maxConcurrentJobs", "4");
        PollConfiguration pollConf = new PollConfiguration(10, 50, 100);
        OctopusConfiguration octopusConf = new OctopusConfiguration(scheduler, sandbox, prefs, pollConf);
        return octopusConf;
    }

    @Test
    public void testJobLauncherConfiguration() throws URISyntaxException {
        OctopusConfiguration octopusConf = getSampleOctopusConfiguration();
        ImmutableList<MacCredential> macs = ImmutableList.of(new MacCredential("id", "key", new URI("http://localhost")));
        HttpClientConfiguration httpClient = new HttpClientConfiguration();

        JobLauncherConfiguration conf = new JobLauncherConfiguration(octopusConf, macs, httpClient);

        assertThat(conf.getOctopusConfiguration()).isEqualTo(octopusConf);
        assertThat(conf.getMacs()).isEqualTo(macs);
        assertThat(conf.getHttpClientConfiguration()).isEqualTo(httpClient);
        assertThat(conf.isUseInsecureSSL()).isFalse();
    }

    @Test
    public void testJobLauncherConfiguration_NoArgs() {
        JobLauncherConfiguration conf = new JobLauncherConfiguration();

        assertEquals(new OctopusConfiguration(), conf.getOctopusConfiguration());
        assertNotNull(conf.getHttpClientConfiguration());
        ImmutableList<MacCredential> macs = ImmutableList.of();
        assertEquals(macs, conf.getMacs());
        assertThat(conf.isUseInsecureSSL()).isFalse();
    }

    @Test
    public void testJobLauncherConfiguration_UseInsecureSSL() throws URISyntaxException {
        OctopusConfiguration octopusConf = getSampleOctopusConfiguration();
        ImmutableList<MacCredential> macs = ImmutableList.of(new MacCredential("id", "key", new URI("http://localhost")));
        HttpClientConfiguration httpClient = new HttpClientConfiguration();

        JobLauncherConfiguration conf = new JobLauncherConfiguration(octopusConf, macs, httpClient, true);

        assertThat(conf.isUseInsecureSSL()).isTrue();
    }

    @Test
    public void deserializesFromJSON() throws IOException, URISyntaxException {
        JobLauncherConfiguration conf = fromJson(jsonFixture("fixtures/joblauncher.config.json"), JobLauncherConfiguration.class);

        OctopusConfiguration octopusConf = getSampleOctopusConfiguration();
        octopusConf.setPoll(new PollConfiguration());
        ImmutableList<MacCredential> macs = ImmutableList.of(new MacCredential("id", "key", new URI("http://localhost")));
        ImmutableMap<String, String> emptyProps = ImmutableMap.of();
        octopusConf.setScheduler(new SchedulerConfiguration("local", null, "multi", emptyProps));
        octopusConf.setSandbox(new SandboxConfiguration("file", "/", "/tmp/sandboxes", emptyProps));

        assertThat(conf.getOctopusConfiguration()).isEqualTo(octopusConf);
        assertThat(conf.getMacs()).isEqualTo(macs);
        assertThat(conf.isUseInsecureSSL()).isFalse();
    }

    @Test
    public void testHashCode() throws URISyntaxException {
        OctopusConfiguration octopusConf = getSampleOctopusConfiguration();

        int hashcode = octopusConf.hashCode();

        assertThat(hashcode).isEqualTo(-1086214713);
    }

    @Test
    public void testToString() throws URISyntaxException {
        OctopusConfiguration octopusConf = getSampleOctopusConfiguration();

        String self = octopusConf.toString();

        String expected = "OctopusConfiguration{SchedulerConfiguration{local, multi, null, null}, SandboxConfiguration{file, /tmp/sandboxes, null, null}, {octopus.adaptors.local.queue.multi.maxConcurrentJobs=4}, PollConfiguration{10, 50, 100}}";
        assertThat(self).isEqualTo(expected);

    }
}
