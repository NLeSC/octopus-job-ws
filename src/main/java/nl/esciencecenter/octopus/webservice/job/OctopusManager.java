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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.exceptions.NoSuchJobException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
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
    private final Map<String, SandboxedJob> jobs;
    private final JobsPoller poller;
    private ScheduledExecutorService executor;

    /**
     * Sets preferences in GAT context and initializes a broker.
     *
     * @param configuration
     * @throws URISyntaxException
     * @throws OctopusException
     * @throws OctopusIOException
     */
    public OctopusManager(OctopusConfiguration configuration) throws URISyntaxException, OctopusException, OctopusIOException {
        this.configuration = configuration;
        octopus = OctopusFactory.newOctopus(configuration.getPreferences());
        URI schedulerURI = configuration.getScheduler();
        Credential credential = configuration.getCredential();
        // TODO prompt user for password/passphrases
        scheduler = octopus.jobs().newScheduler(schedulerURI, credential, null);
        jobs = new ConcurrentHashMap<String, SandboxedJob>();
        executor = Executors.newSingleThreadScheduledExecutor();
        PollConfiguration pollConf = configuration.getPollConfiguration();
        poller = new JobsPoller(jobs, pollConf, octopus);
    }

    protected OctopusManager(OctopusConfiguration configuration, Octopus octopus, Scheduler scheduler,
            Map<String, SandboxedJob> jobs, JobsPoller poller, ScheduledExecutorService executor) {
        super();
        this.configuration = configuration;
        this.octopus = octopus;
        this.scheduler = scheduler;
        this.jobs = jobs;
        this.poller = poller;
        this.executor = executor;
    }

    /**
     * Starts the job poller.
     */
    public void start() {
        long interval = configuration.getPollConfiguration().getInterval();
        executor.scheduleAtFixedRate(poller, 0, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * Terminates any running Octopus processes and stops the job poller.
     * @throws InterruptedException
     * @throws OctopusException
     * @throws OctopusIOException
     */
    public void stop() throws InterruptedException, OctopusIOException, OctopusException {
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
     * @throws OctopusIOException
     * @throws OctopusException
     * @throws URISyntaxException
     */
    public SandboxedJob submitJob(JobSubmitRequest request, HttpClient httpClient) throws OctopusIOException, OctopusException,
            URISyntaxException {
        Credential credential = configuration.getCredential();
        // filesystems cant have path in them so strip eg. file:///tmp to file:///
        URI s = configuration.getSandboxRoot();
        URI sandboxURI = new URI(s.getScheme(), s.getUserInfo(), s.getHost(), s.getPort(), "/", s.getQuery(), s.getFragment());
        //create sandbox
        FileSystem sandboxFS = octopus.files().newFileSystem(sandboxURI, credential, null);
        String sandboxRoot = configuration.getSandboxRoot().getPath();
        AbsolutePath sandboxRootPath = octopus.files().newPath(sandboxFS, new RelativePath(sandboxRoot));
        Sandbox sandbox = request.toSandbox(octopus, sandboxRootPath, null);

        // create job description
        JobDescription description = request.toJobDescription();
        description.setQueueName(configuration.getQueue());
        description.setWorkingDirectory(sandbox.getPath().getPath());
        long cancelTimeout = configuration.getPollConfiguration().getCancelTimeout();
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
                long timeout = configuration.getPollConfiguration().getInterval();
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
