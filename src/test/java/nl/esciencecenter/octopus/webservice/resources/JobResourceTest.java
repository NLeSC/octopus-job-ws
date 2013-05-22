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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import nl.esciencecenter.octopus.exceptions.NoSuchJobException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.webservice.api.SandboxedJob;
import nl.esciencecenter.octopus.webservice.job.OctopusManager;

import org.junit.Test;

public class JobResourceTest {

    @Test
    public void testStateJob_KnownJob_JobStatusReturned() throws OctopusIOException, OctopusException {
        OctopusManager manager = mock(OctopusManager.class);
        SandboxedJob job = mock(SandboxedJob.class);
        when(manager.getJob("1234")).thenReturn(job);

        JobResource resource = new JobResource(manager);

        SandboxedJob response = resource.getJob("1234");

        assertThat(response).isEqualTo(job);
    }

    @Test
    public void testStateJob_UnknownJob_ThrowsWebApplicationException() throws OctopusIOException, OctopusException {
        String request = "1234";
        OctopusManager manager = mock(OctopusManager.class);
        doThrow(new NoSuchJobException("", "Job not found")).when(manager).getJob("1234");

        JobResource resource = new JobResource(manager);

        try {
            resource.getJob(request);
            fail("WebApplicationException not thrown");
        } catch (WebApplicationException e) {
            // Equal with `new WebApplicationException(Status.NOT_FOUND)` fails on unimplemented equals().
            assertThat(e.getResponse().getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void cancelJob_KnownJob_JobCanceled() throws OctopusException, IOException {
        String request = "1234";
        OctopusManager manager = mock(OctopusManager.class);

        JobResource resource = new JobResource(manager);

        resource.cancelJob(request);

        verify(manager).cancelJob("1234");
    }

    @Test
    public void cancelJob_UnknownJob_ThrowsWebApplicationException() throws OctopusException, IOException {
        String request = "1234";
        OctopusManager manager = mock(OctopusManager.class);
        doThrow(new NoSuchJobException("", "Job not found")).when(manager).cancelJob("1234");

        JobResource resource = new JobResource(manager);

        try {
            resource.cancelJob(request);
            fail("WebApplicationException not thrown");
        } catch (WebApplicationException e) {
            // Equal with `new WebApplicationException(Status.NOT_FOUND)` fails on unimplemented equals().
            assertThat(e.getResponse().getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
        }
    }
}
