package nl.esciencecenter.octopus.webservice.job;

import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;
import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;

import nl.esciencecenter.octopus.webservice.job.OctopusConfiguration;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class OctopusConfigurationTest {

    @Test
    public void testGATConfigurationBrokerPrefs() {
        ImmutableMap<String, Object> prefs = ImmutableMap.of(
                "localq.max.concurrent.jobs", (Object) 1);

        OctopusConfiguration g = new OctopusConfiguration("localq://localhost", prefs);

        assertThat(g.getBrokerURI()).isEqualTo("localq://localhost");
        assertThat(g.getPreferences()).isEqualTo(prefs);
    }

    @Test
    public void testSetGetBrokerURI() {
        OctopusConfiguration g = new OctopusConfiguration();
        g.setBrokerURI("localq://localhost");
        assertThat(g.getBrokerURI()).isEqualTo("localq://localhost");
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
    public void deserializesFromJSON() throws IOException {
        OctopusConfiguration actual = fromJson(jsonFixture("fixtures/octopus.json"),
                OctopusConfiguration.class);

        assertThat(actual.getBrokerURI()).isEqualTo("localq://localhost");
        ImmutableMap<String, Object> prefs = ImmutableMap.of(
                "localq.max.concurrent.jobs", (Object) 1);
        assertThat(actual.getPreferences()).isEqualTo(prefs);
    }

    @Test
    public void hasAWorkingEqualsMethod() throws Exception {
        ImmutableMap<String, Object> prefs = ImmutableMap.of(
                "localq.max.concurrent.jobs", (Object) 1);

        OctopusConfiguration g = new OctopusConfiguration("localq://localhost", prefs);

        assertThat(g.equals(g)).isTrue();

        assertThat(g.equals(new OctopusConfiguration("localq://localhost", prefs)))
                .isTrue();

        assertThat(g.equals(null)).isFalse();

        assertThat(g.equals("string")).isFalse();

        assertThat(g.equals(new OctopusConfiguration("ssh://example.com", prefs)))
                .isFalse();

        ImmutableMap<String, Object> prefs2 = ImmutableMap.of(
                "localq.max.concurrent.jobs", (Object) 4);
        assertThat(g.equals(new OctopusConfiguration("localq://localhost", prefs2)))
                .isFalse();
    }
}
