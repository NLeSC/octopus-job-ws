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
     * Scheduler name to take if none is given.
     */
    private final static String SINGLE_SCHEDULER_NAME = "default";

    /**
     * Scheduler configuration used to submit jobs.
     */
    @Valid
    @JsonProperty
    private ImmutableMap<String, SchedulerConfiguration> schedulers = ImmutableMap.of();

    /**
     * Default scheduler to use.
     */
    @JsonProperty
    private String defaultScheduler;

    /**
     * Sandbox configuration used to upload input files and download output files from job.
     */
    @Valid
    @JsonProperty
    private SandboxConfiguration sandbox;

    /**
     * Xenon preferences.
     * These could also be put xenon.properties file, but I like scheduler together with it's preferences
     */
    @JsonProperty
    private ImmutableMap<String, String> preferences = ImmutableMap.of();

    /**
     * Fields required for polling the state of a job.
     */
    @Valid
    @JsonProperty
    private PollConfiguration poll = new PollConfiguration();

    public XenonConfiguration(ImmutableMap<String, SchedulerConfiguration> schedulers, String defaultScheduler, SandboxConfiguration sandbox,
            ImmutableMap<String, String> preferences, PollConfiguration poll) {
        this.schedulers = schedulers;
        this.defaultScheduler = defaultScheduler;
        this.sandbox = sandbox;
        this.preferences = preferences;
        this.poll = poll;
    }

    public XenonConfiguration(SchedulerConfiguration scheduler, SandboxConfiguration sandbox,
            ImmutableMap<String, String> preferences, PollConfiguration poll) {
        this(ImmutableMap.of(SINGLE_SCHEDULER_NAME, scheduler), SINGLE_SCHEDULER_NAME, sandbox, preferences, poll);
    }

    public XenonConfiguration() {
        this.sandbox = null;
    }

    @Valid
    @JsonProperty
    public SchedulerConfiguration getScheduler() {
        return this.schedulers.get(SINGLE_SCHEDULER_NAME);
    }

    @Valid
    @JsonProperty
    public void setScheduler(SchedulerConfiguration scheduler) {
        this.schedulers = ImmutableMap.of(SINGLE_SCHEDULER_NAME, scheduler);
        this.defaultScheduler = SINGLE_SCHEDULER_NAME;
    }

    public ImmutableMap<String, SchedulerConfiguration> getSchedulers() {
        return schedulers;
    }

    public void setSchedulers(ImmutableMap<String, SchedulerConfiguration> schedulers) {
        this.schedulers = schedulers;
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
        return Objects.hashCode(schedulers, preferences, sandbox, poll);
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
        return Objects.equal(this.schedulers, other.schedulers)
                && Objects.equal(this.preferences, other.preferences)
                && Objects.equal(this.poll, other.poll)
                && Objects.equal(this.sandbox, other.sandbox);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(this.schedulers).addValue(this.sandbox)
                .addValue(this.preferences).addValue(this.poll).toString();
    }

    public String getDefaultScheduler() {
        return defaultScheduler;
    }

    public void setDefaultScheduler(String defaultScheduler) {
        this.defaultScheduler = defaultScheduler;
    }
}
