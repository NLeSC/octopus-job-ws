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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nl.esciencecenter.osmium.api.JobSubmitRequest;
import nl.esciencecenter.osmium.api.SandboxedJob;
import nl.esciencecenter.osmium.callback.CallbackClient;
import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.NoSuchJobException;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.util.Sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.lifecycle.Managed;

/**
 * Xenon manager.
 *
 * Responsible for submitting jobs, polling their status and cleaning jobs up.
 *
 * @author verhoes
 *
 */
public class XenonManager implements Managed {
    protected static final Logger LOGGER = LoggerFactory.getLogger(XenonManager.class);

    private final XenonConfiguration configuration;
    private final Xenon xenon;
    private final Map<String,Scheduler> schedulers;
    private final Map<String, Path> sandboxRootPaths;
    /**
     * Map of started jobs.
     * A new job is added for each job submitted. The variable is shared and
     * concurrently modified by a JobsPoller instance.
     */
    private final Map<String, SandboxedJob> jobs;
    private final JobsPoller poller;
    private final ScheduledExecutorService executor;


    /**
     * Sets preferences in GAT context and initializes a broker.
     *
     * @param configuration Configuration for Xenon
     * @throws XenonException if Xenon could not be configured
     */
    public XenonManager(XenonConfiguration configuration) throws XenonException {
        this.configuration = configuration;

        xenon = XenonFactory.newXenon(configuration.getPreferences());

        schedulers = newSchedulers();

        sandboxRootPaths = newSandboxRootPaths();

        jobs = new ConcurrentHashMap<String, SandboxedJob>(10);

        executor = Executors.newSingleThreadScheduledExecutor();
        PollConfiguration pollConf = configuration.getPoll();

        poller = new JobsPoller(jobs, pollConf, xenon);
    }

    /**
     * @return Path
     * @throws XenonException If the creation of the FileSystem failed or an I/O error occured.
     */
    private Map<String, Path> newSandboxRootPaths() throws XenonException {
        Credential credential = null;
        Map<String, LauncherConfiguration> launcherConfs = configuration.getLaunchers();
        Map<String, Path> sandboxMap = new HashMap<String, Path>(launcherConfs.size()*4/3);

        for (Map.Entry<String, LauncherConfiguration> entry : launcherConfs.entrySet()) {

            SandboxConfiguration sandboxConf = entry.getValue().getSandbox();
            Files filesEngine = xenon.files();
            FileSystem sandboxFS = filesEngine.newFileSystem(sandboxConf.getScheme(), sandboxConf.getLocation(), credential, sandboxConf.getProperties());
            sandboxMap.put(entry.getKey(), filesEngine.newPath(sandboxFS, sandboxConf.getPath()));
        }
        return sandboxMap;
    }

    /**
     * @return Scheduler
     * @throws XenonException If the creation of the Scheduler failed
     */
    private Map<String,Scheduler> newSchedulers() throws XenonException {
        Credential credential = null;
        Map<String, LauncherConfiguration> launcherConfs = configuration.getLaunchers();
        Map<String, Scheduler> schedulerMap = new HashMap<String, Scheduler>(launcherConfs.size()*4/3);
        for (Map.Entry<String, LauncherConfiguration> entry : launcherConfs.entrySet()) {
            SchedulerConfiguration schedulerConf = entry.getValue().getScheduler();
            // TODO prompt user for password/passphrases
            schedulerMap.put(
                entry.getKey(),
                xenon.jobs().newScheduler(schedulerConf.getScheme(), schedulerConf.getLocation(), credential, schedulerConf.getProperties())
            );
        }
        return schedulerMap;
    }

    protected XenonManager(XenonConfiguration configuration, Xenon xenon, Map<String,Scheduler> scheduler, Map<String,Path> sandboxRootPaths,
            Map<String, SandboxedJob> jobs, JobsPoller poller, ScheduledExecutorService executor) {
        super();
        this.configuration = configuration;
        this.xenon = xenon;
        this.schedulers = scheduler;
        this.sandboxRootPaths = sandboxRootPaths;
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
     * Terminates any running Xenon processes and stops the job poller.
     *
     * @throws InterruptedException If waiting for job to complete failed
     * @throws XenonException If Xenon is unable to stop
     */
    public void stop() throws InterruptedException, XenonException {
        executor.shutdown();
        // JobsPoller can be in middle of fetching job statuses so give it 1 minute to finish before interrupting it
        executor.awaitTermination(1, TimeUnit.MINUTES);
        poller.stop();
        XenonFactory.endXenon(xenon);
    }

    /**
     * Submit a job request.
     *
     * @param request
     *            The job request
     * @param callbackClient
     *            callback client used to reporting status to job callback.
     * @return SandboxedJob job
     *
     * @throws XenonException If staging file or submit job failed
     */
    public SandboxedJob submitJob(JobSubmitRequest request, CallbackClient callbackClient) throws XenonException {
        if (request.launcher == null) {
            request.launcher = configuration.getDefaultLauncher();
        }
        if (!schedulers.containsKey(request.launcher)) {
            throw new XenonException(null, "Launcher '" + request.launcher + "' not configured. Choose from " + Objects.toString(schedulers.keySet()));
        }

        Sandbox sandbox = request.toSandbox(xenon.files(), sandboxRootPaths.get(request.launcher), null);

        // create job description
        JobDescription description = request.toJobDescription();
        description.setQueueName(configuration.getLaunchers().get(request.launcher).getScheduler().getQueue());
        description.setWorkingDirectory(sandbox.getPath().getRelativePath().getAbsolutePath());
        long cancelTimeout = configuration.getPoll().getCancelTimeout();
        // CancelTimeout is in milliseconds and MaxTime must be in minutes, so convert it
        int maxTime = (int) TimeUnit.MINUTES.convert(cancelTimeout, TimeUnit.MILLISECONDS);
        description.setMaxTime(maxTime);

        // stage input files
        sandbox.upload();

        // submit job
        Job job = xenon.jobs().submitJob(schedulers.get(request.launcher), description);

        // store job in jobs map
        SandboxedJob sjob = new SandboxedJob(sandbox, job, request, callbackClient);
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
     * @throws XenonException if job cancelation failed
     * @throws IOException if job status callback failed
     */
    public void cancelJob(String jobIdentifier) throws XenonException, IOException {
        SandboxedJob job = getJob(jobIdentifier);
        // no need to cancel completed jobs
        JobStatus status = job.getStatus();
        if (status == null || !status.isDone()) {
            JobStatus canceledStatus = xenon.jobs().cancelJob(job.getJob());
            if (!canceledStatus.isDone()) {
                long timeout = configuration.getPoll().getInterval();
                canceledStatus = xenon.jobs().waitUntilDone(job.getJob(), timeout);
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
     * @param jobIdentifier Identifier of job
     * @return the job
     * @throws NoSuchJobException When job is not found
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
