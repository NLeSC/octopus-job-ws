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
package nl.esciencecenter.osmium.api;

import java.util.Map;

import javax.validation.constraints.NotNull;

import nl.esciencecenter.xenon.jobs.JobStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;

public class JobStatusResponse {
    @NotNull
    private final String state;
    private final Integer exitCode;
    @NotNull
    private final Exception exception;
    @NotNull
    private final boolean running;
    @NotNull
    private final boolean done;
    private final Map<String, String> schedulerSpecficInformation;

    public JobStatusResponse(JobStatus jobStatus) {
        if (jobStatus == null) {
            state = "INITIAL";
            exitCode = null;
            exception = null;
            running = false;
            done = false;
            schedulerSpecficInformation = null;
        } else {
            state = jobStatus.getState();
            exitCode = jobStatus.getExitCode();
            exception = jobStatus.getException();
            running = jobStatus.isRunning();
            done = jobStatus.isDone();
            schedulerSpecficInformation = jobStatus.getSchedulerSpecficInformation();
        }
    }

    public JobStatusResponse(String state, boolean running, boolean done, Integer exitCode, Exception exception,
            Map<String, String> schedulerSpecficInformation) {
        super();
        this.state = state;
        this.running = running;
        this.done = done;
        this.exitCode = exitCode;
        this.exception = exception;
        this.schedulerSpecficInformation = schedulerSpecficInformation;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public String getState() {
        return state;
    }

    /*
     * During serialization don't serialize exception, but return exception message.
     */
    @JsonIgnore
    public Exception getException() {
        return exception;
    }

    @JsonProperty("exception")
    public String getExceptionMessage() {
        if (exception == null) {
            return null;
        }
        return exception.getMessage();
    }

    public boolean isDone() {
        return done;
    }

    public boolean isRunning() {
        return running;
    }

    public Map<String, String> getSchedulerSpecficInformation() {
        return schedulerSpecficInformation;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(state, running, done, exitCode, exception, schedulerSpecficInformation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JobStatusResponse other = (JobStatusResponse) obj;
        return Objects.equal(this.state, other.state)
                && Objects.equal(this.running, other.running)
                && Objects.equal(this.done, other.done)
                && Objects.equal(this.exitCode, other.exitCode)
                && Objects.equal(this.exception, other.exception)
                && Objects.equal(this.schedulerSpecficInformation, other.schedulerSpecficInformation);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .addValue(this.state)
                .addValue(this.running)
                .addValue(this.done)
                .addValue(this.exitCode)
                .addValue(this.exception)
                .addValue(this.schedulerSpecficInformation)
                .toString();
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
