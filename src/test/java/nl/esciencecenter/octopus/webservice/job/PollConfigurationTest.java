package nl.esciencecenter.octopus.webservice.job;

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

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

public class PollConfigurationTest {

    @Test
    public void testPollConfiguration_AllParameters() {
        PollConfiguration conf = new PollConfiguration(123, 456, 789);
        assertThat(conf.getInterval()).isEqualTo(123);
        assertThat(conf.getCancelTimeout()).isEqualTo(456);
        assertThat(conf.getDeleteTimeout()).isEqualTo(789);
    }

    @Test
    public void testPollConfiguration_NoParameters() {
        PollConfiguration conf = new PollConfiguration();
        assertThat(conf.getInterval()).isEqualTo(30000);
        assertThat(conf.getCancelTimeout()).isEqualTo(3600000);
        assertThat(conf.getDeleteTimeout()).isEqualTo(12*60*60*1000);
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

        conf.setCancelTimeout(456);

        assertThat(conf.getCancelTimeout()).isEqualTo(456);
    }

    @Test
    public void testToString() {
        PollConfiguration conf = new PollConfiguration();

        String result = conf.toString();

        String expected = "PollConfiguration{30000, 3600000, 43200000}";
        assertThat(result).isEqualTo(expected);
    }
}
