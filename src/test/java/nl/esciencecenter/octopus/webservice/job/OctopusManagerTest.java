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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.exceptions.NoSuchJobException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.util.Sandbox;
import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;
import nl.esciencecenter.octopus.webservice.api.SandboxedJob;

import org.apache.http.client.HttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableMap;

/**
 *
 * @author verhoes
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(OctopusFactory.class)
public class OctopusManagerTest {

    @Test
    public void testOctopusManager() throws URISyntaxException, OctopusException, OctopusIOException {
        // Use powermock mockito to mock OctopusFactory.newOctopus()
        PowerMockito.mockStatic(OctopusFactory.class);
        Octopus octopus = mock(Octopus.class);
        Jobs jobs = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobs);
        when(OctopusFactory.newOctopus(any(Properties.class))).thenReturn(octopus);

        ImmutableMap<String, Object> prefs = ImmutableMap.of("octopus.adaptors.local.queue.multi.maxConcurrentJobs", (Object) 1);
        OctopusConfiguration conf =
                new OctopusConfiguration(new URI("local:///"), "multi", new URI("file:///tmp/sandboxes"), prefs);

        new OctopusManager(conf);

        // verify octopus created
        PowerMockito.verifyStatic();
        Properties expected_props = new Properties();
        expected_props.setProperty("octopus.adaptors.local.queue.multi.maxConcurrentJobs", "1");
        OctopusFactory.newOctopus(expected_props);
        // verify scheduler created
        when(jobs.newScheduler(new URI("local:///"), null, expected_props));
    }

    @Test
    public void testStart() throws Exception {
        JobsPoller poller = mock(JobsPoller.class);
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        OctopusConfiguration conf = new OctopusConfiguration();
        OctopusManager manager = new OctopusManager(conf, null, null, null, poller, executor);

        manager.start();

        verify(executor).scheduleAtFixedRate(poller, 0, 30*1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testStop() throws Exception {
        Octopus octopus = mock(Octopus.class);
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        OctopusManager manager = new OctopusManager(null, octopus, null, null, null, executor);

        manager.stop();

        verify(octopus).end();
        verify(executor).shutdown();
        verify(executor).awaitTermination(1, TimeUnit.MINUTES);
    }

    @Test
    public void testSubmitJob() throws OctopusIOException, OctopusException, URISyntaxException {
        Properties props = new Properties();
        props.put("octopus.adaptors.local.queue.multi.maxConcurrentJobs", "4");
        ImmutableMap<String, Object> prefs = ImmutableMap.of("octopus.adaptors.local.queue.multi.maxConcurrentJobs", (Object) 1);
        OctopusConfiguration conf =
                new OctopusConfiguration(new URI("local:///"), "multi", new URI("file:///tmp/sandboxes"), prefs);
        Octopus octopus = mock(Octopus.class);
        Scheduler scheduler = mock(Scheduler.class);
        Jobs jobs = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobs);
        Files files = mock(Files.class);
        when(octopus.files()).thenReturn(files);
        AbsolutePath sandboxPath = mock(AbsolutePath.class);
        FileSystem filesystem = mock(FileSystem.class);
        when(files.newFileSystem(new URI("file:///"), null, null)).thenReturn(filesystem);
        when(files.newPath(filesystem, new RelativePath("/tmp/sandboxes"))).thenReturn(sandboxPath);
        JobSubmitRequest request = mock(JobSubmitRequest.class);
        JobDescription description = new JobDescription();
        when(request.toJobDescription()).thenReturn(description);
        Sandbox sandbox = mock(Sandbox.class);
        when(request.toSandbox(octopus, sandboxPath, null)).thenReturn(sandbox);
        when(sandbox.getPath()).thenReturn(sandboxPath);
        when(sandboxPath.getPath()).thenReturn("/tmp/sandboxes");
        HttpClient httpClient = mock(HttpClient.class);
        Job job = mock(Job.class);
        when(job.getUUID()).thenReturn(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        when(jobs.submitJob(scheduler, description)).thenReturn(job);
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        JobsPoller poller = mock(JobsPoller.class);
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        OctopusManager manager = new OctopusManager(conf, octopus, scheduler, sjobs, poller, executor);

        SandboxedJob result = manager.submitJob(request, httpClient);

        assertThat(result.getIdentifier()).isEqualTo("11111111-1111-1111-1111-111111111111");
        verify(sandbox).upload();
        verify(jobs).submitJob(scheduler, description);

        // assert description configuration
        assertThat(description.getMaxTime()).isEqualTo(60);
        assertThat(description.getQueueName()).isEqualTo("multi");
        assertThat(description.getWorkingDirectory()).isEqualTo("/tmp/sandboxes");
    }

    @Test
    public void getJob_DoneJob_DoneJob() throws URISyntaxException, OctopusIOException, OctopusException {
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        SandboxedJob sjob = mock(SandboxedJob.class);
        sjobs.put("11111111-1111-1111-1111-111111111111", sjob);
        OctopusManager manager = new OctopusManager(null, null, null, sjobs, null, null);

        SandboxedJob result = manager.getJob("11111111-1111-1111-1111-111111111111");

        assertThat(result).isEqualTo(sjob);
    }

    @Test(expected=NoSuchJobException.class)
    public void getJob_UnknownJob_ThrowsNoSuchJobException() throws OctopusIOException, OctopusException {
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        OctopusManager manager = new OctopusManager(null, null, null, sjobs, null, null);

        manager.getJob("11111111-1111-1111-1111-111111111111");
    }

    @Test
    public void testCancelJob_NonDoneJob_JobCanceled() throws OctopusException, IOException {
        // create manager with mocked Jobs and other members stubbed
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine= mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        SandboxedJob sjob = mock(SandboxedJob.class);
        Job job = mock(Job.class);
        when(sjob.getJob()).thenReturn(job);
        JobStatus status = mock(JobStatus.class);
        when(status.isDone()).thenReturn(false); // Job is not done
        when(sjob.getStatus()).thenReturn(status);
        sjobs.put("11111111-1111-1111-1111-111111111111", sjob);
        JobStatus timeout_jobstatus = new JobStatusImplementation(job, "KILLED", null, new Exception("Process timed out"), false, true, null);
        when(jobsEngine.getJobStatus(job)).thenReturn(timeout_jobstatus);
        OctopusManager manager = new OctopusManager(null, octopus, null, sjobs, null, null);

        manager.cancelJob("11111111-1111-1111-1111-111111111111");

        verify(jobsEngine).cancelJob(job);
        verify(sjob).setStatus(timeout_jobstatus);
    }

    @Test
    public void testCancelJob_DoneJob_JobNotCanceled() throws OctopusException, IOException {
        // create manager with mocked Jobs and other members stubbed
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine= mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        SandboxedJob sjob = mock(SandboxedJob.class);
        Job job = mock(Job.class);
        when(sjob.getJob()).thenReturn(job);
        JobStatus status = mock(JobStatus.class);
        when(status.isDone()).thenReturn(true); // Job is done
        when(sjob.getStatus()).thenReturn(status);
        sjobs.put("11111111-1111-1111-1111-111111111111", sjob);
        OctopusManager manager = new OctopusManager(null, octopus, null, sjobs, null, null);

        manager.cancelJob("11111111-1111-1111-1111-111111111111");

        verifyNoMoreInteractions(jobsEngine);
    }

    @Test(expected=NoSuchJobException.class)
    public void testCancelJob_UnknownJob_ThrowsNoSuchJobException() throws OctopusException, IOException {
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        OctopusManager manager = new OctopusManager(null, null, null, sjobs, null, null);

        manager.cancelJob("11111111-1111-1111-1111-111111111111");
    }

    @Test
    public void getJobs() {
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        SandboxedJob sjob = mock(SandboxedJob.class);
        when(sjob.getIdentifier()).thenReturn("11111111-1111-1111-1111-111111111111");
        sjobs.put(sjob.getIdentifier(), sjob);
        OctopusManager manager = new OctopusManager(null, null, null, sjobs, null, null);

        Collection<SandboxedJob> jobs = manager.getJobs();

        assertThat(jobs).contains(sjob);
    }
}
