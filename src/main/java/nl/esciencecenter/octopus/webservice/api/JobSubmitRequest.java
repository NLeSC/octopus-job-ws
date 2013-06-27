package nl.esciencecenter.octopus.webservice.api;

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
import java.util.List;

import javax.validation.constraints.NotNull;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.util.Sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

/**
 * Request which can be converted to JobDescription which can be submitted using JavaGAT.
 * 
 * @author Stefan Verhoeven <s.verhoeven@esciencecenter.nl>
 * 
 */
public class JobSubmitRequest {
    protected final static Logger logger = LoggerFactory.getLogger(JobSubmitRequest.class);

    /**
     * Job directory where stderr/stdout/prestaged/poststaged file are relative to and where job.state file is written. Must end
     * with '/'
     */
    @NotNull
    public String jobdir;
    /**
     * Local directory to read prestaged files from and write poststaged files to.
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
    public List<String> arguments;
    /**
     * List of filenames to copy from job directory to work directory before executable is called. Work directory is created on
     * the execution host. Can be relative to job directory or absolute paths.
     */
    public List<String> prestaged;
    /**
     * List of filenames to copy from work directory to job directory after executable is called. Must be relative to job
     * directory.
     */
    public List<String> poststaged;
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
    public JobSubmitRequest(String jobdir, String executable, List<String> arguments, List<String> prestaged,
            List<String> poststaged, String stderr, String stdout, URI status_callback_url) {
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
    public JobDescription toJobDescription() {
        JobDescription description = new JobDescription();
        description.setExecutable(executable);
        description.setArguments(arguments.toArray(new String[0]));
        description.setStdout(stdout);
        description.setStderr(stderr);

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
     * [direction], [argument], [source] -> [destination]</li>
     * <li>
     * Prestage, "runme.sh", "/tmp/jobdir/runme.sh" -> "/tmp/sandbox/runme.sh"</li>
     * <li>
     * Prestage, "/data/uniprot.fasta", "/data/uniprot.fasta" -> "/tmp/sandbox/uniprot.fasta"</li>
     * <li>
     * Prestage, "input/data.in", "/tmp/jobdir/input/data.in" -> "/tmp/sandbox/data.in"</li>
     * <li>
     * Poststage, "data.out", "/tmp/sandbox/data.out" -> "/tmp/jobdir/data.out"</li>
     * <li>
     * Poststage, "/data/uniprot.fasta", "/tmp/sandbox/uniprot.fasta" -> "/tmp/jobdir/data/uniprot.fasta"</li>
     * <li>
     * Poststage "output/data.out", "/tmp/sandbox/data.out" -> "/tmp/jobdir/output/data.out"</li>
     * <ul>
     * 
     * @param octopus
     *            Octopus instance
     * @param sandBoxRoot
     *            Path in which a sandbox will be created.
     * @param sandboxId
     *            Identifier of sandbox
     * @return A sandbox with stderr, stdout, prestaged and poststaged files/directories.
     * @throws OctopusException
     * @throws OctopusIOException
     * @throws URISyntaxException
     */
    public Sandbox toSandbox(Octopus octopus, AbsolutePath sandBoxRoot, String sandboxId) throws OctopusException,
            OctopusIOException, URISyntaxException {
        Sandbox sandbox = new Sandbox(octopus, sandBoxRoot, sandboxId);
        FileSystem localFs = octopus.files().newFileSystem(new URI("file:///"), null, null);
        AbsolutePath localRoot = octopus.files().newPath(localFs, new RelativePath());
        AbsolutePath jobPath = localRoot.resolve(new RelativePath(jobdir));

        // Upload files in request to sandbox
        for (String prestage : prestaged) {
            AbsolutePath src;
            if (prestage.startsWith("/")) {
                src = localRoot.resolve(new RelativePath(prestage));
            } else {
                src = jobPath.resolve(new RelativePath(prestage));
            }
            sandbox.addUploadFile(src);
        }

        // Download files from sandbox to request.jobdir
        sandbox.addDownloadFile(stdout, jobPath.resolve(new RelativePath(stdout)));
        sandbox.addDownloadFile(stderr, jobPath.resolve(new RelativePath(stderr)));
        for (String poststage : poststaged) {
            AbsolutePath dest = jobPath.resolve(new RelativePath(poststage));
            sandbox.addDownloadFile(null, dest);
        }

        return sandbox;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(jobdir, executable, stderr, stdout, arguments, prestaged, poststaged, status_callback_url);
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
        return Objects.equal(this.jobdir, other.jobdir) && Objects.equal(this.executable, other.executable)
                && Objects.equal(this.arguments, other.arguments) && Objects.equal(this.stderr, other.stderr)
                && Objects.equal(this.stdout, other.stdout) && Objects.equal(this.prestaged, other.prestaged)
                && Objects.equal(this.poststaged, other.poststaged)
                && Objects.equal(this.status_callback_url, other.status_callback_url);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("jobdir", jobdir).add("executable", executable).add("stderr", stderr)
                .add("stdout", stdout).add("arguments", arguments).add("prestaged", prestaged).add("poststaged", poststaged)
                .add("status_callback_url", status_callback_url).toString();
    }
}
