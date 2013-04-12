package nl.esciencecenter.octopus.webservice.job;

import java.net.URI;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

/**
 * JavaGAT configuration
 *
 * @author verhoes
 *
 */
public class OctopusConfiguration {

    /**
     * Scheduler URI used to submit jobs
     */
    @NotEmpty
    @JsonProperty
    private String schedulerURI;

    /**
     * JavaGAT preferences, these could also be put javagat.properties file, but I like scheduler together with it's preferences
     */
    @JsonProperty
    private ImmutableMap<String, Object> preferences = ImmutableMap.of();

    /**
     * The job state has to be polled.
     * This is the time in milliseconds between poll calls.
     */
    @JsonProperty
    private int statePollInterval = 500;

    /**
     * The job state has to be polled.
     * This is the time in milliseconds it polls before canceling the job.
     */
    @JsonProperty
    private int statePollTimeout = 1000*60*60; // one hour

    /**
     * Place where sandboxes get created.
     * For `local` scheduler set it to `file:///tmp` for example.
     * For `ssh` scheduler set it to `ssh://<machine>/tmp`
     */
    @JsonProperty
    private URI sandboxRoot;

    public OctopusConfiguration(String schedulerURI, ImmutableMap<String, Object> preferences, int statePollInterval, int statePollTimeout, URI sandboxRoot) {
        this.schedulerURI = schedulerURI;
        this.preferences = preferences;
        this.statePollInterval = statePollInterval;
        this.statePollTimeout = statePollTimeout;
        this.sandboxRoot = sandboxRoot;
    }

    public OctopusConfiguration() {
        this.schedulerURI = null;
    }

    public String getSchedulerURI() {
        return schedulerURI;
    }

    public void setSchedulerURI(String schedulerURI) {
        this.schedulerURI = schedulerURI;
    }

    public ImmutableMap<String, Object> getPreferences() {
        return preferences;
    }

    public void setPreferences(ImmutableMap<String, Object> preferences) {
        this.preferences = preferences;
    }

    public int getStatePollInterval() {
        return statePollInterval;
    }

    public void setStatePollInterval(int statePollInterval) {
        this.statePollInterval = statePollInterval;
    }

    public int getStatePollTimeout() {
        return statePollTimeout;
    }

    public void setStatePollTimeout(int statePollTimeout) {
        this.statePollTimeout = statePollTimeout;
    }

    public URI getSandboxRoot() {
        return sandboxRoot;
    }

    public void setSandboxRoot(URI sandboxRoot) {
        this.sandboxRoot = sandboxRoot;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(schedulerURI, preferences, statePollInterval);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OctopusConfiguration other = (OctopusConfiguration) obj;
        return Objects.equal(this.schedulerURI, other.schedulerURI) && Objects.equal(this.preferences, other.preferences)
                && Objects.equal(this.statePollInterval, other.statePollInterval);
    }
}
