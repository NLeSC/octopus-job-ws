package nl.esciencecenter.octopus.webservice.job;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.util.Sandbox;
import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;

import org.apache.http.client.HttpClient;

import com.yammer.dropwizard.lifecycle.Managed;

/**
 * JavaGAT manager.
 *
 * @author verhoes
 *
 */
public class OctopusManager implements Managed {
    private final OctopusConfiguration configuration;
    private final Octopus octopus;
    private final Scheduler scheduler;
    private final Credential credential = null;
    private final Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();

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
        // copy over preferences from config to default GAT context
        Properties properties = new Properties();
        Set<String> keys = configuration.getPreferences().keySet();
        for (String key : keys) {
            String value = configuration.getPreferences().get(key).toString();
            properties.setProperty(key, value);
        }

        octopus = OctopusFactory.newOctopus(properties);
        URI schedulerURI = configuration.getScheduler();
        // TODO parse credentials from config of prompt user for password/passphrases
        scheduler = octopus.jobs().newScheduler(schedulerURI, credential, null);
    }

    protected OctopusManager(OctopusConfiguration configuration, Octopus octopus, Scheduler scheduler) {
        super();
        this.configuration = configuration;
        this.octopus = octopus;
        this.scheduler = scheduler;
    }

    public void start() throws Exception {
    }

    /**
     * Terminates any running Octopus processes.
     */
    public void stop() throws Exception {
        // TODO should I call OctopusFactory.endAll() or the octopus.end()
        octopus.end();
    }

    public Job submitJob(JobSubmitRequest request, HttpClient httpClient) throws OctopusIOException, OctopusException,
            URISyntaxException {
        //create sandbox
        FileSystem sandboxFS = octopus.files().newFileSystem(configuration.getSandboxRoot(), credential, null);
        String sandboxRoot = configuration.getSandboxRoot().getPath();
        AbsolutePath sandboxRootPath =
                octopus.files().newPath(sandboxFS, new RelativePath(sandboxRoot));
        Sandbox sandbox = request.toSandbox(octopus, sandboxRootPath, null);

        // create job description
        JobDescription description = request.toJobDescription(octopus);
        description.setQueueName(configuration.getQueue());
        description.setWorkingDirectory(sandbox.getPath().getPath());

        // stage input files and submit job
        sandbox.upload();
        Job job = octopus.jobs().submitJob(scheduler, description);

        // store job in jobs map
        URI callback = request.status_callback_url;
        SandboxedJob sjob = new SandboxedJob(sandbox, job, callback, httpClient);
        jobs.put(job.getIdentifier(), sjob);

        // poll job status
        JobPoller jp = new JobPoller(sjob, configuration.getPollConfiguration(), octopus);
        jp.start();

        return job;
    }

    public JobStatus stateOfJob(String jobIdentifier) throws OctopusIOException, OctopusException {
        SandboxedJob job;
        if ((job = jobs.get(jobIdentifier)) != null) {
            return octopus.jobs().getJobStatus(job.getJob());
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
