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
package nl.esciencecenter.octopus.webservice.job;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusException;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.NoSuchJobException;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.util.Sandbox;
import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;
import nl.esciencecenter.octopus.webservice.api.SandboxedJob;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.dropwizard.lifecycle.Managed;

/**
 * Octopus manager.
 *
 * Responsible for submitting jobs, polling their status and cleaning jobs up.
 *
 * @author verhoes
 *
 */
public class OctopusManager implements Managed {
    protected static final Logger LOGGER = LoggerFactory.getLogger(OctopusManager.class);

    private final OctopusConfiguration configuration;
    private final Octopus octopus;
    private final Scheduler scheduler;
    private final Path sandboxRootPath;
    private final Map<String, SandboxedJob> jobs;
    private final JobsPoller poller;
    private ScheduledExecutorService executor;


    /**
     * Sets preferences in GAT context and initializes a broker.
     *
     * @param configuration
     * @throws URISyntaxException
     * @throws OctopusException
     */
    public OctopusManager(OctopusConfiguration configuration) throws URISyntaxException, OctopusException {
        this.configuration = configuration;

        octopus = OctopusFactory.newOctopus(configuration.getPreferences());

        scheduler = newScheduler();

        sandboxRootPath = newSandboxRootPath();

        jobs = new ConcurrentHashMap<String, SandboxedJob>();

        executor = Executors.newSingleThreadScheduledExecutor();
        PollConfiguration pollConf = configuration.getPoll();

        poller = new JobsPoller(jobs, pollConf, octopus);
    }

    /**
     * @return Path
     * @throws OctopusException
     */
    protected Path newSandboxRootPath() throws OctopusException {
        Credential credential = null;
        SandboxConfiguration sandboxConf = this.configuration.getSandbox();
        Files filesEngine = octopus.files();
        FileSystem sandboxFS = filesEngine.newFileSystem(sandboxConf.getScheme(), sandboxConf.getLocation(), credential, sandboxConf.getProperties());
        return filesEngine.newPath(sandboxFS, sandboxConf.getPath());
    }

    /**
     * @return Scheduler
     * @throws OctopusException
     */
    protected Scheduler newScheduler() throws OctopusException {
        Credential credential = null;
        SchedulerConfiguration schedulerConf = configuration.getScheduler();
        // TODO prompt user for password/passphrases
        return octopus.jobs().newScheduler(schedulerConf.getScheme(), schedulerConf.getLocation(), credential, schedulerConf.getProperties());
    }

    protected OctopusManager(OctopusConfiguration configuration, Octopus octopus, Scheduler scheduler, Path sandboxRootPath,
            Map<String, SandboxedJob> jobs, JobsPoller poller, ScheduledExecutorService executor) {
        super();
        this.configuration = configuration;
        this.octopus = octopus;
        this.scheduler = scheduler;
        this.sandboxRootPath = sandboxRootPath;
        this.jobs = jobs;
        this.poller = poller;
        this.executor = executor;
    }

    /**
     * Starts the job poller.
     */
    public void start() {
        long interval = configuration.getPoll().getInterval();
        executor.scheduleAtFixedRate(poller, 0, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * Terminates any running Octopus processes and stops the job poller.
     *
     * @throws InterruptedException
     * @throws OctopusException
     * @throws OctopusIOException
     */
    public void stop() throws InterruptedException, OctopusException {
        executor.shutdown();
        // JobsPoller can be in middle of fetching job statuses so give it 1 minute to finish before interrupting it
        executor.awaitTermination(1, TimeUnit.MINUTES);
        poller.stop();
        OctopusFactory.endOctopus(octopus);
    }

    /**
     * Submit a job request.
     *
     * @param request
     *            The job request
     * @param httpClient
     *            http client used to reporting status to job callback.
     * @return SandboxedJob job
     *
     * @throws OctopusException
     * @throws URISyntaxException
     */
    public SandboxedJob submitJob(JobSubmitRequest request, HttpClient httpClient) throws OctopusException,
            URISyntaxException {
        Sandbox sandbox = request.toSandbox(octopus.files(), sandboxRootPath, null);

        // create job description
        JobDescription description = request.toJobDescription();
        description.setQueueName(configuration.getScheduler().getQueue());
        description.setWorkingDirectory(sandbox.getPath().getRelativePath().getAbsolutePath());
        long cancelTimeout = configuration.getPoll().getCancelTimeout();
        // CancelTimeout is in milliseconds and MaxTime must be in minutes, so convert it
        int maxTime = (int) TimeUnit.MINUTES.convert(cancelTimeout, TimeUnit.MILLISECONDS);
        description.setMaxTime(maxTime);

        // stage input files
        sandbox.upload();

        // submit job
        Job job = octopus.jobs().submitJob(scheduler, description);

        // store job in jobs map
        SandboxedJob sjob = new SandboxedJob(sandbox, job, request, httpClient);
        jobs.put(sjob.getIdentifier(), sjob);

        // JobsPoller will poll job status and download sandbox when job is done.

        return sjob;
    }

    /**
     * Cancel job, cancels pending job and kills running job.
     *
     * Stores canceled state.
     *
     * If job is done then nothing happens.
     *
     * @param jobIdentifier
     *            The identifier of the job.
     *
     * @throws NoSuchJobException
     *             When job with jobIdentifier could not be found.
     * @throws OctopusException
     * @throws IOException
     */
    public void cancelJob(String jobIdentifier) throws OctopusException, IOException {
        SandboxedJob job = getJob(jobIdentifier);
        // no need to cancel completed jobs
        JobStatus status = job.getStatus();
        if (status == null || !status.isDone()) {
            JobStatus canceledStatus = octopus.jobs().cancelJob(job.getJob());
            if (!canceledStatus.isDone()) {
                long timeout = configuration.getPoll().getInterval();
                canceledStatus = octopus.jobs().waitUntilDone(job.getJob(), timeout);
            }
            job.cleanSandbox();
            job.setStatus(canceledStatus);
        }
    }

    /**
     * Get list of submitted jobs.
     *
     * @return List of submitted jobs.
     */
    public Collection<SandboxedJob> getJobs() {
        return jobs.values();
    }

    /**
     * Get a job
     *
     * @param jobIdentifier
     * @return the job
     * @throws NoSuchJobException
     */
    public SandboxedJob getJob(String jobIdentifier) throws NoSuchJobException {
        SandboxedJob job = jobs.get(jobIdentifier);
        if (job != null) {
            return job;
        } else {
            throw new NoSuchJobException("", "Job not found");
        }
    }
}
