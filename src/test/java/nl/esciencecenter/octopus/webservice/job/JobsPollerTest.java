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
package nl.esciencecenter.octopus.webservice.job;



import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusException;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.InvalidCopyOptionsException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.util.Sandbox;
import nl.esciencecenter.octopus.webservice.api.SandboxedJob;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

public class JobsPollerTest {

    @Test
    public void run_NoState_StateFilledAndIterationIncreased() throws URISyntaxException {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "1234";
        Job job = new JobImplementation(mock(Scheduler.class), identifier, mock(JobDescription.class), false, false);
        SandboxedJob sjob = new SandboxedJob(null, job, null, null);
        jobs.put(identifier, sjob);
        PollConfiguration pollConf = new PollConfiguration();
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        JobStatus jobstatus = new JobStatusImplementation(job, "RUNNING", 0, null, true, false, null);
        JobStatus[] statuses = { jobstatus };
        // use `doReturn` instead of `when` as argument matching fails
        doReturn(statuses).when(jobsEngine).getJobStatuses((Job[]) any());
        JobsPoller poller = new JobsPoller(jobs, pollConf, octopus);

        poller.run();

        assertThat(sjob.getStatus()).isEqualTo(jobstatus);
        assertThat(sjob.getPollIterations()).isEqualTo(1);
    }

    @Test
    public void run_RunningState_StateUnchangedIterationIncreased() throws URISyntaxException, UnsupportedEncodingException,
            ClientProtocolException, IOException {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "1234";
        Job job = new JobImplementation(mock(Scheduler.class), identifier, mock(JobDescription.class), false, false);
        JobStatus jobstatus = new JobStatusImplementation(job, "RUNNING", 0, null, true, false, null);
        SandboxedJob sjob = new SandboxedJob(null, job, null, null, jobstatus, 5);
        jobs.put(identifier, sjob);
        PollConfiguration pollConf = new PollConfiguration();
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        JobStatus[] statuses = { jobstatus };
        // use `doReturn` instead of `when` as argument matching fails
        doReturn(statuses).when(jobsEngine).getJobStatuses((Job[]) any());
        JobsPoller poller = new JobsPoller(jobs, pollConf, octopus);

        poller.run();

        assertThat(sjob.getStatus()).isEqualTo(jobstatus);
        assertThat(sjob.getPollIterations()).isEqualTo(6);
    }

    @Test
    public void run_PendingState_StateUpdated() throws URISyntaxException {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "1234";
        Job job = new JobImplementation(mock(Scheduler.class), identifier, mock(JobDescription.class), false, false);
        JobStatus jobstatus = new JobStatusImplementation(job, "PENDING", 0, null, false, false, null);
        SandboxedJob sjob = new SandboxedJob(null, job, null, null, jobstatus, 5);
        jobs.put(identifier, sjob);
        PollConfiguration pollConf = new PollConfiguration();
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        JobStatus new_jobstatus = new JobStatusImplementation(job, "RUNNING", 0, null, false, false, null);
        JobStatus[] statuses = { new_jobstatus };
        // use `doReturn` instead of `when` as argument matching fails
        doReturn(statuses).when(jobsEngine).getJobStatuses((Job[]) any());
        JobsPoller poller = new JobsPoller(jobs, pollConf, octopus);

        poller.run();

        assertThat(sjob.getStatus()).isEqualTo(new_jobstatus);
        assertThat(sjob.getPollIterations()).isEqualTo(6);
    }

    @Test
    public void run_RunningState_DoneOkWithCleanupSandbox() throws UnsupportedOperationException, InvalidCopyOptionsException, OctopusException {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "1234";
        Job job = new JobImplementation(mock(Scheduler.class), identifier, mock(JobDescription.class), false, false);
        JobStatus jobstatus = new JobStatusImplementation(job, "RUNNING", 0, null, true, false, null);
        Sandbox sb = mock(Sandbox.class);
        SandboxedJob sjob = new SandboxedJob(sb, job, null, null, jobstatus, 5);
        jobs.put(identifier, sjob);
        PollConfiguration pollConf = new PollConfiguration();
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        JobStatus new_jobstatus = new JobStatusImplementation(job, "Done", 0, null, false, true, null);
        JobStatus[] statuses = { new_jobstatus };
        // use `doReturn` instead of `when` as argument matching fails
        doReturn(statuses).when(jobsEngine).getJobStatuses((Job[]) any());
        JobsPoller poller = new JobsPoller(jobs, pollConf, octopus);

        poller.run();

        verify(sb).download(CopyOption.REPLACE);
        verify(sb).delete();
    }

    @Test
    public void run_PendingStateOnCancelTimeout_JobCanceled() throws OctopusException {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "1234";
        Job job = new JobImplementation(mock(Scheduler.class), identifier, mock(JobDescription.class), false, false);
        JobStatus jobstatus = new JobStatusImplementation(job, "PENDING", 0, null, false, false, null);
        Sandbox sb = mock(Sandbox.class);
        SandboxedJob sjob = new SandboxedJob(sb, job, null, null, jobstatus, 5);
        jobs.put(identifier, sjob);
        PollConfiguration pollConf = new PollConfiguration(1, 5, 10);
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        JobStatus timeout_jobstatus =
                new JobStatusImplementation(job, "KILLED", null, new Exception("Process timed out"), false, true, null);
        when(jobsEngine.cancelJob(job)).thenReturn(timeout_jobstatus);
        JobsPoller poller = new JobsPoller(jobs, pollConf, octopus);

        poller.run();

        verify(jobsEngine).cancelJob(job);
        verify(sb, times(0)).download(CopyOption.REPLACE);
        verify(sb).delete();
        assertThat(sjob.getStatus()).isEqualTo(timeout_jobstatus);
    }

