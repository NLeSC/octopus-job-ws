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

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;
import nl.esciencecenter.octopus.webservice.api.SandboxedJob;
import nl.esciencecenter.octopus.webservice.job.OctopusManager;

import org.apache.http.client.HttpClient;

import com.yammer.metrics.annotation.Timed;

@Path("/job")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
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
     * Use to make absolute URI to job.
     * Will get injected by JSR311
     */
    @Context
    public UriInfo uriInfo = null;

    /**
     * Constructor
     *
     * @param octopusmanager
     * @param httpClient
     * @param uriInfo
     */
    public JobsResource(OctopusManager octopusmanager, HttpClient httpClient) {
        super();
        this.octopusmanager = octopusmanager;
        this.httpClient = httpClient;
    }

    /**
     * Constructor
     *
     * @param octopusmanager
     * @param httpClient
     * @param uriInfo
     */
    public JobsResource(OctopusManager octopusmanager, HttpClient httpClient, UriInfo uriInfo) {
        super();
        this.octopusmanager = octopusmanager;
        this.httpClient = httpClient;
        this.uriInfo = uriInfo;
    }

    /**
     * Launch a job based on a request.
     *
     * @param request
     *            A job submission request
     * @return Response with element URI in Location header
     * @throws Exception
     * @throws GATInvocationException
     * @throws GATObjectCreationException
     */
    @POST
    @Timed
    public Response submitJob(@Valid JobSubmitRequest request) throws Exception {
        SandboxedJob job = octopusmanager.submitJob(request, httpClient);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        URI location = builder.path(job.getIdentifier()).build();
        Response response = Response.created(location).build();
        return response;
    }

    /**
     * @return List of URI's of all submitted jobs.
     */
    @GET
    @Timed
    public URI[] getJobs() {
        List<URI> uris = new LinkedList<URI>();
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        for (SandboxedJob job : octopusmanager.getJobs()) {
            uris.add(builder.path(job.getIdentifier()).build());
        }
        return uris.toArray(new URI[0]);
    }
}
