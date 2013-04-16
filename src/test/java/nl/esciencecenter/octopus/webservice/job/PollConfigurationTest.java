package nl.esciencecenter.octopus.webservice.job;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

public class PollConfigurationTest {

    @Test
    public void testPollConfigurationIntInt() {
        PollConfiguration conf = new PollConfiguration(123, 456);
        assertThat(conf.getInterval()).isEqualTo(123);
        assertThat(conf.getTimeout()).isEqualTo(456);
    }

    @Test
    public void testPollConfiguration() {
        PollConfiguration conf = new PollConfiguration();
        assertThat(conf.getInterval()).isEqualTo(500);
        assertThat(conf.getTimeout()).isEqualTo(3600000);
    }

    @Test
    public void testSetInterval() {
        PollConfiguration conf = new PollConfiguration();

        conf.setInterval(123);

        assertThat(conf.getInterval()).isEqualTo(123);
    }

    @Test
    public void testSetTimeout() {
        PollConfiguration conf = new PollConfiguration();

        conf.setTimeout(456);

        assertThat(conf.getTimeout()).isEqualTo(456);
    }

    @Test
    public void testToString() {
        PollConfiguration conf = new PollConfiguration();

        String result = conf.toString();

        String expected = "PollConfiguration{500, 3600000}";
        assertThat(result).isEqualTo(expected);
    }
}
