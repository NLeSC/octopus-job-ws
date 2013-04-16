package nl.esciencecenter.octopus.webservice.api;

import java.net.URI;
import java.util.Arrays;

import javax.validation.constraints.NotNull;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.util.Sandbox;

import com.google.common.base.Objects;

/**
 * Request which can be converted to JobDescription which can be submitted using
 * JavaGAT.
 *
 * @author Stefan Verhoeven <s.verhoeven@esciencecenter.nl>
 *
 */
public class JobSubmitRequest {
    /**
     * Job directory where stderr/stdout/prestaged/poststaged file are relative
     * to and where job.state file is written. Must end with '/'
     */
    @NotNull
    public String jobdir;
    /**
     * Path to executable on execution host
     */
    @NotNull
    public String executable;
    /**
     * File name to write standard error to
     */
    public String stderr = "stderr.txt";
    /**
     * File name to write standard out to
     */
    public String stdout = "stdout.txt";
    /**
     * Arguments passed to executable
     */
    public String[] arguments = {};
    /**
     * List of filenames to copy from job directory to work directory before
     * executable is called. Work directory is created on the execution host.
     * Can be relative to job directory or absolute paths.
     */
    public String[] prestaged = {};
    /**
     * List of filenames to copy from work directory to job directory after
     * executable is called. Must be relative to job directory
     */
    public String[] poststaged = {};
    /**
     * The maximum walltime or cputime for a single execution of the executable.
     * The units is in minutes.
     */
    public long time_max = 0;
    /**
     * minimal required memory in MB
     */
    public int memory_min = 0;
    /**
     * maximum required memory in MB
     */
    public int memory_max = 0;
    /**
     * Url where changes of state are PUT to.
     */
    public URI status_callback_url;

    /**
     * Constructor
     *
     * @param jobdir
     * @param executable
     * @param arguments
     * @param prestaged
     * @param poststaged
     * @param stderr
     * @param stdout
     */
    public JobSubmitRequest(String jobdir, String executable,
            String[] arguments, String[] prestaged, String[] poststaged,
            String stderr, String stdout, URI status_callback_url) {
        super();
        this.jobdir = jobdir;
        this.executable = executable;
        this.arguments = arguments;
        this.prestaged = prestaged;
        this.poststaged = poststaged;
        this.stderr = stderr;
        this.stdout = stdout;
        this.status_callback_url = status_callback_url;
    }

    public URI getStatus_callback_url() {
        return status_callback_url;
    }

    /**
     * JAXB needs this
     */
    public JobSubmitRequest() {
        super();
    }

    /**
     * Convert requested jobsubmission to JobDescription which can be submitted
     *
     * @return JobDescription
     * @throws GATObjectCreationException
     */
    public JobDescription toJobDescription(Octopus octopus) {
        JobDescription description = new JobDescription();
        description.setExecutable(executable);
        description.setArguments(arguments);
        description.setStdout(stdout);
        description.setStderr(stderr);

        return description;
    }

    public Sandbox toSandbox(Octopus octopus, AbsolutePath sandBoxRoot, String sandboxId) throws OctopusException, OctopusIOException {
        Sandbox sandbox = new Sandbox(octopus, sandBoxRoot, sandboxId);
        FileSystem localFS = sandBoxRoot.getFileSystem();
        // Upload files in request to sandbox
        for (String prestage : prestaged) {
            AbsolutePath src;
            if (prestage.startsWith("/")) {
                src = octopus.files().newPath(localFS, new RelativePath(prestage));
            } else {
                src = octopus.files().newPath(localFS, new RelativePath(new String[] {jobdir, prestage}));
            }
            sandbox.addUploadFile(src, src.getFileName());
        }
        // Download files from sandbox to request.jobdir
        sandbox.addDownloadFile(stdout, octopus.files().newPath(localFS, new RelativePath(new String[] {jobdir, stdout})));
        sandbox.addDownloadFile(stderr, octopus.files().newPath(localFS, new RelativePath(new String[] {jobdir, stderr})));
        for (String poststage : poststaged) {
            AbsolutePath dest = octopus.files().newPath(localFS, new RelativePath(new String[] {jobdir, poststage}));
            sandbox.addDownloadFile(poststage, dest);
        }
        return sandbox;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(jobdir, executable, stderr, stdout, arguments,
                prestaged, poststaged, time_max, memory_max, memory_min,
                status_callback_url);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JobSubmitRequest other = (JobSubmitRequest) obj;
        // TODO Use guava equal helper
//        return Objects.equal(this.arguments, other.arguments) && Objects.equal(this.executable, other.executable)
//                && Objects.equal(this.jobdir, other.jobdir);

        if (!Arrays.equals(arguments, other.arguments))
            return false;
        if (executable == null) {
            if (other.executable != null)
                return false;
        } else if (!executable.equals(other.executable))
            return false;
        if (jobdir == null) {
            if (other.jobdir != null)
                return false;
        } else if (!jobdir.equals(other.jobdir))
            return false;
        if (memory_max != other.memory_max)
            return false;
        if (memory_min != other.memory_min)
            return false;
        if (!Arrays.equals(poststaged, other.poststaged))
            return false;
        if (!Arrays.equals(prestaged, other.prestaged))
            return false;
        if (status_callback_url == null) {
            if (other.status_callback_url != null)
                return false;
        } else if (!status_callback_url.equals(other.status_callback_url))
            return false;
        if (stderr == null) {
            if (other.stderr != null)
                return false;
        } else if (!stderr.equals(other.stderr))
            return false;
        if (stdout == null) {
            if (other.stdout != null)
                return false;
        } else if (!stdout.equals(other.stdout))
            return false;
        if (time_max != other.time_max)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("jobdir", jobdir)
                .add("executable", executable).add("stderr", stderr)
                .add("stdout", stdout)
                .add("arguments", Arrays.toString(arguments))
                .add("prestaged", Arrays.toString(prestaged))
                .add("poststaged", Arrays.toString(poststaged))
                .add("time_max", time_max).add("memory_min", memory_min)
                .add("memory_max", memory_max)
                .add("status_callback_url", status_callback_url).toString();
    }
}
