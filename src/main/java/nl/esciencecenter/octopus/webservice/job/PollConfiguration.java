package nl.esciencecenter.octopus.webservice.job;

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
        if (cancelTimeout >= deleteTimeout) {
            // TODO throw exception
        }
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
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
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