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

import static com.yammer.dropwizard.testing.JsonHelpers.asJson;
import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;
import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.files.FileSystemImplementation;
import nl.esciencecenter.xenon.engine.files.PathImplementation;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.util.Sandbox;

import org.junit.Before;
import org.junit.Test;

public class JobSubmitRequestTest {
    private JobSubmitRequest request;
    private FileSystem filesystem;
    private Xenon xenon;
    private Files filesEngine;

    @Before
    public void setUp() throws URISyntaxException, XenonException {
        request = sampleRequest();
        filesystem = new FileSystemImplementation("local", "local-1", "file", "/", new RelativePath(), null, null);
        xenon = mock(Xenon.class);
        filesEngine = mock(Files.class);
        when(xenon.files()).thenReturn(filesEngine);
        when(filesEngine.newFileSystem("file", "/", null, null)).thenReturn(filesystem);
        Path path = new PathImplementation(filesystem, new RelativePath());
        when(filesEngine.newPath(filesystem, new RelativePath())).thenReturn(path);
        when(filesEngine.newPath(filesystem, new RelativePath("/tmp/sandboxes"))).thenReturn(
                new PathImplementation(filesystem, new RelativePath("/tmp/sandboxes")));
        when(filesEngine.newPath(filesystem, new RelativePath("/tmp/sandboxes/xenon-sandbox-1234567890"))).thenReturn(
                new PathImplementation(filesystem, new RelativePath("/tmp/sandboxes/xenon-sandbox-1234567890")));
        when(filesEngine.newPath(filesystem, new RelativePath("/tmp/jobdir"))).thenReturn(
                new PathImplementation(filesystem, new RelativePath("/tmp/jobdir")));
        when(filesEngine.newPath(filesystem, new RelativePath("/tmp/jobdir/runme.sh"))).thenReturn(
                new PathImplementation(filesystem, new RelativePath("/tmp/jobdir/runme.sh")));
        when(filesEngine.newPath(filesystem, new RelativePath("/tmp/jobdir/input.dat"))).thenReturn(
                new PathImplementation(filesystem, new RelativePath("/tmp/jobdir/input.dat")));
        when(filesEngine.newPath(filesystem, new RelativePath("/tmp/jobdir/stdout.txt"))).thenReturn(
                new PathImplementation(filesystem, new RelativePath("/tmp/jobdir/stdout.txt")));
        when(filesEngine.newPath(filesystem, new RelativePath("/tmp/jobdir/stderr.txt"))).thenReturn(
                new PathImplementation(filesystem, new RelativePath("/tmp/jobdir/stderr.txt")));
        when(filesEngine.newPath(filesystem, new RelativePath("/tmp/jobdir/output.dat"))).thenReturn(
                new PathImplementation(filesystem, new RelativePath("/tmp/jobdir/output.dat")));
        when(filesEngine.newPath(filesystem, new RelativePath("/tmp/jobdir/data/uniprot.fasta"))).thenReturn(
                new PathImplementation(filesystem, new RelativePath("/tmp/jobdir/data/uniprot.fasta")));
        when(filesEngine.newPath(filesystem, new RelativePath("/data/uniprot.fasta"))).thenReturn(
                new PathImplementation(filesystem, new RelativePath("/data/uniprot.fasta")));
    }

    @Test
    public void testJobSubmitRequest_AllArguments() {
        assertNotNull(request);
    }

    @Test
    public void testGetStatus_callback_url() throws URISyntaxException {
        assertEquals(request.getStatus_callback_url(), new URI("http://localhost/status"));
    }

    @Test
    public void testJobSubmitRequest() {
        assertNotNull(new JobSubmitRequest());
    }

    public static JobSubmitRequest sampleRequest() {
        List<String> arguments = new ArrayList<String>();
        arguments.add("runme.sh");
        List<String> prestaged = new ArrayList<String>();
        prestaged.add("runme.sh");
        prestaged.add("input.dat");
        List<String> poststaged = new ArrayList<String>();
        poststaged.add("output.dat");
        URI cb = null;
        try {
            cb = new URI("http://localhost/status");
        } catch (URISyntaxException e) {
        }
		Map<String, String> environment = new HashMap<String, String>();
		environment.put("OSMIUM_JOBID", "mynewjob");
        return new JobSubmitRequest(null, "/tmp/jobdir", "/bin/sh", arguments, prestaged, poststaged, "stderr.txt", "stdout.txt", environment, cb);
    }

    @Test
    public void serializesToJSON() throws IOException {
        assertThat("a JobSubmitRequest can be serialized to JSON", asJson(request),
                is(equalTo(jsonFixture("fixtures/request.json"))));
    }

