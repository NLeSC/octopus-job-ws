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
package nl.esciencecenter.osmium;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import nl.esciencecenter.osmium.callback.CallbackConfiguration;
import nl.esciencecenter.osmium.job.XenonConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.dropwizard.Configuration;

/**
 * Main configuration of job launcher.
 *
 * @author verhoes
 *
 */
public class JobLauncherConfiguration extends Configuration {
    /**
     * Xenon configuration, contains settings to schedule a job
     */
    @Valid
    @NotNull
    @JsonProperty("xenon")
    private XenonConfiguration xenonConfiguration = new XenonConfiguration();

    /**
     * Callback configuration, contains settings to sent job status changes to some url
     */
    @Valid
    @NotNull
    @JsonProperty("callback")
    private CallbackConfiguration callbackConfiguration = new CallbackConfiguration();

    /**
     * No argument constructor required for JAXB.
     */
    public JobLauncherConfiguration() {

    }

    public JobLauncherConfiguration(XenonConfiguration xenonConfiguration, CallbackConfiguration callbackConfiguration) {
        super();
        this.xenonConfiguration = xenonConfiguration;
        this.callbackConfiguration = callbackConfiguration;
    }

    /**
     * @return Xenon configuration
     */
    public XenonConfiguration getXenonConfiguration() {
        return xenonConfiguration;
    }

    public CallbackConfiguration getCallbackConfiguration() {
        return callbackConfiguration;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(xenonConfiguration, callbackConfiguration);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JobLauncherConfiguration other = (JobLauncherConfiguration) obj;
        return Objects.equal(this.xenonConfiguration, other.xenonConfiguration)
                && Objects.equal(this.callbackConfiguration, other.callbackConfiguration);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(JobLauncherConfiguration.class)
                .addValue(xenonConfiguration)
                .addValue(callbackConfiguration)
                .toString();
    }

}
