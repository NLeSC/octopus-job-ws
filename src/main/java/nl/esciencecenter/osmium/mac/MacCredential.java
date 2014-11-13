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
package nl.esciencecenter.osmium.mac;

import java.net.URI;
import java.security.Principal;

import javax.validation.constraints.NotNull;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

/**
 * Credential information required to perform MAC Access Authentication.
 *
 * @author verhoes
 *
 */
public class MacCredential implements Credentials {
    /**
     * The MAC id
     */
    @NotNull
    @JsonProperty
    private String id = null;

    /**
     * The MAC key
     */
    @NotNull
    @JsonProperty
    private String key = null;

    /**
     * Algorithm used to sign requests
     */
    @JsonProperty
    @NotNull
    private String algorithm = "hmac-sha-1";

    /**
     * When scope is part of url used by http client
     * then MAC Access Authentication will be used
     * with this.id, this.key and this.algorithm
     */
    @JsonProperty
    @NotNull
    private URI scope = null;

    /**
     * Constructor
     *
     * @param id MAC id
     * @param key MAC key
     * @param scope URI for which the id, key combination should be used
     * Algorithm is set to 'hmac-sha-1'.
     */
    public MacCredential(String id, String key, URI scope) {
        this.id = id;
        this.key = key;
        this.scope = scope;
    }

    public MacCredential() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public URI getScope() {
        return scope;
    }

    public void setScope(URI scope) {
        this.scope = scope;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Id/Key pair belong to a certain URI.
     *
     * @return AuthScope
     */
    public AuthScope getAuthScope() {
        return new AuthScope(scope.getHost(), MacScheme.getPort(scope), "",
                MacScheme.SCHEME_NAME);
    }

    public Principal getUserPrincipal() {
        return new BasicUserPrincipal(id);
    }

    public String getPassword() {
        return key;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, key, algorithm, scope);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MacCredential other = (MacCredential) obj;
        return Objects.equal(this.id, other.id)
                && Objects.equal(this.key, other.key)
                && Objects.equal(this.algorithm, other.algorithm)
                && Objects.equal(this.scope, other.scope);
    }

    @Override
    public String toString() {
        return "MacCredential [id=" + id + ", key=" + key + ", algorithm="
                + algorithm + ", scope=" + scope + "]";
    }
}
