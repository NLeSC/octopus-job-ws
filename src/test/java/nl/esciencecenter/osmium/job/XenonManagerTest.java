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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.jobs.NoSuchJobException;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.util.Sandbox;
import nl.esciencecenter.osmium.api.JobSubmitRequest;
import nl.esciencecenter.osmium.api.SandboxedJob;

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
@PrepareForTest(XenonFactory.class)
public class XenonManagerTest {

    @Test
    public void testXenonManager() throws URISyntaxException, XenonException {
        // Use powermock mockito to mock XenonFactory.newXenon()
        PowerMockito.mockStatic(XenonFactory.class);
        Xenon xenon = mock(Xenon.class);
        Jobs jobs = mock(Jobs.class);
        when(xenon.jobs()).thenReturn(jobs);
        Files files = mock(Files.class);
        when(xenon.files()).thenReturn(files);
        ImmutableMap<String, String> prefs = ImmutableMap.of("xenon.adaptors.local.queue.multi.maxConcurrentJobs", "1");
        when(XenonFactory.newXenon(prefs)).thenReturn(xenon);

        PollConfiguration pollConf = new PollConfiguration();
        SchedulerConfiguration scheduler = new SchedulerConfiguration("local", null, "multi", null);
        SandboxConfiguration sandbox = new SandboxConfiguration("file", null, "/tmp/sandboxes", null);
        XenonConfiguration conf = new XenonConfiguration(scheduler, sandbox, "default", prefs, pollConf);

        new XenonManager(conf);

        // verify xenon created
        PowerMockito.verifyStatic();
        XenonFactory.newXenon(prefs);
        // verify scheduler created
        verify(jobs).newScheduler("local", null, null, null);
    }

