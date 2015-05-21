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
     * Scheduler configuration used to submit jobs.
     */
    @Valid
    @JsonProperty
    private ImmutableMap<String, LauncherConfiguration> launchers = ImmutableMap.of();

    /**
     * Default scheduler to use.
     */
    @JsonProperty
    private String defaultLauncher;

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

    public XenonConfiguration(ImmutableMap<String, LauncherConfiguration> launchers, String defaultScheduler,
            ImmutableMap<String, String> preferences, PollConfiguration poll) {
        this.launchers = launchers;
        this.defaultLauncher = defaultScheduler;
        this.preferences = preferences;
        this.poll = poll;
    }

    public XenonConfiguration(SchedulerConfiguration scheduler, SandboxConfiguration sandbox, String schedulerName,
            ImmutableMap<String, String> preferences, PollConfiguration poll) {
        this(ImmutableMap.of(schedulerName, new LauncherConfiguration(scheduler, sandbox)), schedulerName, preferences, poll);
    }
    
    public XenonConfiguration() {
        defaultLauncher = null;
    }

    public ImmutableMap<String, LauncherConfiguration> getLaunchers() {
        return launchers;
    }

    public void setLaunchers(ImmutableMap<String, LauncherConfiguration> launchers) {
        this.launchers = launchers;
    }

    public ImmutableMap<String, String> getPreferences() {
        return preferences;
    }

    public void setPreferences(ImmutableMap<String, String> preferences) {
        this.preferences = preferences;
    }

    public PollConfiguration getPoll() {
        return poll;
    }

    public void setPoll(PollConfiguration poll) {
        this.poll = poll;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(launchers, defaultLauncher, preferences, poll);
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
        return Objects.equal(this.launchers, other.launchers)
                && Objects.equal(this.defaultLauncher, other.defaultLauncher)
                && Objects.equal(this.preferences, other.preferences)
                && Objects.equal(this.poll, other.poll);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(this.launchers).addValue(this.defaultLauncher)
                .addValue(this.preferences).addValue(this.poll).toString();
    }

    public String getDefaultLauncher() {
        return defaultLauncher;
    }

    public void setDefaultLauncher(String defaultLauncher) {
        this.defaultLauncher = defaultLauncher;
    }
}