    @Test
    public void run_PendingStateOnDeleteTimeout_JobDeleted() throws OctopusException {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "1234";
        Job job = new JobImplementation(mock(Scheduler.class), identifier, mock(JobDescription.class), false, false);
        JobStatus jobstatus = new JobStatusImplementation(job, "PENDING", 0, null, false, false, null);
        Sandbox sb = mock(Sandbox.class);
        SandboxedJob sjob = new SandboxedJob(sb, job, null, null, jobstatus, 5);
        jobs.put(identifier, sjob);
        PollConfiguration pollConf = new PollConfiguration(1, 2, 5);
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        JobStatus timeout_jobstatus =
                new JobStatusImplementation(job, "KILLED", null, new Exception("Process timed out"), false, true, null);
        when(jobsEngine.cancelJob(job)).thenReturn(timeout_jobstatus);
        JobsPoller poller = new JobsPoller(jobs, pollConf, octopus);

        poller.run();

        verify(jobsEngine).cancelJob(job);
        verify(sb, times(0)).download(CopyOption.REPLACE);
        verify(sb).delete();
        assertThat(jobs).doesNotContainKey(identifier);
    }

    @Test
    public void run_DoneState_JobStatusNotCalledAndIterationIncreased() {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "1234";
        Job job = new JobImplementation(mock(Scheduler.class), identifier, mock(JobDescription.class), false, false);
        JobStatus jobstatus = new JobStatusImplementation(job, "DONE", 0, null, false, true, null);
        SandboxedJob sjob = new SandboxedJob(null, job, null, null, jobstatus, 5);
        jobs.put(identifier, sjob);
        PollConfiguration pollConf = new PollConfiguration();
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        JobsPoller poller = new JobsPoller(jobs, pollConf, octopus);

        poller.run();

        verify(jobsEngine, never()).getJobStatuses(any(Job.class));
        assertThat(sjob.getPollIterations()).isEqualTo(6);
    }

    @Test
    public void testCancelJob() throws OctopusException {
        Octopus octopus = mock(Octopus.class);
        Jobs jobs = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobs);
        JobsPoller poller = new JobsPoller(null, null, octopus);
        SandboxedJob sjob = mock(SandboxedJob.class);
        Job job = mock(Job.class);
        when(sjob.getJob()).thenReturn(job);

        poller.cancelJob(sjob);

        verify(jobs).cancelJob(job);
    }

    @Test
    public void testCleanSandbox() throws UnsupportedOperationException, OctopusException {
        Octopus octopus = mock(Octopus.class);
        JobsPoller poller = new JobsPoller(null, null, octopus);
        SandboxedJob job = mock(SandboxedJob.class);

        poller.cleanSandbox(job);

        verify(job).cleanSandbox();
    }

    @Test
    public void testCommitStatus() throws UnsupportedEncodingException, ClientProtocolException, IOException {
        Octopus octopus = mock(Octopus.class);
        JobsPoller poller = new JobsPoller(null, null, octopus);
        SandboxedJob job = mock(SandboxedJob.class);
        JobStatus status = mock(JobStatus.class);

        poller.commitStatus(status, job);

        verify(job).setStatus(status);
    }

    @Test
    public void testStop_NoJobs_nocancel() throws OctopusException {
        Octopus octopus = mock(Octopus.class);
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        JobsPoller poller = new JobsPoller(jobs, null, octopus);

        poller.stop();

        verifyZeroInteractions(octopus);
    }

    @Test
    public void testStop_DoneJobs_nocancel() throws OctopusException {
        Octopus octopus = mock(Octopus.class);
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "1234";
        Job job = new JobImplementation(mock(Scheduler.class), identifier, mock(JobDescription.class), false, false);
        JobStatus jobstatus = new JobStatusImplementation(job, "DONE", 0, null, false, true, null);
        SandboxedJob sjob = new SandboxedJob(null, job, null, null, jobstatus, 5);
        jobs.put(identifier, sjob);
        JobsPoller poller = new JobsPoller(jobs, null, octopus);

        poller.stop();

        verifyZeroInteractions(octopus);
    }

    @Test
    public void testStop_runningjobs_cancelanddelete() throws OctopusException {
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "1234";
        Job job = new JobImplementation(mock(Scheduler.class), identifier, mock(JobDescription.class), false, false);
        JobStatus jobstatus = new JobStatusImplementation(job, "RUNNING", null, null, true, false, null);
        Sandbox sandbox = mock(Sandbox.class);
        SandboxedJob sjob = new SandboxedJob(sandbox, job, null, null, jobstatus, 5);
        jobs.put(identifier, sjob);
        JobsPoller poller = new JobsPoller(jobs, null, octopus);

        poller.stop();

        verify(jobsEngine).cancelJob(job);
        verify(sandbox).delete();
    }
}
