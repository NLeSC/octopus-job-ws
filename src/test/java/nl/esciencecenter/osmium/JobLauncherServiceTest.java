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
package nl.esciencecenter.osmium;



import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import nl.esciencecenter.osmium.callback.BasicCredential;
import nl.esciencecenter.osmium.callback.CallbackConfiguration;
import nl.esciencecenter.osmium.health.JobLauncherHealthCheck;
import nl.esciencecenter.osmium.job.PollConfiguration;
import nl.esciencecenter.osmium.job.SandboxConfiguration;
import nl.esciencecenter.osmium.job.SchedulerConfiguration;
import nl.esciencecenter.osmium.job.XenonConfiguration;
import nl.esciencecenter.osmium.job.XenonManager;

public class JobLauncherServiceTest {
    private Environment environment;
    private JobLauncherService service;
    private JobLauncherConfiguration config;

    @Before
    public void setUp() throws URISyntaxException {
        environment = getEnvironment();
        service = new JobLauncherService();
        config = sampleConfiguration();
    }

    private Environment getEnvironment() {
        Environment environment = mock(Environment.class);
        JerseyEnvironment jersey = mock(JerseyEnvironment.class);
        when(environment.jersey()).thenReturn(jersey);
        LifecycleEnvironment lifecycle = mock(LifecycleEnvironment.class);
        when(environment.lifecycle()).thenReturn(lifecycle);
        HealthCheckRegistry healthchecks = mock(HealthCheckRegistry.class);
        when(environment.healthChecks()).thenReturn(healthchecks);
        MetricRegistry metrics = mock(MetricRegistry.class);
        when(environment.metrics()).thenReturn(metrics);
        return environment;
    }

    @Test
    public void testGetName() {
        assertEquals("osmium", service.getName());
    }

    @Test
    public void testRun_managed_xenon() throws Exception {
        service.run(config, environment);

        verify(environment.lifecycle()).manage(any(XenonManager.class));
    }

    @Test
    public void testRun_registered_resources() throws Exception {
        service.run(config, environment);

        verify(environment.jersey(), times(2)).register(any(Object.class));
    }

    @Test
    public void testRun_registered_healthcheck() throws Exception {
        service.run(config, environment);

        verify(environment.healthChecks()).register(eq("osmium"), any(JobLauncherHealthCheck.class));
    }

    private JobLauncherConfiguration sampleConfiguration()
            throws URISyntaxException {
        ImmutableMap<String, String> prefs = ImmutableMap.of(
                "xenon.adaptors.local.queue.multi.maxConcurrentJobs", "1");
        SchedulerConfiguration scheduler = new SchedulerConfiguration("local", null, "multi", null);
        SandboxConfiguration sandbox = new SandboxConfiguration("file", "/", "/tmp/sandboxes", null);
        PollConfiguration pollConf = new PollConfiguration();
        XenonConfiguration xenon = new XenonConfiguration(scheduler, sandbox, "default", prefs, pollConf);
        HttpClientConfiguration httpConf = new HttpClientConfiguration();
        ImmutableList<BasicCredential> basicCredentials = ImmutableList.of();
        Boolean useInsecureSSL = false;
        CallbackConfiguration callBackConfiguration = new CallbackConfiguration(httpConf, basicCredentials, useInsecureSSL);

        JobLauncherConfiguration config = new JobLauncherConfiguration(xenon, callBackConfiguration);
        return config;
    }
}
