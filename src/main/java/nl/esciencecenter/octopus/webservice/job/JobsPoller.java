package nl.esciencecenter.octopus.webservice.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        long timeout = pollConfiguration.getCancelTimeout();
        while (!Thread.currentThread().isInterrupted()) {
            poll();
            try {
                TimeUnit.MILLISECONDS.sleep(timeout);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void poll() {
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
            Boolean polledTooLong = (job.getPollIterations() > maxIterations);
            if (job.getPollIterations() > pollConfiguration.getDeleteTimeout()) {
                // delete timeout reached -> get rid of job completely
                cancelJob(job);
                cleanSandbox(job);
                deleteJob(job);
            } else if (!jobIsDone && polledTooLong) {
                // cancel timeout reached -> remove job from scheduler
                cancelJob(job);
                cleanSandbox(job);
            } else if (!jobIsDone) {
                jjobs.add(job.getJob());
            } else {
                // dont need to fetch status of jobs that are done
            }
        }

        // fetch statuses of all jobs
        Job[] jobarray = jjobs.toArray(new Job[0]);
        Jobs jobsEngine = octopus.jobs();
        JobStatus[] statuses = jobsEngine.getJobStatuses(jobarray);

        if (statuses != null) {
            for (JobStatus status : statuses) {
                SandboxedJob job = jobs.get(status.getJob().getIdentifier());

                // when state changed then commit
                if (!status.equals(job.getStatus())) {
                    commitStatus(status, job);
                    if (status.isDone()) {
                        this.cleanSandbox(job);
                    }
                }
            }
        }
    }

    private void deleteJob(SandboxedJob job) {
        jobs.remove(job.getJob().getIdentifier());
    }

    /**
     * @param status
     * @param job
     */
    public void commitStatus(JobStatus status, SandboxedJob job) {
        try {
            job.setStatus(status);
        } catch (Exception e) {
            logger.info(e.toString());
        }
    }

    public void cleanSandbox(SandboxedJob job) {
        try {
            job.getSandbox().download();
            job.getSandbox().delete();
        } catch (Exception e) {
            logger.info(e.toString());
        }
    }

    public void cancelJob(SandboxedJob job) {
        try {
            octopus.jobs().cancelJob(job.getJob());
        } catch (Exception e) {
            logger.info(e.toString());
        }
    }

}
