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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

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
        HttpClient httpClient = new DefaultHttpClient();
        when(manager.submitJob(request, httpClient)).thenReturn(job);
        when(job.getUrl()).thenReturn(new URI("/job/1234"));

        JobsResource resource = new JobsResource(manager, httpClient);

        SandboxedJob response = resource.submitJob(request);

        assertEquals(response, job);
    }

    @Test
    public void getJobs() throws URISyntaxException {
        // mock manager so it returns a list of jobs
        OctopusManager manager = mock(OctopusManager.class);
        SandboxedJob job = mock(SandboxedJob.class);
        when(job.getUrl()).thenReturn(new URI("/job/1234"));
        Collection<SandboxedJob> jobs = new LinkedList<SandboxedJob>();
        jobs.add(job);
        when(manager.getJobs()).thenReturn(jobs);
        HttpClient httpClient = new DefaultHttpClient();
        JobsResource resource = new JobsResource(manager, httpClient);

        URI[] response = resource.getJobs();

        URI[] expected = {new URI("/job/1234")};
        assertThat(response, is(expected));
    }
}
