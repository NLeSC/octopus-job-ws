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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.util.CopyOption;
import nl.esciencecenter.octopus.util.Sandbox;
import nl.esciencecenter.octopus.webservice.resources.JobResource;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

/**
 * Job representation.
 *
 * @author verhoes
 *
 */
public class SandboxedJob {
    protected final static Logger logger = LoggerFactory.getLogger(SandboxedJob.class);

    private final JobSubmitRequest request;
    private final Sandbox sandbox;
    private final Job job;
    private final HttpClient httpClient;
    private JobStatus status = null;
    private int pollIterations = 0;

    public SandboxedJob(Sandbox sandbox, Job job, JobSubmitRequest request, HttpClient httpClient) {
        super();
        this.sandbox = sandbox;
        this.job = job;
        this.request = request;
        this.httpClient = httpClient;
    }

    public SandboxedJob(Sandbox sandbox, Job job, JobSubmitRequest request, HttpClient httpClient, JobStatus status, int pollIterations) {
        super();
        this.sandbox = sandbox;
        this.job = job;
        this.request = request;
        this.httpClient = httpClient;
        this.status = status;
        this.pollIterations = pollIterations;
    }

    public String getIdentifier() {
        return job.getIdentifier();
    }

    @JsonIgnore
    public Sandbox getSandbox() {
        return sandbox;
    }

    @JsonIgnore
    public Job getJob() {
        return job;
    }

    public JobSubmitRequest getRequest() {
        return request;
    }

    @JsonIgnore
    public HttpClient getHttpClient() {
        return httpClient;
    }

    @JsonIgnore
    public JobStatus getStatus() {
        return status;
    }

    @JsonProperty("status")
    public JobStatusResponse getStatusResponse() {
        return new JobStatusResponse(status);
    }

    @JsonIgnore
    public int getPollIterations() {
        return pollIterations;
    }

    public void setPollIterations(int polliterations) {
        this.pollIterations = polliterations;
    }

    /**
     * Increase poll iteration
     */
    public void incrPollIterations() {
        this.pollIterations++;
    }

    /**
     * Sets status.
     * If callback is set then sends PUT request with status.getState() to callback URL.
     *
     * @param status
     * @throws UnsupportedEncodingException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void setStatus(JobStatus status) throws UnsupportedEncodingException, ClientProtocolException, IOException {
        if (!Objects.equal(this.status, status)) {
            this.status = status;
            putState2Callback(status.getState());
        }
    }

    private void putState2Callback(String state) throws UnsupportedEncodingException, IOException, ClientProtocolException {
        URI callback = request.status_callback_url;
        if (callback != null) {
            HttpPut put = new HttpPut(callback);
            put.setEntity(new StringEntity(state));
            httpClient.execute(put);
        }
    }

    /**
     * Downloads sandbox and delete it's contents
     *
     * @throws OctopusIOException
     * @throws UnsupportedOperationException
     */
    public void cleanSandbox() throws OctopusIOException, UnsupportedOperationException {
        sandbox.download(CopyOption.REPLACE_EXISTING);
        sandbox.delete();
    }

    public URI getUrl() {
        return UriBuilder.fromResource(JobResource.class).build(job.getIdentifier());
    }
}
