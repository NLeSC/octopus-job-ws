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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.webservice.api.JobStatusResponse;
import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;
import nl.esciencecenter.octopus.webservice.api.JobSubmitResponse;
import nl.esciencecenter.octopus.webservice.job.OctopusManager;

import org.apache.http.client.HttpClient;

import com.yammer.metrics.annotation.Timed;

/**
 * Job Resource.
 *
 * @author verhoes
 *
 */
@Path("/job")
public class JobLauncherResource {
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
     * @param broker
     * @param httpClient
     */
    public JobLauncherResource(OctopusManager octopusmanager, HttpClient httpClient) {
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

    @GET @Path("/{jobidentifier}")
    @Timed
    public JobStatusResponse stateOfJob(@PathParam("jobidentifier") String jobIdentifier) throws OctopusIOException, OctopusException {
        return octopusmanager.stateOfJob(jobIdentifier);
    }

    @DELETE @Path("/{jobidentifier}")
    @Timed
    public void cancelJob(@PathParam("jobidentifier") String jobIdentifier) throws OctopusIOException, OctopusException {
        octopusmanager.cancelJob(jobIdentifier);
    }
}
