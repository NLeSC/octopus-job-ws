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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.util.Sandbox;
import nl.esciencecenter.octopus.webservice.api.JobStatusResponse;
import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.dropwizard.lifecycle.Managed;

/**
 * JavaGAT manager.
 *
 * @author verhoes
 *
 */
public class OctopusManager implements Managed {
    protected final static Logger logger = LoggerFactory.getLogger(OctopusManager.class);

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
        Properties props = configuration.getPreferencesAsProperties();
        octopus = OctopusFactory.newOctopus(props);
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

    public void start() throws Exception {
        long interval = configuration.getPollConfiguration().getInterval();
        executor.scheduleAtFixedRate(poller, 0, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * Terminates any running Octopus processes.
     */
    public void stop() throws Exception {
        // TODO should I call OctopusFactory.endAll() or the octopus.end()
        octopus.end();
        executor.shutdown();
        // JobsPoller can be in middle of fetching job statuses so give it 1 minute to finish before interrupting it
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    public Job submitJob(JobSubmitRequest request, HttpClient httpClient) throws OctopusIOException, OctopusException,
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
        JobDescription description = request.toJobDescription(octopus);
        description.setQueueName(configuration.getQueue());
        description.setWorkingDirectory(sandbox.getPath().getPath());

        // stage input files
        sandbox.upload();

        // submit job
        Job job = octopus.jobs().submitJob(scheduler, description);

        // store job in jobs map
        URI callback = request.status_callback_url;
        SandboxedJob sjob = new SandboxedJob(sandbox, job, callback, httpClient);
        jobs.put(job.getIdentifier(), sjob);

        // JobsPoller will poll job status and download sandbox when job is done.

        return job;
    }

    public JobStatusResponse stateOfJob(String jobIdentifier) throws OctopusIOException, OctopusException {
        SandboxedJob job;
        if ((job = jobs.get(jobIdentifier)) != null) {
            return new JobStatusResponse(octopus.jobs().getJobStatus(job.getJob()));
        } else {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    public void cancelJob(String jobIdentifier) throws OctopusIOException, OctopusException {
        SandboxedJob job;
        if ((job = jobs.get(jobIdentifier)) != null) {
            octopus.jobs().cancelJob(job.getJob());
        } else {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }
}
