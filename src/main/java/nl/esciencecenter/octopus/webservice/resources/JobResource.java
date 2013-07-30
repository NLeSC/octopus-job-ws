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
package nl.esciencecenter.octopus.webservice.resources;


import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.MediaType;

import nl.esciencecenter.octopus.exceptions.NoSuchJobException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.webservice.api.SandboxedJob;
import nl.esciencecenter.octopus.webservice.job.OctopusManager;

import com.yammer.metrics.annotation.Timed;

/**
 * Job Resource.
 *
 * @author verhoes
 *
 */
@Path("/job/{jobidentifier}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JobResource {
    private OctopusManager octopusmanager;

    public JobResource(OctopusManager octopusmanager) {
        super();
        this.octopusmanager = octopusmanager;
    }

    @GET
    @Timed
    public SandboxedJob getJob(@PathParam("jobidentifier") String jobIdentifier) throws OctopusIOException, OctopusException {
        try {
            return octopusmanager.getJob(jobIdentifier);
        } catch (NoSuchJobException e) {
            throw new WebApplicationException(e, Status.NOT_FOUND);
        }
    }

    @DELETE
    @Timed
    public void cancelJob(@PathParam("jobidentifier") String jobIdentifier) throws OctopusException, IOException {
        try {
            octopusmanager.cancelJob(jobIdentifier);
        } catch (NoSuchJobException e) {
            throw new WebApplicationException(e, Status.NOT_FOUND);
        }
    }
}
