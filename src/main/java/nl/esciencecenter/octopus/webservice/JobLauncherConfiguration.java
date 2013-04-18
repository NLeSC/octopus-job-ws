package nl.esciencecenter.octopus.webservice;

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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import nl.esciencecenter.octopus.webservice.job.OctopusConfiguration;
import nl.esciencecenter.octopus.webservice.mac.MacCredential;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.client.HttpClientConfiguration;
import com.yammer.dropwizard.config.Configuration;

/**
 * Main configuration of job launcher.
 *
 * @author verhoes
 *
 */
public class JobLauncherConfiguration extends Configuration {
    /**
     * List of mac credentials used to perform authentication
     * when using http client on their scope.
     */
    @NotNull
    @JsonProperty
    private ImmutableList<MacCredential> macs = ImmutableList.of();


    @Valid
    @NotNull
    @JsonProperty("octopus")
    private OctopusConfiguration octopusConfiguration = new OctopusConfiguration();

    /**
     * Http client used to PUT state changes of jobs
     * the launcher is monitoring to the status callback url of the job
     */
    @Valid
    @NotNull
    @JsonProperty
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    /**
     * Constructor
     *
     * @param gat
     * @param macs
     * @param httpClient
     */
    public JobLauncherConfiguration(OctopusConfiguration gat,
            ImmutableList<MacCredential> macs,
            HttpClientConfiguration httpClient) {
        this.octopusConfiguration = gat;
        this.macs = macs;
        this.httpClient = httpClient;
    }

    /**
     * No argument contructor required for JAXB
     */
    public JobLauncherConfiguration() {

    }

    /**
     *
     * @return HttpClientConfiguration
     */
    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

    /**
     *
     * @return GATConfiguration
     */
    public OctopusConfiguration getOctopusConfiguration() {
        return octopusConfiguration;
    }

    /**
     * Credentials used for http client.
     *
     * @return List of MAC Access Authentication credentials
     */
    public ImmutableList<MacCredential> getMacs() {
        return macs;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(macs, octopusConfiguration, httpClient);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JobLauncherConfiguration other = (JobLauncherConfiguration) obj;
        return Objects.equal(this.macs, other.macs)
                && Objects.equal(this.octopusConfiguration, other.octopusConfiguration)
                && Objects.equal(this.httpClient, other.httpClient);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .addValue(this.macs)
                .addValue(this.octopusConfiguration)
                .addValue(this.httpClient)
                .toString();
    }
}
