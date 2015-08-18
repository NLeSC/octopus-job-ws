package nl.esciencecenter.osmium.callback;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import nl.esciencecenter.osmium.JobLauncherConfiguration;

public class BasicCredential {
    @NotEmpty
    @JsonProperty
    String username = null;
    @NotEmpty
    @JsonProperty
    String password = null;

    @NotEmpty
    @JsonProperty
    String scheme = HttpHost.DEFAULT_SCHEME_NAME;
    @NotEmpty
    @JsonProperty
    String hostname = null;
    @NotNull
    @Min(1)
    @Max(65535)
    @JsonProperty
    int port;

    public BasicCredential() {
    }

    public BasicCredential(String username, String password, String scheme, String hostname, int port) {
        this.username = username;
        this.password = password;
        this.scheme = scheme;
        this.hostname = hostname;
        this.port = port;
    }

    HttpHost getHttpHost() {
        return new HttpHost(hostname, port, scheme);
    }

    AuthScope getAuthScope() {
        return new AuthScope(hostname, port);
    }

    UsernamePasswordCredentials getUsernamePasswordCredentials() {
        return new UsernamePasswordCredentials(username, password);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username, password, scheme, hostname, port);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BasicCredential other = (BasicCredential) obj;
        return Objects.equal(this.username, other.username) && Objects.equal(this.password, other.password)
                && Objects.equal(this.scheme, other.scheme) && Objects.equal(this.hostname, other.hostname)
                && Objects.equal(this.port, other.port)
        ;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(JobLauncherConfiguration.class).addValue(username).addValue(password).addValue(scheme)
                .addValue(hostname).addValue(port).toString();
    }
}
