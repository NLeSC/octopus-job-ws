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
package nl.esciencecenter.osmium.api;


import java.io.IOException;

import nl.esciencecenter.osmium.callback.CallbackClient;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.util.Sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Job representation.
 *
 * @author verhoes
 *
 */
public class SandboxedJob {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SandboxedJob.class);

    private final JobSubmitRequest request;
    private final Sandbox sandbox;
    private final Job job;
    private final CallbackClient callbackClient;
    private JobStatus status = null;
    private int pollIterations = 0;

    public SandboxedJob() {
		super();
        this.sandbox = null;
        this.job = null;
        this.request = null;
        this.callbackClient = null;
	}

	public SandboxedJob(Sandbox sandbox, Job job, JobSubmitRequest request, CallbackClient callbackClient) {
        super();
        this.sandbox = sandbox;
        this.job = job;
        this.request = request;
        this.callbackClient = callbackClient;
    }

    public SandboxedJob(Sandbox sandbox, Job job, JobSubmitRequest request, CallbackClient callbackClient, JobStatus status,
            int pollIterations) {
        super();
        this.sandbox = sandbox;
        this.job = job;
        this.request = request;
        this.callbackClient = callbackClient;
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
    public CallbackClient getCallbackClient() {
        return callbackClient;
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
     * Sets status. If status has changed and callback is set then sends PUT request with {@link JobStatusResponse
     * JobStatusResponse} as JSON to callback URL.
     *
     * @param status
     *            new Job status
     * @throws IOException
     *             when callback fails.
     */
    public void setStatus(JobStatus status) throws IOException {
        if (!status.equals(this.status)) {
            this.status = status;
            putState2Callback();
        }
    }

    private void putState2Callback() throws IOException {
        if (request != null && request.status_callback_url != null) {
            callbackClient.putState(request.status_callback_url, getStatusResponse());
        }
    }

    /**
     * Deletes sandbox.
     *
     * @throws XenonException if deletion of sandbox fails
     */
    public void cleanSandbox() throws XenonException {
        sandbox.delete();
    }

    /**
     * Downloads sandbox contents.
     *
     * @throws XenonException if copying of poststaged files from sandbox to jobdir fails
     */
    public void downloadSandbox() throws XenonException {
        sandbox.download(CopyOption.REPLACE);
    }

    /**
     *
     * @return Unique identifier of job
     *
     */
    @JsonIgnore
    public String getIdentifier() {
        // Current adaptors return web safe identifiers.
        // If a new Xenon adaptor returns identifier which
        // is not web safe then the job can not be mapped to resource url.
        // eg. id is "slow/3456" then a GET /job/slow/3456 will
        // try to fetch job with id "slow" which will fail.
        return job.getIdentifier();
    }
}
