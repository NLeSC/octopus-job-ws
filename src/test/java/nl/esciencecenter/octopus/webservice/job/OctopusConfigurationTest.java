package nl.esciencecenter.octopus.webservice.job;

import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;
import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import nl.esciencecenter.octopus.webservice.job.OctopusConfiguration;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class OctopusConfigurationTest {

    @Test
    public void testGATConfigurationBrokerPrefs() throws URISyntaxException {
        URI scheduler = new URI("local:///");
        String queue = null;
        URI sandbox = new URI("file:///tmp");
        ImmutableMap<String, Object> prefs = ImmutableMap.of(
                "octopus.adaptors.local.queue.multi.maxConcurrentJobs", (Object) 1);

        OctopusConfiguration g = new OctopusConfiguration(scheduler, queue, sandbox, prefs);

        assertThat(g.getScheduler()).isEqualTo(scheduler);
        assertThat(g.getQueue()).isEqualTo(queue);
        assertThat(g.getSandboxRoot()).isEqualTo(sandbox);
        assertThat(g.getPreferences()).isEqualTo(prefs);
    }

    @Test
    public void testGetPreferences() {
        OctopusConfiguration g = new OctopusConfiguration();
        ImmutableMap<String, Object> expected = ImmutableMap.of();
        assertThat(g.getPreferences()).isEqualTo(expected);
    }

    @Test
    public void testSetPreferences() {
        OctopusConfiguration g = new OctopusConfiguration();
        ImmutableMap<String, Object> prefs = ImmutableMap.of("mykey",
                (Object) "myval");
        g.setPreferences(prefs);
        assertThat(g.getPreferences()).isEqualTo(prefs);
    }

    @Test
    public void deserializesFromJSON() throws IOException, URISyntaxException {
        OctopusConfiguration actual = fromJson(jsonFixture("fixtures/octopus.json"),
                OctopusConfiguration.class);

        assertThat(actual.getScheduler()).isEqualTo(new URI("local:///"));
        assertThat(actual.getQueue()).isEqualTo("multi");
        assertThat(actual.getSandboxRoot()).isEqualTo(new URI("file:///tmp/sandboxes"));
        ImmutableMap<String, Object> prefs = ImmutableMap.of(
                "octopus.adaptors.local.queue.multi.maxConcurrentJobs", (Object) 4);
        assertThat(actual.getPreferences()).isEqualTo(prefs);
        PollConfiguration expected_poll = new PollConfiguration(500, 3600000, 43200000);
        assertThat(actual.getPollConfiguration()).isEqualTo(expected_poll);
    }

    @Test
    public void getPreferencesAsProperties() throws URISyntaxException {
        URI scheduler = new URI("local:///");
        String queue = null;
        URI sandbox = new URI("file:///tmp");
        ImmutableMap<String, Object> prefs = ImmutableMap.of(
                "octopus.adaptors.local.queue.multi.maxConcurrentJobs", (Object) 1);
        OctopusConfiguration conf = new OctopusConfiguration(scheduler, queue, sandbox, prefs);

        Properties props = conf.getPreferencesAsProperties();
        Properties expected_props = new Properties();
        expected_props.put("octopus.adaptors.local.queue.multi.maxConcurrentJobs", "1");
        assertThat(props).isEqualTo(expected_props);
    }
}
