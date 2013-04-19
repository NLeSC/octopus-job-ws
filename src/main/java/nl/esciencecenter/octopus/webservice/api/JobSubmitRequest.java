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

import com.google.common.base.Objects;

/**
 * Request which can be converted to JobDescription which can be submitted using JavaGAT.
 *
 * @author Stefan Verhoeven <s.verhoeven@esciencecenter.nl>
 *
 */
public class JobSubmitRequest {
    /**
     * Job directory where stderr/stdout/prestaged/poststaged file are relative to and where job.state file is written. Must end
     * with '/'
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
    public List<String> arguments;
    /**
     * List of filenames to copy from job directory to work directory before executable is called. Work directory is created on
     * the execution host. Can be relative to job directory or absolute paths.
     */
    public List<String> prestaged;
    /**
     * List of filenames to copy from work directory to job directory after executable is called. Must be relative to job
     * directory
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

    public Sandbox toSandbox(Octopus octopus, AbsolutePath sandBoxRoot, String sandboxId) throws OctopusException,
            OctopusIOException {
        Sandbox sandbox = new Sandbox(octopus, sandBoxRoot, sandboxId);
        FileSystem localFS = sandBoxRoot.getFileSystem();
        // Upload files in request to sandbox
        for (String prestage : prestaged) {
            AbsolutePath src;
            if (prestage.startsWith("/")) {
                src = octopus.files().newPath(localFS, new RelativePath(prestage));
            } else {
                RelativePath rsrc = new RelativePath(new String[] { jobdir, prestage });
                src = octopus.files().newPath(localFS, rsrc);
            }
            String filename = src.getFileName();
            sandbox.addUploadFile(src, filename);
        }
        // Download files from sandbox to request.jobdir
        sandbox.addDownloadFile(stdout, octopus.files().newPath(localFS, new RelativePath(new String[] { jobdir, stdout })));
        sandbox.addDownloadFile(stderr, octopus.files().newPath(localFS, new RelativePath(new String[] { jobdir, stderr })));
        for (String poststage : poststaged) {
            AbsolutePath dest = octopus.files().newPath(localFS, new RelativePath(new String[] { jobdir, poststage }));
            sandbox.addDownloadFile(poststage, dest);
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
