package nl.esciencecenter.octopus.webservice.api;

/*
 * #%L
 * Octopus Job Webservice
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

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import nl.esciencecenter.octopus.webservice.resources.JobResource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

/**
 * Returned when job is submitted successfully.
 *
 * @author Stefan Verhoeven <s.verhoeven@esciencecenter.nl>
 */
public class JobSubmitResponse {
    @JsonProperty
    public String jobid;

    public JobSubmitResponse(String jobid) {
        this.jobid = jobid;
    }

    public JobSubmitResponse() {
        super();
        jobid = null;
    }

    /**
     * URL of job.
     *
     * @return URL which can be used to fetch status of job (GET) or cancel it (DELETE).
     */
    public URI getURL() {
        return UriBuilder.fromResource(JobResource.class).build(jobid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(jobid);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JobSubmitResponse other = (JobSubmitResponse) obj;
        return Objects.equal(this.jobid, other.jobid);
    }
}
