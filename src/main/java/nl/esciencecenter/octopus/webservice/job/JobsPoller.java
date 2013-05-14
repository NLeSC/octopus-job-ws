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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.webservice.api.SandboxedJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Poll the status of jobs using pollConfiguration and octopus.
 *
 * Only polls status of jobs that are not DONE.
 *
 * @author verhoes
 *
 */
public class JobsPoller implements Runnable {
    protected final static Logger logger = LoggerFactory.getLogger(JobsPoller.class);

    private final Map<String, SandboxedJob> jobs;
    private final PollConfiguration pollConfiguration;
    private final Octopus octopus;

    public JobsPoller(Map<String, SandboxedJob> jobs, PollConfiguration pollConfiguration, Octopus octopus) {
        super();
        this.jobs = jobs;
        this.pollConfiguration = pollConfiguration;
        this.octopus = octopus;
    }

    public void run() {
        logger.debug("Polling for jobs statuses");
        long interval = pollConfiguration.getInterval();
        long timeout = pollConfiguration.getCancelTimeout();
        // maximum number of poll iterations
        long maxIterations = timeout / interval;
        // convert SandboxedJob list to Job list
        List<Job> jjobs = new ArrayList<Job>();
        for (SandboxedJob job : jobs.values()) {
            Boolean jobIsDone = false;
            if (job.getStatus() != null) {
                jobIsDone = job.getStatus().isDone();
            }

            job.incrPollIterations();
            Boolean polledTooLong = job.getPollIterations() > maxIterations;
            if (job.getPollIterations() > pollConfiguration.getDeleteTimeout()) {
                logger.debug("Deleting job");
                // delete timeout reached -> get rid of job completely
                cancelJob(job);
                cleanSandbox(job);
                deleteJob(job);
            } else if (!jobIsDone && polledTooLong) {
                logger.debug("Canceling job");
                // cancel timeout reached -> remove job from scheduler
                cancelJob(job);
                cleanSandbox(job);
            } else if (!jobIsDone) {
                jjobs.add(job.getJob());
            }
            // else dont need to fetch status of jobs that are done
        }

        logger.debug("Fetching job statuses");

        // fetch statuses of all jobs
        Job[] jobarray = jjobs.toArray(new Job[0]);
        Jobs jobsEngine = octopus.jobs();
        JobStatus[] statuses = jobsEngine.getJobStatuses(jobarray);

        if (statuses != null) {
            for (JobStatus status : statuses) {
                SandboxedJob job = jobs.get(status.getJob().getIdentifier());

                // when state changed then commit
                if (job.getStatus() == null || !status.getState().equals(job.getStatus().getState())) {
                    logger.debug("Status changed");
                    commitStatus(status, job);
                    if (status.isDone()) {
                        logger.debug("Emptying sandbox");
                        this.cleanSandbox(job);
                    }
                }
            }
        }
    }

    private void deleteJob(SandboxedJob job) {
        jobs.remove(job.getJob().getIdentifier());
    }

    protected void commitStatus(JobStatus status, SandboxedJob job) {
        try {
            job.setStatus(status);
        } catch (Exception e) {
            logger.info(e.toString());
        }
    }

    protected void cleanSandbox(SandboxedJob job) {
        try {
            job.cleanSandbox();
        } catch (Exception e) {
            logger.info(e.toString());
        }
    }

    protected void cancelJob(SandboxedJob job) {
        try {
            octopus.jobs().cancelJob(job.getJob());
        } catch (Exception e) {
            logger.info(e.toString());
        }
    }
}
