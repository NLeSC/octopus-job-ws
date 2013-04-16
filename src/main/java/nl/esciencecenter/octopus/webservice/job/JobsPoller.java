package nl.esciencecenter.octopus.webservice.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobStatus;

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
        int interval = pollConfiguration.getInterval();
        int timeout = pollConfiguration.getTimeout();
        // maximum number of poll iterations
        int maxIterations = timeout / interval;

        while (!Thread.currentThread().isInterrupted()) {
            // convert SandboxedJob list to Job list
            List<Job> jjobs = new ArrayList<Job>();
            for (SandboxedJob job : jobs.values()) {
                // dont need to fetch status of jobs that are done
                if (!job.getStatus().isDone()) {
                    jjobs.add(job.getJob());
                }
            }

            // fetch statuses of all jobs
            JobStatus[] statuses = octopus.jobs().getJobStatuses(jjobs.toArray(new Job[0]));

            for (JobStatus status : statuses) {
                SandboxedJob job = jobs.get(status.getJob().getIdentifier());

                // when state changed then commit
                if (!status.equals(job.getStatus())) {
                    commitStatus(status, job);
                    if (status.isDone()) {
                        // download and delete sandbox
                        this.cleanSandbox(job);
                    }
                }

                // if job is has been running to long then kill it
                Boolean polledTooLong = (job.getPollIterations() > maxIterations);
                if (!status.isDone() && polledTooLong) {
                    cancelJob(job);
                }
                job.incrPollIterations();
            }
            try {
                TimeUnit.SECONDS.sleep(timeout);
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
                return;
            }
        }
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
        this.cleanSandbox(job);
    }

}
