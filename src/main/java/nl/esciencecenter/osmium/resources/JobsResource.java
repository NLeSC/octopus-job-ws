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
package nl.esciencecenter.osmium.resources;


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

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.osmium.api.JobSubmitRequest;
import nl.esciencecenter.osmium.api.SandboxedJob;
import nl.esciencecenter.osmium.callback.CallbackClient;
import nl.esciencecenter.osmium.job.XenonManager;

import com.codahale.metrics.annotation.Timed;

@Path("/job")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JobsResource {
    /**
     * Broker to submit jobs with
     */
    private final XenonManager xenonmanager;
    /**
     * Http client to perform status callbacks with
     */
    private final CallbackClient callbackClient;

    /**
     * Use to make absolute URI to job. Will get injected by JSR311
     */
    @Context
    public UriInfo uriInfo = null;

    /**
     * Constructor
     *
     * @param xenonmanager Xenon manager
     * @param callbackClient http client
     */
    public JobsResource(XenonManager xenonmanager, CallbackClient callbackClient) {
        super();
        this.xenonmanager = xenonmanager;
        this.callbackClient = callbackClient;
    }

    /**
     * Constructor
     *
     * @param xenonmanager Xenon manager
     * @param httpClient http client
     * @param uriInfo uri info
     */
    public JobsResource(XenonManager xenonmanager, CallbackClient callbackClient, UriInfo uriInfo) {
        super();
        this.xenonmanager = xenonmanager;
        this.callbackClient = callbackClient;
        this.uriInfo = uriInfo;
    }

    /**
     * Launch a job based on a request.
     *
     * @param request
     *            A job submission request
     * @return Response with element URI in Location header
     * @throws XenonException When job submission fails
     */
    @POST
    @Timed
    public Response submitJob(@Valid JobSubmitRequest request) throws XenonException {
        SandboxedJob job = xenonmanager.submitJob(request, callbackClient);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        URI location = builder.path(job.getIdentifier()).build();
        return Response.created(location).build();
    }

    /**
     * @return List of URI's of all submitted jobs.
     */
    @GET
    @Timed
    public URI[] getJobs() {
        List<URI> uris = new LinkedList<URI>();
        UriBuilder builder = uriInfo.getAbsolutePathBuilder().path("{jobidentifier}");
        for (SandboxedJob job : xenonmanager.getJobs()) {
            uris.add(builder.build(job.getIdentifier()));
        }
        return uris.toArray(new URI[0]);
    }
}
