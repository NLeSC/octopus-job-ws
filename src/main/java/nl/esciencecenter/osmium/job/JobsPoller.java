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
package nl.esciencecenter.osmium.job;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.osmium.api.SandboxedJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Poll the status of jobs using pollConfiguration and xenon.
 *
 * Only polls status of jobs that are not DONE.
 *
 * @author verhoes
 *
 */
public class JobsPoller implements Runnable {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JobsPoller.class);

    private final Map<String, SandboxedJob> jobs;
    private final PollConfiguration pollConfiguration;
    private final Xenon xenon;

    public JobsPoller(Map<String, SandboxedJob> jobs, PollConfiguration pollConfiguration, Xenon xenon) {
        super();
        this.jobs = jobs;
        this.pollConfiguration = pollConfiguration;
        this.xenon = xenon;
    }

    public void run() {
        LOGGER.debug("Polling for jobs statuses");
        Job[] jobarray = getJobsNeedingStatusUpdate();

        // fetch statuses of all jobs
        Jobs jobsEngine = xenon.jobs();
        JobStatus[] statuses = jobsEngine.getJobStatuses(jobarray);

        processStatusUpdate(statuses);
    }

    /**
     * @param statuses
     */
    private void processStatusUpdate(JobStatus[] statuses) {
        if (statuses != null) {
            for (JobStatus status : statuses) {
                SandboxedJob job = jobs.get(status.getJob().getIdentifier());

                // when state changed then commit
                if (job.getStatus() == null || !status.getState().equals(job.getStatus().getState())) {
                    if (status.isDone()) {
                        LOGGER.debug("Job is done: " + job.getIdentifier());
                        downloadSandbox(job);
                        cleanSandbox(job);
                    }
                    LOGGER.debug("Status changed of " + job.getIdentifier() + " to " + status.getState());
                    commitStatus(status, job);
                }
            }
        }
    }

    /**
     * @return
     */
    private Job[] getJobsNeedingStatusUpdate() {
        long interval = pollConfiguration.getInterval();
        long timeout = pollConfiguration.getCancelTimeout();
        // maximum number of poll iterations
        long maxIterations = timeout / interval;
        // convert SandboxedJob list to Job list
        List<Job> jjobs = new ArrayList<Job>(jobs.size());
        for (SandboxedJob job : jobs.values()) {
            Boolean jobIsDone = false;
            if (job.getStatus() != null) {
                jobIsDone = job.getStatus().isDone();
            }

            job.incrPollIterations();
            Boolean polledTooLong = job.getPollIterations() > maxIterations;
            if (job.getPollIterations() > pollConfiguration.getDeleteTimeout()) {
                LOGGER.debug("Deleting job");
                // delete timeout reached -> get rid of job completely
                cancelJob(job);
                cleanSandbox(job);
                deleteJob(job);
            } else if (!jobIsDone && polledTooLong) {
                LOGGER.debug("Canceling job");
                // cancel timeout reached -> remove job from scheduler
                cancelJob(job);
                cleanSandbox(job);
            } else if (!jobIsDone) {
                jjobs.add(job.getJob());
            }
            // else dont need to fetch status of jobs that are done
        }

        LOGGER.trace("Fetching job statuses of " + jjobs.toString());

        return jjobs.toArray(new Job[jobs.size()]);
    }

    private void deleteJob(SandboxedJob job) {
        jobs.remove(job.getIdentifier());
    }

    protected void commitStatus(JobStatus status, SandboxedJob job) {
        try {
            job.setStatus(status);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected void downloadSandbox(SandboxedJob job) {
        try {
            job.downloadSandbox();
        } catch (XenonException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (UnsupportedOperationException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected void cleanSandbox(SandboxedJob job) {
        try {
            job.cleanSandbox();
        } catch (XenonException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (UnsupportedOperationException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected void cancelJob(SandboxedJob job) {
        LOGGER.debug("Cancelling job:" + job.getIdentifier());
        try {
            JobStatus status = xenon.jobs().cancelJob(job.getJob());
            job.setStatus(status);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug("Cancelled job:" + job.getIdentifier());
    }

    /**
     * Cancels all not done jobs and cleans sandboxes of those jobs.
     * @throws XenonException if job was not cancelled successfully
     *
     */
    public void stop() throws XenonException {
        LOGGER.debug("Cancelling jobs and cleaning their sandboxes");
        for (SandboxedJob job : jobs.values()) {
            if (!job.getStatus().isDone()) {
                xenon.jobs().cancelJob(job.getJob());
                job.cleanSandbox();
            }
        }
    }
}
