package nl.esciencecenter.octopus.webservice.job;

import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

public class SchedulerConfiguration {
    /**
     * Scheme of scheduler used to submit jobs
     */
    @NotEmpty
    @JsonProperty
    private String scheme;

    /**
     * Location of scheduler used to submit jobs.
     * eg. For scheme=='ssh' use 'username@hostname:port'.
     */
    @JsonProperty
    private String location;

    /**
     * Queue of scheduler used to submit jobs
     */
    @NotEmpty
    @JsonProperty
    private String queue;

    /**
     * Octopus scheduler preferences
     */
    @JsonProperty
    private ImmutableMap<String, String> properties = ImmutableMap.of();

    public SchedulerConfiguration() {
    }

    public SchedulerConfiguration(String scheme, String location, String queue, ImmutableMap<String, String> properties) {
        super();
        this.scheme = scheme;
        this.location = location;
        this.properties = properties;
        this.queue = queue;
    }

    public String getScheme() {
        return scheme;
    }

    public String getLocation() {
        return location;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getQueue() {
        return queue;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(scheme, location, queue, properties);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SchedulerConfiguration other = (SchedulerConfiguration) obj;
        return Objects.equal(this.scheme, other.scheme)
                && Objects.equal(this.location, other.location)
                && Objects.equal(this.queue, other.queue)
                && Objects.equal(this.properties, other.properties);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(this.scheme).addValue(this.queue)
                .addValue(this.location).addValue(this.properties).toString();
    }
}
