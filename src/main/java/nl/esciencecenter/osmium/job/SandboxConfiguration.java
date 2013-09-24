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

import java.util.Map;

import nl.esciencecenter.xenon.files.RelativePath;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

public class SandboxConfiguration {
    /**
     * Scheme of sandbox used for input/output
     */
    @NotEmpty
    @JsonProperty
    private String scheme;

    /**
     * Location of root used to submit jobs.
     * For schema=='file' use '/' or 'C:'.
     * For scheme=='ssh' use 'username@hostname:port'.
     */
    @JsonProperty
    private String location;

    /**
     * Xenon sandbox filesystem preferences
     */
    @JsonProperty
    private ImmutableMap<String, String> properties = ImmutableMap.of();

    /**
     * Path inside location to create sandboxes.
     */
    @NotEmpty
    @JsonProperty
    private String path;

    public SandboxConfiguration() {

    }

    public SandboxConfiguration(String scheme, String location, String path, ImmutableMap<String, String> properties) {
        super();
        this.scheme = scheme;
        this.location = location;
        this.properties = properties;
        this.path = path;
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

    public RelativePath getPath() {
        return new RelativePath(path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(scheme, location, path, properties);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SandboxConfiguration other = (SandboxConfiguration) obj;
        return Objects.equal(this.scheme, other.scheme)
                && Objects.equal(this.location, other.location)
                && Objects.equal(this.properties, other.properties)
                && Objects.equal(this.path, other.path);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(this.scheme).addValue(this.path)
                .addValue(this.location).addValue(this.properties).toString();
    }
}
