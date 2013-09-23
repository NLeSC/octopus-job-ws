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

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

/**
 * Xenon configuration
 *
 * @author verhoes
 *
 */
public class XenonConfiguration {

    /**
     * Scheduler configuration used to submit jobs
     */
    @Valid
    @JsonProperty
    private SchedulerConfiguration scheduler;

    /**
     * Sandbox configuration used to upload input files and download output files from job.
     */
    @Valid
    @JsonProperty
    private SandboxConfiguration sandbox;

    /**
     * Xenon preferences, these could also be put xenon.properties file, but I like scheduler together with it's preferences
     */
    @JsonProperty
    private ImmutableMap<String, String> preferences = ImmutableMap.of();

    /**
     * Fields required for polling the state of a job.
     */
    @Valid
    @JsonProperty
    private PollConfiguration poll = new PollConfiguration();

    public XenonConfiguration(SchedulerConfiguration scheduler, SandboxConfiguration sandbox,
            ImmutableMap<String, String> preferences, PollConfiguration poll) {
        this.scheduler = scheduler;
        this.sandbox = sandbox;
        this.preferences = preferences;
        this.poll = poll;
    }

    public XenonConfiguration() {
        this.scheduler = null;
        this.sandbox = null;
    }

    public SchedulerConfiguration getScheduler() {
        return scheduler;
    }

    public void setScheduler(SchedulerConfiguration scheduler) {
        this.scheduler = scheduler;
    }

    public ImmutableMap<String, String> getPreferences() {
        return preferences;
    }

    public void setPreferences(ImmutableMap<String, String> preferences) {
        this.preferences = preferences;
    }

    public SandboxConfiguration getSandbox() {
        return sandbox;
    }

    public void setSandbox(SandboxConfiguration sandboxConfiguration) {
        sandbox = sandboxConfiguration;
    }

    public PollConfiguration getPoll() {
        return poll;
    }

    public void setPoll(PollConfiguration poll) {
        this.poll = poll;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(scheduler, preferences, sandbox, poll);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        XenonConfiguration other = (XenonConfiguration) obj;
        return Objects.equal(this.scheduler, other.scheduler)
                && Objects.equal(this.preferences, other.preferences)
                && Objects.equal(this.poll, other.poll)
                && Objects.equal(this.sandbox, other.sandbox);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(this.scheduler).addValue(this.sandbox)
                .addValue(this.preferences).addValue(this.poll).toString();
    }
}