    @Test
    public void deserializesFromJSON() throws IOException {
        assertThat("a JobSubmitRequest can be deserialized from JSON",
                fromJson(jsonFixture("fixtures/request.json"), JobSubmitRequest.class), is(request));
    }

    @Test
    public void deserializedFromJson_WithoutCallback() throws IOException {
        request.status_callback_url = null;
        assertThat("a JobSubmitRequest can be deserialized from JSON",
                fromJson(jsonFixture("fixtures/request.nocallback.json"), JobSubmitRequest.class), is(request));
    }

    @Test
    public void deserializedFromJson_Minimal() throws IOException {
        JobSubmitRequest minimalRequest = new JobSubmitRequest();
        minimalRequest.jobdir = "/tmp/jobdir";
        minimalRequest.executable = "/bin/sh";
        assertThat("a JobSubmitRequest can be deserialized from JSON",
                fromJson(jsonFixture("fixtures/request.minimal.json"), JobSubmitRequest.class), is(minimalRequest));
    }

    @Test
    public void testEquals() {
        assertThat(request.equals(null)).isFalse();

        assertThat(request.equals("string")).isFalse();

        assertTrue(request.equals(request));

        assertTrue(request.equals(sampleRequest()));

        JobSubmitRequest r2 = sampleRequest();
        r2.executable = "/bin/bash";
        assertFalse(request.equals(r2));

        JobSubmitRequest r3 = sampleRequest();
        r3.jobdir = "/tmp/jobdir2";
        assertFalse(request.equals(r3));

        JobSubmitRequest r4 = sampleRequest();
        r4.stderr = "error.log";
        assertFalse(request.equals(r4));

        JobSubmitRequest r5 = sampleRequest();
        r5.stdout = "out.log";
        assertFalse(request.equals(r5));

        JobSubmitRequest r6 = sampleRequest();
        r6.arguments.add("bla");
        assertFalse(request.equals(r6));

        JobSubmitRequest r7 = sampleRequest();
        r7.prestaged.add("somefile");
        assertFalse(request.equals(r7));

        JobSubmitRequest r8 = sampleRequest();
        r8.poststaged.add("somefile");
        assertFalse(request.equals(r8));

        try {
            JobSubmitRequest r9 = sampleRequest();
            r9.status_callback_url = new URI("http://example.com");
            assertFalse(request.equals(r9));
        } catch (URISyntaxException e) {
            fail();
        }
    }

    @Test
    public void testHashCode() {
        assertThat(request.hashCode()).isEqualTo(-2075112292);
    }

    @Test
    public void testToString() {
        String s =
                "JobSubmitRequest{scheduler=null, jobdir=/tmp/jobdir, executable=/bin/sh, stderr=stderr.txt, stdout=stdout.txt, arguments=[runme.sh], prestaged=[runme.sh, input.dat], poststaged=[output.dat], environment={OSMIUM_JOBID=mynewjob}, status_callback_url=http://localhost/status}";
        assertEquals(s, request.toString());
    }

    @Test
    public void toJobDescription() {
        JobDescription description = request.toJobDescription();

        JobDescription expected_description = new JobDescription();
        expected_description.setArguments("runme.sh");
        expected_description.setExecutable("/bin/sh");
        expected_description.setStderr("stderr.txt");
        expected_description.setStdout("stdout.txt");
		Map<String, String> env = new HashMap<String, String>();
		env.put("OSMIUM_JOBID", "mynewjob");
		expected_description.setEnvironment(env);

        // FIXME when https://github.com/NLeSC/xenon/issues/53 is resolved then remove ignore
        assertThat(description.toString()).isEqualTo(expected_description.toString());
    }

    @Test
    public void toSandbox() throws URISyntaxException, XenonException {
        String sandboxId = "xenon-sandbox-1234567890";
        Path sandBoxRoot = makePath("/tmp/sandboxes");

        Sandbox sandbox = request.toSandbox(filesEngine, sandBoxRoot, sandboxId);

        Sandbox expected = new Sandbox(filesEngine, sandBoxRoot, sandboxId);
        expected.addUploadFile(makePath("/tmp/jobdir/runme.sh"), "runme.sh");
        expected.addUploadFile(makePath("/tmp/jobdir/input.dat"), "input.dat");
        expected.addDownloadFile("stdout.txt", makePath("/tmp/jobdir/stdout.txt"));
        expected.addDownloadFile("stderr.txt", makePath("/tmp/jobdir/stderr.txt"));
        expected.addDownloadFile("output.dat", makePath("/tmp/jobdir/output.dat"));

        assertThat(sandbox).isEqualTo(expected);
    }

    private Path makePath(String path) throws XenonException {
        return filesEngine.newPath(filesystem, new RelativePath(path));
    }

