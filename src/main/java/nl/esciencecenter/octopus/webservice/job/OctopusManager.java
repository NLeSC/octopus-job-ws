package nl.esciencecenter.octopus.webservice.job;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.http.client.HttpClient;

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
import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;

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
            String value = (String) configuration.getPreferences().get(key);
            properties.setProperty(key, value);
        }

        octopus = OctopusFactory.newOctopus(properties);
        URI schedulerURI = new URI(configuration.getSchedulerURI());
        // TODO parse credentials from config of prompt user for password/passphrases
        scheduler = octopus.jobs().newScheduler(schedulerURI, credential, null);
    }

    public void start() throws Exception {
    }

    /**
     * Terminates any running JavaGAT processes.
     */
    public void stop() throws Exception {
        // TODO replace with end of octopus instance
        OctopusFactory.end();
    }

    public Octopus getOctopus() {
        return octopus;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public Job submitJob(JobSubmitRequest request, HttpClient httpClient) throws Exception {
        JobDescription description = new JobDescription();
        description.setExecutable(request.executable);
        description.setArguments(request.arguments);

        String sandbox_id = UUID.randomUUID().toString();
        FileSystem localrootfs = octopus.files().newFileSystem(new URI("file:///"), credential, null);
        FileSystem localjobdirfs = octopus.files().newFileSystem(new URI("file://"+request.jobdir), credential, null);
        FileSystem filesystem = octopus.files().newFileSystem(configuration.getSandboxRoot(), credential, null);
        RelativePath location = new RelativePath(System.getProperty("java.io.tmpdir") + "/" + sandbox_id);
        AbsolutePath root = octopus.files().newPath(filesystem, location);
        Sandbox sandbox = new Sandbox(octopus, root, sandbox_id);

        // Upload files in request to sandbox
        for (String prestage : request.prestaged) {
            AbsolutePath src;
            if (prestage.startsWith("/")) {
                src = octopus.files().newPath(localrootfs, new RelativePath(prestage));
            } else {
                src = octopus.files().newPath(localjobdirfs, new RelativePath(prestage));
            }
            sandbox.addUploadFile(src, src.getFileName());
        }
        // Download files from sandbox to request.jobdir
        for (String poststaged : request.poststaged) {
            AbsolutePath dest = octopus.files().newPath(localjobdirfs, new RelativePath(poststaged));
            sandbox.addDownloadFile(poststaged, dest);
        }

        sandbox.upload();

        Job job = octopus.jobs().submitJob(scheduler, description);

        URI callback = request.status_callback_url;

        JobPoller jp = new JobPoller(configuration.getStatePollTimeout(), configuration.getStatePollInterval(), octopus, job, sandbox, callback, httpClient);
        jp.start();

        return job;
    }
}
