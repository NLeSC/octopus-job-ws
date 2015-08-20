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
package nl.esciencecenter.osmium.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import nl.esciencecenter.osmium.job.SchedulerConfiguration;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class SchedulerConfigurationTest {
    private SchedulerConfiguration scheduler;

    @Before
    public void setUp() {
        ImmutableMap<String, String> props = ImmutableMap.of(
                "xenon.adaptors.ssh.autoAddHostKey", "false");
        scheduler = new SchedulerConfiguration("ssh", "localhost", "multi", props);
    }

    @Test
    public void testHashCode() {
        assertThat(scheduler.hashCode()).isEqualTo(2094263025);
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
                "xenon.adaptors.ssh.autoAddHostKey", "false");
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
                "xenon.adaptors.ssh.autoAddHostKey", "false");
        SchedulerConfiguration expected = new SchedulerConfiguration("ssh", "localhost", "multi", props);
        assertThat(scheduler.equals(expected)).isTrue();
    }

    @Test
    public void testToString() {
        String expected = "SchedulerConfiguration{ssh, multi, localhost, {xenon.adaptors.ssh.autoAddHostKey=false}}";
        assertThat(scheduler.toString()).isEqualTo(expected);
    }

}