    @Test
    public void postStage2Sandbox_AbsolutePrestage_keepAbsolute() throws URISyntaxException, XenonException {
        String sandboxId = "xenon-sandbox-1234567890";
        Path sandBoxRoot = makePath("/tmp/sandboxes");

        List<String> prestaged = new ArrayList<String>();
        prestaged.add("/data/uniprot.fasta");
        JobSubmitRequest req =
                new JobSubmitRequest(null, "/tmp/jobdir", "/usr/bin/mail", new ArrayList<String>(), prestaged, new ArrayList<String>(),
                        "stderr.txt", "stdout.txt", new HashMap<String, String>(), null);

        Sandbox sandbox = req.toSandbox(filesEngine, sandBoxRoot, sandboxId);

        Sandbox expected = new Sandbox(filesEngine, sandBoxRoot, sandboxId);
        expected.addUploadFile(makePath("/data/uniprot.fasta"), "uniprot.fasta");
        expected.addDownloadFile("stdout.txt", makePath("/tmp/jobdir/stdout.txt"));
        expected.addDownloadFile("stderr.txt", makePath("/tmp/jobdir/stderr.txt"));

        assertThat(sandbox).isEqualTo(expected);
    }

    @Test
    public void toSandbox_NestedPreStage_flattened() throws XenonException, URISyntaxException {
        String sandboxId = "xenon-sandbox-1234567890";
        Path sandBoxRoot = makePath("/tmp/sandboxes");

        List<String> prestaged = new ArrayList<String>();
        prestaged.add("data/uniprot.fasta");
        JobSubmitRequest req =
                new JobSubmitRequest(null, "/tmp/jobdir", "/usr/bin/mail", new ArrayList<String>(), prestaged, new ArrayList<String>(),
                        "stderr.txt", "stdout.txt", new HashMap<String, String>(), null);

        Sandbox sandbox = req.toSandbox(filesEngine, sandBoxRoot, sandboxId);

        Sandbox expected = new Sandbox(filesEngine, sandBoxRoot, sandboxId);
        expected.addUploadFile(makePath("/tmp/jobdir/data/uniprot.fasta"), "uniprot.fasta");
        expected.addDownloadFile("stdout.txt", makePath("/tmp/jobdir/stdout.txt"));
        expected.addDownloadFile("stderr.txt", makePath("/tmp/jobdir/stderr.txt"));

        assertThat(sandbox).isEqualTo(expected);
    }

    @Test
    public void toSandbox_AbsolutePoststage_keepAbsolute() throws URISyntaxException, XenonException {
        String sandboxId = "xenon-sandbox-1234567890";
        Path sandBoxRoot = makePath("/tmp/sandboxes");

        List<String> poststaged = new ArrayList<String>();
        poststaged.add("/data/uniprot.fasta");
        JobSubmitRequest req =
                new JobSubmitRequest(null, "/tmp/jobdir", "/usr/bin/mail", new ArrayList<String>(), new ArrayList<String>(),
                        poststaged, "stderr.txt", "stdout.txt", new HashMap<String, String>(), null);

        Sandbox sandbox = req.toSandbox(filesEngine, sandBoxRoot, sandboxId);

        Sandbox expected = new Sandbox(filesEngine, sandBoxRoot, sandboxId);
        expected.addDownloadFile("stdout.txt", makePath("/tmp/jobdir/stdout.txt"));
        expected.addDownloadFile("stderr.txt", makePath("/tmp/jobdir/stderr.txt"));
        expected.addDownloadFile("uniprot.fasta", makePath("/tmp/jobdir/data/uniprot.fasta"));

        assertThat(sandbox).isEqualTo(expected);
    }

    @Test
    public void toSandbox_NestedPoststage_Flattened() throws URISyntaxException, XenonException {
        String sandboxId = "xenon-sandbox-1234567890";
        Path sandBoxRoot = makePath("/tmp/sandboxes");

        List<String> poststaged = new ArrayList<String>();
        poststaged.add("data/uniprot.fasta");
        JobSubmitRequest req =
                new JobSubmitRequest(null, "/tmp/jobdir", "/usr/bin/mail", new ArrayList<String>(), new ArrayList<String>(),
                        poststaged, "stderr.txt", "stdout.txt", new HashMap<String, String>(), null);

        Sandbox sandbox = req.toSandbox(filesEngine, sandBoxRoot, sandboxId);

        Sandbox expected = new Sandbox(filesEngine, sandBoxRoot, sandboxId);
        expected.addDownloadFile("stdout.txt", makePath("/tmp/jobdir/stdout.txt"));
        expected.addDownloadFile("stderr.txt", makePath("/tmp/jobdir/stderr.txt"));
        expected.addDownloadFile("uniprot.fasta", makePath("/tmp/jobdir/data/uniprot.fasta"));

        assertThat(sandbox).isEqualTo(expected);
    }

}
