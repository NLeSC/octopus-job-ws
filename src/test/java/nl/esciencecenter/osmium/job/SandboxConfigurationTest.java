/*
 * #%L
 * Xenon Job Webservice
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

import static org.fest.assertions.api.Assertions.assertThat;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.osmium.job.SandboxConfiguration;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class SandboxConfigurationTest {
    SandboxConfiguration sandbox;

    @Before
    public void setUp() {
        ImmutableMap<String, String> props = ImmutableMap.of(
                "xenon.adaptors.ssh.autoAddHostKey", "false");
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
                "xenon.adaptors.ssh.autoAddHostKey", "false");
        assertThat(sandbox.getProperties()).isEqualTo(expected);
    }

    @Test
    public void testGetPath() {
        RelativePath expected = new RelativePath("/tmp/sandboxes");
        assertThat(sandbox.getPath()).isEqualTo(expected);
    }

    @Test
    public void testEquals_SameObj_Equals() {
        assertThat(sandbox.equals(sandbox)).isTrue();
    }

    @Test
    public void testEquals_OtherObj_Equals() {
        ImmutableMap<String, String> props = ImmutableMap.of(
                "xenon.adaptors.ssh.autoAddHostKey", "false");
        SandboxConfiguration expected = new SandboxConfiguration("ssh", "localhost",  "/tmp/sandboxes", props);

        assertThat(sandbox.equals(expected)).isTrue();
    }

    @Test
    public void testEquals_Null_NotEquals() {
        assertThat(sandbox.equals(null)).isFalse();
    }
}
