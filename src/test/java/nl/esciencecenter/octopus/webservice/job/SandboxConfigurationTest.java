package nl.esciencecenter.octopus.webservice.job;

import static org.fest.assertions.api.Assertions.assertThat;
import nl.esciencecenter.octopus.files.Pathname;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class SandboxConfigurationTest {
    SandboxConfiguration sandbox;

    @Before
    public void setUp() {
        ImmutableMap<String, String> props = ImmutableMap.of(
                "octopus.adaptors.ssh.autoAddHostKey", "false");
        sandbox = new SandboxConfiguration("ssh", "localhost",  "/tmp/sandboxes", props);
    }

    @Test
    public void testGetScheme() {
        assertThat(sandbox.getScheme()).isEqualTo("ssh");
    }

    @Test
    public void testGetLocation() {
        assertThat(sandbox.getLocation()).isEqualTo("localhost");
    }

    @Test
    public void testGetProperties() {
        ImmutableMap<String, String> expected = ImmutableMap.of(
                "octopus.adaptors.ssh.autoAddHostKey", "false");
        assertThat(sandbox.getProperties()).isEqualTo(expected);
    }

    @Test
    public void testGetPath() {
        Pathname expected = new Pathname("/tmp/sandboxes");
        assertThat(sandbox.getPath()).isEqualTo(expected);
    }

    @Test
    public void testEquals_SameObj_Equals() {
        assertThat(sandbox.equals(sandbox)).isTrue();
    }

    @Test
    public void testEquals_OtherObj_Equals() {
        ImmutableMap<String, String> props = ImmutableMap.of(
                "octopus.adaptors.ssh.autoAddHostKey", "false");
        SandboxConfiguration expected = new SandboxConfiguration("ssh", "localhost",  "/tmp/sandboxes", props);

        assertThat(sandbox.equals(expected)).isTrue();
    }

    @Test
    public void testEquals_Null_NotEquals() {
        assertThat(sandbox.equals(null)).isFalse();
    }
}
