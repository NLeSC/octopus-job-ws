package nl.esciencecenter.octopus.webservice.job;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class SchedulerConfigurationTest {
    private SchedulerConfiguration scheduler;

    @Before
    public void setUp() {
        ImmutableMap<String, String> props = ImmutableMap.of(
                "octopus.adaptors.ssh.autoAddHostKey", "false");
        scheduler = new SchedulerConfiguration("ssh", "localhost", "multi", props);
    }

    @Test
    public void testHashCode() {
        assertThat(scheduler.hashCode()).isEqualTo(243831506);
    }

    @Test
    public void testGetScheme() {
        assertThat(scheduler.getScheme()).isEqualTo("ssh");
    }

    @Test
    public void testGetLocation() {
        assertThat(scheduler.getLocation()).isEqualTo("localhost");
    }

    @Test
    public void testGetProperties() {
        Map<String, String> expected = ImmutableMap.of(
                "octopus.adaptors.ssh.autoAddHostKey", "false");
        assertThat(scheduler.getProperties()).isEqualTo(expected);

    }

    @Test
    public void testGetQueue() {
        assertThat(scheduler.getQueue()).isEqualTo("multi");
    }

    @Test
    public void testEqualsObject() {
        assertThat(scheduler.equals(scheduler)).isTrue();
        assertThat(scheduler.equals(null)).isFalse();
        assertThat(scheduler.equals(5)).isFalse();
        ImmutableMap<String, String> props = ImmutableMap.of(
                "octopus.adaptors.ssh.autoAddHostKey", "false");
        SchedulerConfiguration expected = new SchedulerConfiguration("ssh", "localhost", "multi", props);
        assertThat(scheduler.equals(expected)).isTrue();
    }

    @Test
    public void testToString() {
        String expected = "SchedulerConfiguration{ssh, multi, localhost, {octopus.adaptors.ssh.autoAddHostKey=false}}";
        assertThat(scheduler.toString()).isEqualTo(expected);
    }

}
