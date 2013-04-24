package nl.esciencecenter.octopus.webservice.job;

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Matchers.any;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.util.Sandbox;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Before;
import org.junit.Test;

public class SandboxedJobTest {
    Sandbox sandbox;
    Job ojob;
    URI callback;
    HttpClient httpClient;
    JobStatus status;
    int pollIterations;
    SandboxedJob job;

    @Before
    public void setUp() throws URISyntaxException {
        sandbox = mock(Sandbox.class);
        ojob = mock(Job.class);
        callback = new URI("http://localhost/job/status");
        httpClient = mock(HttpClient.class);
        status = new JobStatusImplementation(ojob, "DONE", 0, null, true, null);
        pollIterations = 10;
        job = new SandboxedJob(sandbox, ojob, callback, httpClient, status, pollIterations);
    }

    @Test
    public void testSandboxedJob_Default() {
        SandboxedJob sjob = new SandboxedJob(sandbox, ojob, callback, httpClient);

        assertThat(sjob.getStatus()).isEqualTo(null);
        assertThat(sjob.getPollIterations()).isEqualTo(0);
    }

    @Test
    public void testGetSandbox() {
        assertThat(job.getSandbox()).isEqualTo(sandbox);
    }

    @Test
    public void testGetJob() {
        assertThat(job.getJob()).isEqualTo(ojob);
    }

    @Test
    public void testGetCallback() throws URISyntaxException {
        URI expected = new URI("http://localhost/job/status");
        assertThat(job.getCallback()).isEqualTo(expected);
    }

    @Test
    public void testGetHttpClient() {
        assertThat(job.getHttpClient()).isEqualTo(httpClient);
    }

    @Test
    public void testGetStatus() {
        assertThat(job.getStatus()).isEqualTo(status);
    }

    @Test
    public void testGetPollIterations() {
        assertThat(job.getPollIterations()).isEqualTo(10);
    }

    @Test
    public void testSetPollIterations() {
        job.setPollIterations(25);

        assertThat(job.getPollIterations()).isEqualTo(25);
    }

    @Test
    public void testIncrPollIterations() {
        job.incrPollIterations();

        assertThat(job.getPollIterations()).isEqualTo(11);
    }

    @Test
    public void testSetStatus_ChangedWithCallback_HttpClientExecute() throws UnsupportedEncodingException, ClientProtocolException, IOException {
        JobStatus rstatus = new JobStatusImplementation(ojob, "RUNNING", null, null, false, null);
        pollIterations = 10;
        job = new SandboxedJob(sandbox, ojob, callback, httpClient, rstatus, pollIterations);

        job.setStatus(this.status);

        assertThat(job.getStatus()).isEqualTo(this.status);
        verify(httpClient).execute(any(HttpUriRequest.class));
    }

    @Test
    public void testSetStatus_UnChangedWithCallback_NoHttpClientExecute() throws UnsupportedEncodingException, ClientProtocolException, IOException {
        pollIterations = 10;
        job = new SandboxedJob(sandbox, ojob, callback, httpClient, status, pollIterations);

        job.setStatus(this.status);

        assertThat(job.getStatus()).isEqualTo(this.status);
        verifyNoMoreInteractions(httpClient);
    }

    @Test
    public void testCleanSandbox() throws URISyntaxException, OctopusIOException {
        job.cleanSandbox();

        verify(sandbox).download();
        verify(sandbox).delete();
    }

}
