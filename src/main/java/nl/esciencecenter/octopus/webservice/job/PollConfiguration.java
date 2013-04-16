package nl.esciencecenter.octopus.webservice.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class PollConfiguration {
    /**
     * The job state has to be polled.
     * This is the time in milliseconds between poll calls.
     */
    @JsonProperty
    private int interval = 500;
    /**
     * The job state has to be polled.
     * This is the time in milliseconds it polls before canceling the job.
     */
    @JsonProperty
    private int timeout = 1000*60*60;

    public PollConfiguration(int interval, int timeout) {
        this.interval = interval;
        this.timeout = timeout;
    }

    public PollConfiguration() {
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(interval, timeout);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PollConfiguration other = (PollConfiguration) obj;
        return Objects.equal(this.interval, other.interval) && Objects.equal(this.timeout, other.timeout);
    }

    @Override
    public String toString()
    {
       return com.google.common.base.Objects.toStringHelper(this)
                 .addValue(this.interval)
                 .addValue(this.timeout)
                 .toString();
    }
}