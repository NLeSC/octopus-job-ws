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
package nl.esciencecenter.octopus.webservice;

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
     * List of mac credentials used to perform authentication when using http client on their scope.
     */
    @NotNull
    @JsonProperty
    private ImmutableList<MacCredential> macs = ImmutableList.of();

    /**
     * Octopus configuration, contains settings to schedule a job
     */
    @Valid
    @NotNull
    @JsonProperty("octopus")
    private OctopusConfiguration octopusConfiguration = new OctopusConfiguration();

    /**
     * Http client used to PUT state changes of jobs the launcher is monitoring to the status callback url of the job
     */
    @Valid
    @NotNull
    @JsonProperty
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    /**
     * Http Client can be configured so self signed ssl certificates work.
     */
    @Valid
    @NotNull
    @JsonProperty
    private Boolean useInsecureSSL = false;

    /**
     * Constructor
     *
     * @param octopus
     *            Octopus configuration
     * @param macs
     *            List of macs
     * @param httpClient
     *            Http client configuration
     */
    public JobLauncherConfiguration(OctopusConfiguration octopus, final ImmutableList<MacCredential> macs,
            HttpClientConfiguration httpClient) {
        this.octopusConfiguration = octopus;
        this.macs = macs;
        this.httpClient = httpClient;
    }

    public JobLauncherConfiguration(OctopusConfiguration octopusConfiguration, final ImmutableList<MacCredential> macs,
            HttpClientConfiguration httpClient, Boolean useInsecureSSL) {
        super();
        this.macs = macs;
        this.octopusConfiguration = octopusConfiguration;
        this.httpClient = httpClient;
        this.useInsecureSSL = useInsecureSSL;
    }

    /**
     * No argument contructor required for JAXB.
     */
    public JobLauncherConfiguration() {

    }

    /**
     * @return Http client configuration
     */
    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

    /**
     * @return Octopus configuration
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

    public Boolean isUseInsecureSSL() {
        return useInsecureSSL;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(macs, octopusConfiguration, httpClient);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JobLauncherConfiguration other = (JobLauncherConfiguration) obj;
        return Objects.equal(this.macs, other.macs) && Objects.equal(this.octopusConfiguration, other.octopusConfiguration)
                && Objects.equal(this.httpClient, other.httpClient);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(this.macs).addValue(this.octopusConfiguration).addValue(this.httpClient)
                .toString();
    }
}
