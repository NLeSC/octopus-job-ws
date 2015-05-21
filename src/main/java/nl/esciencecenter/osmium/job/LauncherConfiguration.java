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
/**
 *
 * @author joris
 */
public class LauncherConfiguration {
    /**
     * Scheduler configuration used to submit jobs.
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

    public LauncherConfiguration() {
        scheduler = null;
        sandbox = null;
    }

    public LauncherConfiguration(SchedulerConfiguration scheduler, SandboxConfiguration sandbox) {
        this.scheduler = scheduler;
        this.sandbox = sandbox;
    }

    public SchedulerConfiguration getScheduler() {
        return scheduler;
    }

    public void setSchedulers(SchedulerConfiguration schedulers) {
        this.scheduler = schedulers;
    }

    public SandboxConfiguration getSandbox() {
        return sandbox;
    }

    public void setSandbox(SandboxConfiguration sandboxConfiguration) {
        sandbox = sandboxConfiguration;
    }
    
    
    @Override
    public int hashCode() {
        return Objects.hashCode(scheduler, sandbox);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LauncherConfiguration other = (LauncherConfiguration) obj;
        return Objects.equal(this.scheduler, other.scheduler)
                && Objects.equal(this.sandbox, other.sandbox);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .addValue(this.scheduler)
                .addValue(this.sandbox)
                .toString();
    }

}
