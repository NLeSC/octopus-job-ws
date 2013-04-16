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
import nl.esciencecenter.octopus.webservice.mac.MacCredential;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.yammer.dropwizard.client.HttpClientConfiguration;

public class JobLauncherConfigurationTest {
    /**
     * @return
     * @throws URISyntaxException
     */
    public OctopusConfiguration getSampleOctopusConfiguration() throws URISyntaxException {
        URI scheduler = new URI("local:///");
        String queue = "multi";
        URI sandboxRoot = new URI("file:///tmp/sandboxes");
        ImmutableMap<String, Object> prefs = ImmutableMap.of("octopus.adaptors.local.queue.multi.maxConcurrentJobs", (Object) 4);
        OctopusConfiguration octopusConf = new OctopusConfiguration(scheduler, queue, sandboxRoot, prefs);
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
    }

    @Test
    public void testJobLauncherConfigurationNoArgs() {
        JobLauncherConfiguration conf = new JobLauncherConfiguration();

        assertEquals(new OctopusConfiguration(), conf.getOctopusConfiguration());
        assertNotNull(conf.getHttpClientConfiguration());
        ImmutableList<MacCredential> macs = ImmutableList.of();
        assertEquals(macs, conf.getMacs());
    }

    @Test
    public void deserializesFromJSON() throws IOException, URISyntaxException {
        JobLauncherConfiguration conf = fromJson(jsonFixture("fixtures/joblauncher.config.json"), JobLauncherConfiguration.class);

        OctopusConfiguration octopusConf = getSampleOctopusConfiguration();
        ImmutableList<MacCredential> macs = ImmutableList.of(new MacCredential("id", "key", new URI("http://localhost")));

        assertThat(conf.getOctopusConfiguration()).isEqualTo(octopusConf);
        assertThat(conf.getMacs()).isEqualTo(macs);
    }

}
