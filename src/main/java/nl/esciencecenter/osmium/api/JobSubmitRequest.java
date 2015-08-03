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
package nl.esciencecenter.osmium.api;

import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.util.Sandbox;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

/**
 * Request which can be converted to JobDescription which can be submitted using JavaGAT.
 *
 * @author Stefan Verhoeven &lt;s.verhoeven@esciencecenter.nl&gt;
 *
 */
public class JobSubmitRequest {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JobSubmitRequest.class);

    /**
     * Pre-configured launcher to run job with.
     */
    public String launcher;

    /**
     * Job directory where stderr/stdout/prestaged/poststaged file are relative to and where job.state file is written. Must end
     * with '/'
     */
    @NotNull
    public String jobdir;
    /**
     * Path to executable on execution host.
     */
    @NotNull
    public String executable;
    
    /**
     * Local directory to read pre-staged files from and write post-staged files to.
     */
    @NotNull
    public String sandbox_path;

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
    public List<String> arguments = new ArrayList<>(0);
    /**
     * List of filenames to copy from job directory to work directory before executable is called. Work directory is created on
     * the execution host. Can be relative to job directory or absolute paths.
     */
    public List<String> prestaged = new ArrayList<>(0);
    /**
     * List of filenames to copy from work directory to job directory after executable is called. Must be relative to job
     * directory.
     */
    public List<String> poststaged = new ArrayList<>(0);
    /**
     * Environment variables and their values.
     */
    public Map<String, String> environment = new HashMap<>(0);

    /**
     * Url where changes of state are PUT to.
     */
    public URI status_callback_url;
	
    /**
     * Number of minutes the job may run.
     */
    public int max_time = -1;

	/**
     * Constructor
     *
     * @param launcher Launcher to run job with. If null, assume the default launcher.
     * @param jobdir Directory as source of prestaged files and target of poststaged files
     * @param executable Path to executable
     * @param arguments Arguments for executable
     * @param prestaged List of files that must be copied from jobdir to sandbox
     * @param poststaged List of files that must be copied from sandbox to jobidr
     * @param stderr Name of file where stderr is written
     * @param stdout Name of file where stdout is written
     * @param environment variables url where status changes should be POST-ed
     * @param statusCallbackURI Optional, url where status changes should be POST-ed
     * @param maxTime number of minutes the job may run.
     */
    public JobSubmitRequest(String launcher, String jobdir, String executable, List<String> arguments, List<String> prestaged,
            List<String> poststaged, String stderr, String stdout, Map<String, String> environment, URI statusCallbackURI,
            int maxTime) {
        super();
        this.launcher = launcher;
        this.jobdir = jobdir;
        this.executable = executable;
        this.arguments = arguments;
        this.prestaged = prestaged;
        this.poststaged = poststaged;
        this.stderr = stderr;
        this.stdout = stdout;
		this.environment = environment;
        this.status_callback_url = statusCallbackURI;
        this.max_time = maxTime;
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
     */
    public JobDescription toJobDescription() {
        JobDescription description = new JobDescription();
        description.setExecutable(executable);
        description.setArguments(arguments.toArray(new String[arguments.size()]));
        description.setStdout(stdout);
        description.setStderr(stderr);
		description.setEnvironment(environment);
        description.setMaxTime(max_time);

        return description;
    }

    /**
     * Create sandbox from request.
     *
     * Prestaged files/directories will be added to upload list. Poststaged files/directories will be added to download list.
     * Stderr and Stdout will be added to download list when they are not null.
     *
     * Examples when jobdir = /tmp/jobdir and sandboxpath = /tmp/sandbox
     *
     * <ul>
     * <li>
     * [direction], [argument], [source] -&gt; [destination]</li>
     * <li>
     * Prestage, "runme.sh", "/tmp/jobdir/runme.sh" -&gt; "/tmp/sandbox/runme.sh"</li>
     * <li>
     * Prestage, "/data/uniprot.fasta", "/data/uniprot.fasta" -&gt; "/tmp/sandbox/uniprot.fasta"</li>
     * <li>
     * Prestage, "input/data.in", "/tmp/jobdir/input/data.in" -&gt; "/tmp/sandbox/data.in"</li>
     * <li>
     * Poststage, "data.out", "/tmp/sandbox/data.out" -&gt; "/tmp/jobdir/data.out"</li>
     * <li>
     * Poststage, "/data/uniprot.fasta", "/tmp/sandbox/uniprot.fasta" -&gt; "/tmp/jobdir/data/uniprot.fasta"</li>
     * <li>
     * Poststage "output/data.out", "/tmp/sandbox/data.out" -&gt; "/tmp/jobdir/output/data.out"</li>
     * </ul>
     *
     * @param filesEngine
     *            Xenon Files instance
     * @param sandBoxRoot
     *            Path in which a sandbox will be created.
     * @param sandboxId
     *            Identifier of sandbox
     * @return A sandbox with stderr, stdout, prestaged and poststaged files/directories.
     *
     * @throws XenonException if unable to copy files
     */
    public Sandbox toSandbox(Files filesEngine, Path sandBoxRoot, String sandboxId) throws XenonException {
        Sandbox sandbox = new Sandbox(filesEngine, sandBoxRoot, sandboxId);
        FileSystem localFs = filesEngine.newFileSystem("file", "/", null, null);
        Path localRoot = filesEngine.newPath(localFs, new RelativePath());
        Path jobPath = filesEngine.newPath(localFs, new RelativePath(jobdir));

        // Upload files in request to sandbox
        for (String prestage : prestaged) {
            RelativePath src;
            if (prestage.startsWith("/")) {
                src = localRoot.getRelativePath().resolve(prestage);
            } else {
                src = jobPath.getRelativePath().resolve(prestage);
            }
            sandbox.addUploadFile(filesEngine.newPath(localFs, src));
        }

        // Download files from sandbox to request.jobdir
        sandbox.addDownloadFile(stdout, filesEngine.newPath(localFs, jobPath.getRelativePath().resolve(stdout)));
        sandbox.addDownloadFile(stderr, filesEngine.newPath(localFs, jobPath.getRelativePath().resolve(stderr)));
        for (String poststage : poststaged) {
            Path dest = filesEngine.newPath(localFs, jobPath.getRelativePath().resolve(poststage));
            sandbox.addDownloadFile(null, dest);
        }

        return sandbox;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(launcher, jobdir, executable, stderr, stdout, arguments, prestaged, poststaged, environment, status_callback_url, max_time);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JobSubmitRequest other = (JobSubmitRequest) obj;
        return Objects.equal(this.launcher, other.launcher)
                && Objects.equal(this.jobdir, other.jobdir) && Objects.equal(this.executable, other.executable)
                && Objects.equal(this.arguments, other.arguments) && Objects.equal(this.stderr, other.stderr)
                && Objects.equal(this.stdout, other.stdout) && Objects.equal(this.prestaged, other.prestaged)
                && Objects.equal(this.poststaged, other.poststaged) && Objects.equal(this.environment, other.environment)
                && Objects.equal(this.status_callback_url, other.status_callback_url)
                && Objects.equal(this.max_time, other.max_time);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("launcher", launcher)
                .add("jobdir", jobdir).add("executable", executable).add("stderr", stderr)
                .add("stdout", stdout).add("arguments", arguments).add("prestaged", prestaged).add("poststaged", poststaged)
				.add("environment", environment).add("status_callback_url", status_callback_url).add("maxTime", max_time).toString();
    }
}