    @Test
    public void testStart() throws Exception {
        JobsPoller poller = mock(JobsPoller.class);
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        XenonConfiguration conf = new XenonConfiguration();
        XenonManager manager = new XenonManager(conf, null, null, null, null, poller, executor);

        manager.start();

        verify(executor).scheduleAtFixedRate(poller, 0, 30 * 1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testStop() throws Exception {
        // Use powermock mockito to mock XenonFactory.endXenon()
        PowerMockito.mockStatic(XenonFactory.class);
        Xenon xenon = mock(Xenon.class);
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        JobsPoller poller = mock(JobsPoller.class);
        XenonManager manager = new XenonManager(null, xenon, ImmutableMap.<String,Scheduler>of(), null, null, poller, executor);

        manager.stop();

        verify(executor).shutdown();
        verify(executor).awaitTermination(1, TimeUnit.MINUTES);
        verify(poller).stop();
        PowerMockito.verifyStatic();
        XenonFactory.endXenon(xenon);
    }

    @Test
    public void testSubmitJob() throws XenonException, URISyntaxException {
        Properties props = new Properties();
        props.put("xenon.adaptors.local.queue.multi.maxConcurrentJobs", "4");
        ImmutableMap<String, String> prefs = ImmutableMap.of("xenon.adaptors.local.queue.multi.maxConcurrentJobs", "1");
        PollConfiguration pollConf = new PollConfiguration();
        SchedulerConfiguration schedulerConf = new SchedulerConfiguration("local", null, "multi", null);
        SandboxConfiguration sandboxConf = new SandboxConfiguration("file", null, "/tmp/sandboxes", null);
        XenonConfiguration conf = new XenonConfiguration(schedulerConf, sandboxConf, "default", prefs, pollConf);
        Xenon xenon = mock(Xenon.class);
        Scheduler scheduler = mock(Scheduler.class);
        ImmutableMap<String, Scheduler> schedulers = ImmutableMap.of(conf.getDefaultLauncher(), scheduler);
        Jobs jobs = mock(Jobs.class);
        when(xenon.jobs()).thenReturn(jobs);
        Files files = mock(Files.class);
        when(xenon.files()).thenReturn(files);
        Path sandboxPath = mock(Path.class);
        FileSystem filesystem = mock(FileSystem.class);
        when(files.newFileSystem("file", "/", null, null)).thenReturn(filesystem);
        when(files.newPath(filesystem, new RelativePath("/tmp/sandboxes"))).thenReturn(sandboxPath);
        JobSubmitRequest request = mock(JobSubmitRequest.class);
        JobDescription description = new JobDescription();
        when(request.toJobDescription()).thenReturn(description);
        Sandbox sandbox = mock(Sandbox.class);
        when(request.toSandbox(files, sandboxPath, null)).thenReturn(sandbox);
        when(sandbox.getPath()).thenReturn(sandboxPath);
        RelativePath sandboxRelativePath = mock(RelativePath.class);
        when(sandboxPath.getRelativePath()).thenReturn(sandboxRelativePath);
        when(sandboxRelativePath.getAbsolutePath()).thenReturn("/tmp/sandboxes");
        HttpClient httpClient = mock(HttpClient.class);
        Job job = mock(Job.class);
        when(job.getIdentifier()).thenReturn("1234");
        when(jobs.submitJob(scheduler, description)).thenReturn(job);
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        JobsPoller poller = mock(JobsPoller.class);
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        XenonManager manager = new XenonManager(conf, xenon, schedulers, ImmutableMap.of(conf.getDefaultLauncher(), sandboxPath), sjobs, poller, executor);

        SandboxedJob result = manager.submitJob(request, httpClient);

        assertThat(result.getIdentifier()).isEqualTo("1234");
        verify(sandbox).upload();
        verify(jobs).submitJob(scheduler, description);

        // assert description configuration
        assertThat(description.getMaxTime()).isEqualTo(60);
        assertThat(description.getQueueName()).isEqualTo("multi");
        assertThat(description.getWorkingDirectory()).isEqualTo("/tmp/sandboxes");
    }

    @Test
    public void getJob_DoneJob_DoneJob() throws URISyntaxException, XenonException {
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        SandboxedJob sjob = mock(SandboxedJob.class);
        sjobs.put("1234", sjob);
        XenonManager manager = new XenonManager(null, null, null, null, sjobs, null, null);

        SandboxedJob result = manager.getJob("1234");

        assertThat(result).isEqualTo(sjob);
    }

    @Test(expected = NoSuchJobException.class)
    public void getJob_UnknownJob_ThrowsNoSuchJobException() throws XenonException {
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        XenonManager manager = new XenonManager(null, null, null, null, sjobs, null, null);

        manager.getJob("1234");
    }

    @Test
    public void testCancelJob_NonDoneJob_JobCanceled() throws XenonException, IOException {
        // create manager with mocked Jobs and other members stubbed
        Xenon xenon = mock(Xenon.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(xenon.jobs()).thenReturn(jobsEngine);
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        SandboxedJob sjob = mock(SandboxedJob.class);
        Job job = mock(Job.class);
        when(sjob.getJob()).thenReturn(job);
        JobStatus status = mock(JobStatus.class);
        when(status.isDone()).thenReturn(false); // Job is not done
        when(sjob.getStatus()).thenReturn(status);
        sjobs.put("1234", sjob);
        JobStatus timeout_jobstatus =
                new JobStatusImplementation(job, "KILLED", null, new Exception("Process timed out"), false, true, null);
        when(jobsEngine.cancelJob(job)).thenReturn(timeout_jobstatus);
        XenonManager manager = new XenonManager(null, xenon, null, null, sjobs, null, null);

        manager.cancelJob("1234");

        verify(jobsEngine).cancelJob(job);
        verify(sjob).setStatus(timeout_jobstatus);
    }

    @Test
    public void testCancelJob_DoneJob_JobNotCanceled() throws XenonException, IOException {
        // create manager with mocked Jobs and other members stubbed
        Xenon xenon = mock(Xenon.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(xenon.jobs()).thenReturn(jobsEngine);
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        SandboxedJob sjob = mock(SandboxedJob.class);
        Job job = mock(Job.class);
        when(sjob.getJob()).thenReturn(job);
        JobStatus status = mock(JobStatus.class);
        when(status.isDone()).thenReturn(true); // Job is done
        when(sjob.getStatus()).thenReturn(status);
        sjobs.put("1234", sjob);
        XenonManager manager = new XenonManager(null, xenon, null, null, sjobs, null, null);

        manager.cancelJob("1234");

        verifyNoMoreInteractions(jobsEngine);
    }

    @Test(expected = NoSuchJobException.class)
    public void testCancelJob_UnknownJob_ThrowsNoSuchJobException() throws XenonException, IOException {
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        XenonManager manager = new XenonManager(null, null, null, null, sjobs, null, null);

        manager.cancelJob("1234");
    }

    @Test
    public void testCancelJob_UnknownStatus() throws XenonException, IOException {
        // create manager with mocked Jobs and other members stubbed
        Xenon xenon = mock(Xenon.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(xenon.jobs()).thenReturn(jobsEngine);
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        SandboxedJob sjob = mock(SandboxedJob.class);
        Job job = mock(Job.class);
        when(sjob.getJob()).thenReturn(job);
        JobStatus status = null;
        when(sjob.getStatus()).thenReturn(status);
        sjobs.put("1234", sjob);
        JobStatus timeout_jobstatus =
                new JobStatusImplementation(job, "KILLED", null, new Exception("Process timed out"), false, true, null);
        when(jobsEngine.cancelJob(job)).thenReturn(timeout_jobstatus);
        XenonManager manager = new XenonManager(null, xenon, null, null, sjobs, null, null);

        manager.cancelJob("1234");

        verify(jobsEngine).cancelJob(job);
        verify(sjob).setStatus(timeout_jobstatus);
    }

    @Test
    public void getJobs() {
        Map<String, SandboxedJob> sjobs = new HashMap<String, SandboxedJob>();
        SandboxedJob sjob = mock(SandboxedJob.class);
        when(sjob.getIdentifier()).thenReturn("1234");
        sjobs.put(sjob.getIdentifier(), sjob);
        XenonManager manager = new XenonManager(null, null, null, null, sjobs, null, null);

        Collection<SandboxedJob> jobs = manager.getJobs();

        assertThat(jobs).contains(sjob);
    }
}
