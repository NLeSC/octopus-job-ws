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
import java.util.UUID;

import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.util.Sandbox;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

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
     * If status has changed and callback is set then sends PUT request with {@link JobStatusResponse JobStatusResponse} as JSON to callback URL.
     *
     * @param status
     * @throws JsonProcessingException when job status can not be converted to JSON.
     * @throws IOException when callback fails.
     */
    public void setStatus(JobStatus status) throws IOException {
        if (!status.equals(this.status)) {
            this.status = status;
            putState2Callback();
        }
    }

    private void putState2Callback() throws IOException {
        if (request != null && request.status_callback_url != null) {
            String body = getStatusResponse().toJson();
            HttpPut put = new HttpPut(request.status_callback_url);
            HttpEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
            put.setEntity(entity);
            httpClient.execute(put);
        }
    }

    /**
     * Downloads sandbox and delete it's contents.
     *
     * @throws OctopusIOException
     * @throws UnsupportedOperationException
     */
    public void cleanSandbox() throws OctopusIOException, UnsupportedOperationException {
        sandbox.download(CopyOption.REPLACE);
        sandbox.delete();
    }

    /**
     *
     * @return Universally unique identifier of job
     */
    @JsonIgnore
    public String getIdentifier() {
        UUID uuid = job.getUUID();
        return uuid.toString();
    }
}
