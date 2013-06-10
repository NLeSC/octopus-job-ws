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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;
import nl.esciencecenter.octopus.webservice.api.SandboxedJob;
import nl.esciencecenter.octopus.webservice.job.OctopusManager;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

public class JobsResourceTest {

    @Test
    public void testSubmitJob() throws Exception {
        JobSubmitRequest request = mock(JobSubmitRequest.class);
        OctopusManager manager = mock(OctopusManager.class);
        SandboxedJob job = mock(SandboxedJob.class);
        when(job.getIdentifier()).thenReturn("11111111-1111-1111-1111-111111111111");
        HttpClient httpClient = new DefaultHttpClient();
        when(manager.submitJob(request, httpClient)).thenReturn(job);
        UriInfo uriInfo = mock(UriInfo.class);
        UriBuilder builder = UriBuilder.fromUri("http://localhost/job/");
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(builder);
        JobsResource resource = new JobsResource(manager, httpClient, uriInfo);

        Response response = resource.submitJob(request);

        assertEquals(201, response.getStatus());
        URI expected = new URI("http://localhost/job/11111111-1111-1111-1111-111111111111");
        assertEquals(expected, response.getMetadata().getFirst("Location"));
    }

    @Test
    public void getJobs() throws URISyntaxException {
        // mock manager so it returns a list of jobs
        OctopusManager manager = mock(OctopusManager.class);
        SandboxedJob job = mock(SandboxedJob.class);
        when(job.getIdentifier()).thenReturn("11111111-1111-1111-1111-111111111111");
        Collection<SandboxedJob> jobs = new LinkedList<SandboxedJob>();
        jobs.add(job);
        when(manager.getJobs()).thenReturn(jobs);
        HttpClient httpClient = new DefaultHttpClient();
        UriInfo uriInfo = mock(UriInfo.class);
        UriBuilder builder = UriBuilder.fromUri("http://localhost/job/");
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(builder);
        JobsResource resource = new JobsResource(manager, httpClient, uriInfo);

        URI[] response = resource.getJobs();

        URI[] expected = {new URI("http://localhost/job/11111111-1111-1111-1111-111111111111")};
        assertThat(response, is(expected));
    }

}
