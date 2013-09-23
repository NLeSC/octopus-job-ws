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


import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;
import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import nl.esciencecenter.osmium.job.XenonConfiguration;
import nl.esciencecenter.osmium.job.PollConfiguration;
import nl.esciencecenter.osmium.job.SandboxConfiguration;
import nl.esciencecenter.osmium.job.SchedulerConfiguration;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class XenonConfigurationTest {
    public XenonConfiguration sampleConfig() throws URISyntaxException {
        SchedulerConfiguration scheduler = new SchedulerConfiguration("local", null, "multi", null);
        SandboxConfiguration sandbox = new SandboxConfiguration("file", null, "/tmp/sandboxes", null);
        ImmutableMap<String, String> prefs = ImmutableMap.of(
                "xenon.adaptors.local.queue.multi.maxConcurrentJobs", "1");
        PollConfiguration pollConf = new PollConfiguration();
        XenonConfiguration config = new XenonConfiguration(scheduler, sandbox, prefs, pollConf);
        return config;
    }

    @Test
    public void testGetPreferences_Filled() throws URISyntaxException {
        ImmutableMap<String, String> prefs = ImmutableMap.of(
                "xenon.adaptors.local.queue.multi.maxConcurrentJobs", "1");

        XenonConfiguration config = sampleConfig();

        assertThat(config.getPreferences()).isEqualTo(prefs);
    }

    @Test
    public void testGetPreferences_Empty() {
        XenonConfiguration g = new XenonConfiguration();
        ImmutableMap<String, String> expected = ImmutableMap.of();
        assertThat(g.getPreferences()).isEqualTo(expected);
    }

    @Test
    public void testSetPreferences() {
        XenonConfiguration g = new XenonConfiguration();
        ImmutableMap<String, String> prefs = ImmutableMap.of("mykey", "myval");
        g.setPreferences(prefs);
        assertThat(g.getPreferences()).isEqualTo(prefs);
    }

    @Test
    public void deserializesFromJson() throws IOException, URISyntaxException {
        XenonConfiguration actual = fromJson(jsonFixture("fixtures/xenon.json"),
                XenonConfiguration.class);

        ImmutableMap<String, String> emptyProps = ImmutableMap.of();
        SchedulerConfiguration expectedScheduler = new SchedulerConfiguration("local", null, "multi", emptyProps);
        assertThat(actual.getScheduler()).isEqualTo(expectedScheduler);

        SandboxConfiguration expectedSandbox = new SandboxConfiguration("file", "/", "/tmp/sandboxes", emptyProps);
        assertThat(actual.getSandbox()).isEqualTo(expectedSandbox);

        ImmutableMap<String, String> prefs = ImmutableMap.of(
                "xenon.adaptors.local.queue.multi.maxConcurrentJobs", "4");
        assertThat(actual.getPreferences()).isEqualTo(prefs);
        PollConfiguration expected_poll = new PollConfiguration(500, 3600000, 43200000);
        assertThat(actual.getPoll()).isEqualTo(expected_poll);
    }

    @Test
    public void validateFromJson() throws IOException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        XenonConfiguration actual = fromJson(jsonFixture("fixtures/xenon.json"),
                XenonConfiguration.class);

        Set<ConstraintViolation<XenonConfiguration>> constraintViolations = validator.validateProperty(actual, "scheduler");

        assertThat(constraintViolations.size()).isEqualTo(0);
    }

    @Test
    public void testToString() throws URISyntaxException {
        XenonConfiguration config = sampleConfig();
        String expected = "XenonConfiguration{SchedulerConfiguration{local, multi, null, null}, SandboxConfiguration{file, /tmp/sandboxes, null, null}, {xenon.adaptors.local.queue.multi.maxConcurrentJobs=1}, PollConfiguration{30000, 3600000, 43200000}}";
        assertThat(config.toString()).isEqualTo(expected);
    }

    @Test
    public void testHashCode() throws URISyntaxException {
        XenonConfiguration config = sampleConfig();
        int expected = -318369353;
        assertThat(config.hashCode()).isEqualTo(expected);
    }

    @Test
    public void testEquals_SameObj_equal() throws URISyntaxException {
        XenonConfiguration config = sampleConfig();
        assertThat(config.equals(config)).isTrue();
    }

    @Test
    public void testEquals_DiffClass_unequal() throws URISyntaxException {
        XenonConfiguration config = sampleConfig();
        URI uri = new URI("local:///");
        assertThat(config.equals(uri)).isFalse();
    }

    @Test
    public void testEquals_SameContent_equal() throws URISyntaxException {
        XenonConfiguration config1 = sampleConfig();
        XenonConfiguration config2 = sampleConfig();
        assertThat(config1.equals(config2)).isTrue();
    }
}
