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

import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class PollConfiguration {
    /**
     * The job state has to be polled.
     * This is the time in milliseconds between poll calls.
     * Default 30 seconds.
     */
    @JsonProperty
    private long interval = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);
    /**
     * The job state has to be polled.
     * This is the time in milliseconds it polls before canceling the job.
     * Default 1 hour.
     */
    @JsonProperty
    private long cancelTimeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
    /**
     * The job state has to be polled.
     * This is the time in milliseconds it polls before deleting the job.
     * Default 12 hour.
     */
    @JsonProperty
    private long deleteTimeout = TimeUnit.MILLISECONDS.convert(12, TimeUnit.HOURS);

    public PollConfiguration(long interval, long cancelTimeout, long deleteTimeout) {
        super();
        this.interval = interval;
        this.cancelTimeout = cancelTimeout;
        this.deleteTimeout = deleteTimeout;
    }

    public PollConfiguration() {
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getCancelTimeout() {
        return cancelTimeout;
    }

    public void setCancelTimeout(long cancelTimeout) {
        this.cancelTimeout = cancelTimeout;
    }

    public long getDeleteTimeout() {
        return deleteTimeout;
    }

    public void setDeleteTimeout(long deleteTimeout) {
        this.deleteTimeout = deleteTimeout;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(interval, cancelTimeout, deleteTimeout);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PollConfiguration other = (PollConfiguration) obj;
        return Objects.equal(this.interval, other.interval) && Objects.equal(this.cancelTimeout, other.cancelTimeout)
                && Objects.equal(this.deleteTimeout, other.deleteTimeout);
    }

    @Override
    public String toString()
    {
       return Objects.toStringHelper(this)
                 .addValue(this.interval)
                 .addValue(this.cancelTimeout)
                 .addValue(this.deleteTimeout)
                 .toString();
    }
}
