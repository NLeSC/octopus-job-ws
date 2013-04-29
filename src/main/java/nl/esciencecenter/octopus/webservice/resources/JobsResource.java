package nl.esciencecenter.octopus.webservice.resources;

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

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;
import nl.esciencecenter.octopus.webservice.api.JobSubmitResponse;
import nl.esciencecenter.octopus.webservice.job.OctopusManager;
import nl.esciencecenter.octopus.webservice.job.SandboxedJob;

import org.apache.http.client.HttpClient;

import com.yammer.metrics.annotation.Timed;


@Path("/job")
public class JobsResource {
    /**
     * Broker to submit jobs with
     */
    private final OctopusManager octopusmanager;
    /**
     * Http client to perform status callbacks with
     */
    private final HttpClient httpClient;

    /**
     * Constructor
     *
     * @param octopusmanager
     * @param httpClient
     */
    public JobsResource(OctopusManager octopusmanager, HttpClient httpClient) {
        this.octopusmanager = octopusmanager;
        this.httpClient = httpClient;
    }

    /**
     * Launch a job based on a request.
     *
     * @param request A job submission request
     * @return a job submission response
     * @throws Exception
     * @throws GATInvocationException
     * @throws GATObjectCreationException
     */
    @POST
    @Timed
    public JobSubmitResponse submitJob(JobSubmitRequest request) throws Exception {
        Job job = octopusmanager.submitJob(request, httpClient);

        return new JobSubmitResponse(job.getIdentifier());
    }

    @GET
    @Timed
    public Collection<SandboxedJob> getJobs() {
        return octopusmanager.jobs();
    }
}
