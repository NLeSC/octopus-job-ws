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

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.esciencecenter.osmium.callback.CallbackClient;
import nl.esciencecenter.osmium.callback.HttpClientSSLSetupException;
import nl.esciencecenter.osmium.health.JobLauncherHealthCheck;
import nl.esciencecenter.osmium.job.XenonManager;
import nl.esciencecenter.osmium.resources.JobResource;
import nl.esciencecenter.osmium.resources.JobsResource;
import nl.esciencecenter.xenon.XenonException;

/**
 * Service to submit jobs using a Xenon scheduler and sandbox.
 *
 * @author verhoes
 *
 */
public class JobLauncherService extends Application<JobLauncherConfiguration> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JobLauncherService.class);

    /**
     * Entry point
     *
     * @param args
     *            CLI arguments
     * @throws Exception
     *             if something goes wrong
     */
    public static void main(String[] args) throws Exception {
        new JobLauncherService().run(args);
    }

    @Override
    public String getName() {
        return "osmium";
    }

    @Override
    public void initialize(Bootstrap<JobLauncherConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(JobLauncherConfiguration configuration, Environment environment) throws URISyntaxException,
            KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, XenonException, HttpClientSSLSetupException {
        XenonManager xenon = new XenonManager(configuration.getXenonConfiguration());
        environment.lifecycle().manage(xenon);

        CallbackClient callbackClient = new CallbackClient(environment, configuration.getCallbackConfiguration());

        environment.jersey().register(new JobsResource(xenon, callbackClient));
        environment.jersey().register(new JobResource(xenon));
        environment.healthChecks().register("osmium", new JobLauncherHealthCheck());
    }
}
