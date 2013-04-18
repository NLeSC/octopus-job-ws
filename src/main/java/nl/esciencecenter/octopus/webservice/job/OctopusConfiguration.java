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

import java.net.URI;
import java.util.Properties;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import nl.esciencecenter.octopus.credentials.Credential;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

/**
 * Octopus configuration
 *
 * @author verhoes
 *
 */
public class OctopusConfiguration {

    /**
     * Scheduler URI used to submit jobs
     */
    @NotNull
    @JsonProperty
    private URI scheduler;

    /**
     * Queue of scheduler used to submit jobs
     */
    @NotEmpty
    @JsonProperty
    private String queue;

    /**
     * Place where sandboxes get created. For `local` scheduler set it to `file:///tmp` for example. For `ssh` scheduler set it to
     * `ssh://<machine>/tmp`
     */
    @JsonProperty
    @NotNull
    private URI sandboxRoot;

    /**
     * Octopus preferences, these could also be put octopus.properties file, but I like scheduler together with it's preferences
     */
    @JsonProperty
    private ImmutableMap<String, Object> preferences = ImmutableMap.of();

    /**
     * Fields required for polling the state of a job.
     */
    @Valid
    @JsonProperty("poll")
    private PollConfiguration pollConfiguration = new PollConfiguration();

    public OctopusConfiguration(URI scheduler, String queue, URI sandboxRoot, ImmutableMap<String, Object> preferences) {
        this.scheduler = scheduler;
        this.queue = queue;
        this.sandboxRoot = sandboxRoot;
        this.preferences = preferences;
    }

    public OctopusConfiguration() {
        this.scheduler = null;
    }

    public URI getScheduler() {
        return scheduler;
    }

    public void setScheduler(URI scheduler) {
        this.scheduler = scheduler;
    }

    public ImmutableMap<String, Object> getPreferences() {
        return preferences;
    }

    public void setPreferences(ImmutableMap<String, Object> preferences) {
        this.preferences = preferences;
    }

    public URI getSandboxRoot() {
        return sandboxRoot;
    }

    public void setSandboxRoot(URI sandboxRoot) {
        this.sandboxRoot = sandboxRoot;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public PollConfiguration getPollConfiguration() {
        return pollConfiguration;
    }

    public void setPollConfiguration(PollConfiguration pollConfiguration) {
        this.pollConfiguration = pollConfiguration;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(scheduler, queue, preferences, sandboxRoot, pollConfiguration);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OctopusConfiguration other = (OctopusConfiguration) obj;
        return Objects.equal(this.scheduler, other.scheduler) && Objects.equal(this.preferences, other.preferences)
                && Objects.equal(this.pollConfiguration, other.pollConfiguration) && Objects.equal(this.queue, other.queue)
                && Objects.equal(this.sandboxRoot, other.sandboxRoot);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .addValue(this.scheduler)
                .addValue(this.queue)
                .addValue(this.sandboxRoot)
                .addValue(this.preferences)
                .addValue(this.pollConfiguration)
                .toString();
    }

    public Credential getCredential() {
        // TODO Auto-generated method stub
        return null;
    }

    public Properties getPreferencesAsProperties() {
        // copy over preferences from config to default GAT context
        Properties properties = new Properties();
        Set<String> keys = preferences.keySet();
        for (String key : keys) {
            String value = preferences.get(key).toString();
            properties.setProperty(key, value);
        }
        return properties;
    }
}
