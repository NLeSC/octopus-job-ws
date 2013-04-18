package nl.esciencecenter.octopus.webservice.job;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.util.Sandbox;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

public class JobsPollerTest {

    @Test
    public void testPoll_NoState_StateFilledAndIterationIncreased() throws URISyntaxException {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "11111111-1111-1111-1111-111111111111";
        Job job = new JobImplementation(mock(JobDescription.class), mock(Scheduler.class), identifier);
        SandboxedJob sjob = new SandboxedJob(null, job, null, null);
        jobs.put(identifier, sjob);
        PollConfiguration pollConf = new PollConfiguration();
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        JobStatus jobstatus = new JobStatusImplementation(job, "RUNNING", 0, null, false, null);
        JobStatus[] statuses = { jobstatus };
        // use `doReturn` instead of `when` as argument matching fails
        doReturn(statuses).when(jobsEngine).getJobStatuses((Job[]) any());
        JobsPoller poller = new JobsPoller(jobs, pollConf, octopus);

        poller.poll();

        assertThat(sjob.getStatus()).isEqualTo(jobstatus);
        assertThat(sjob.getPollIterations()).isEqualTo(1);
    }

    @Test
    public void testPoll_RunningState_StateUnchangedIterationIncreased() throws URISyntaxException, UnsupportedEncodingException,
            ClientProtocolException, IOException {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "11111111-1111-1111-1111-111111111111";
        Job job = new JobImplementation(mock(JobDescription.class), mock(Scheduler.class), identifier);
        JobStatus jobstatus = new JobStatusImplementation(job, "RUNNING", 0, null, false, null);
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

        poller.poll();

        assertThat(sjob.getStatus()).isEqualTo(jobstatus);
        assertThat(sjob.getPollIterations()).isEqualTo(6);
    }

    @Test
    public void testPoll_PendingState_StateUpdated() throws URISyntaxException {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "11111111-1111-1111-1111-111111111111";
        Job job = new JobImplementation(mock(JobDescription.class), mock(Scheduler.class), identifier);
        JobStatus jobstatus = new JobStatusImplementation(job, "PENDING", 0, null, false, null);
        SandboxedJob sjob = new SandboxedJob(null, job, null, null, jobstatus, 5);
        jobs.put(identifier, sjob);
        PollConfiguration pollConf = new PollConfiguration();
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        JobStatus new_jobstatus = new JobStatusImplementation(job, "RUNNING", 0, null, false, null);
        JobStatus[] statuses = { new_jobstatus };
        // use `doReturn` instead of `when` as argument matching fails
        doReturn(statuses).when(jobsEngine).getJobStatuses((Job[]) any());
        JobsPoller poller = new JobsPoller(jobs, pollConf, octopus);

        poller.poll();

        assertThat(sjob.getStatus()).isEqualTo(new_jobstatus);
        assertThat(sjob.getPollIterations()).isEqualTo(6);
    }

    @Test
    public void testPoll_RunningState_DoneOkWithCleanupSandbox() throws OctopusIOException {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "11111111-1111-1111-1111-111111111111";
        Job job = new JobImplementation(mock(JobDescription.class), mock(Scheduler.class), identifier);
        JobStatus jobstatus = new JobStatusImplementation(job, "RUNNING", 0, null, false, null);
        Sandbox sb = mock(Sandbox.class);
        SandboxedJob sjob = new SandboxedJob(sb, job, null, null, jobstatus, 5);
        jobs.put(identifier, sjob);
        PollConfiguration pollConf = new PollConfiguration();
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        JobStatus new_jobstatus = new JobStatusImplementation(job, "Done", 0, null, true, null);
        JobStatus[] statuses = { new_jobstatus };
        // use `doReturn` instead of `when` as argument matching fails
        doReturn(statuses).when(jobsEngine).getJobStatuses((Job[]) any());
        JobsPoller poller = new JobsPoller(jobs, pollConf, octopus);

        poller.poll();

        verify(sb).download();
        verify(sb).delete();
    }

    @Test
    public void testPoll_PendingStateOnCancelTimeout_JobCanceled() throws OctopusIOException, OctopusException {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "11111111-1111-1111-1111-111111111111";
        Job job = new JobImplementation(mock(JobDescription.class), mock(Scheduler.class), identifier);
        JobStatus jobstatus = new JobStatusImplementation(job, "PENDING", 0, null, false, null);
        Sandbox sb = mock(Sandbox.class);
        SandboxedJob sjob = new SandboxedJob(sb, job, null, null, jobstatus, 5);
        jobs.put(identifier, sjob);
        PollConfiguration pollConf = new PollConfiguration(1, 5, 10);
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        JobStatus new_jobstatus = new JobStatusImplementation(job, "RUNNING", 0, null, false, null);
        JobStatus[] statuses = { new_jobstatus };
        // use `doReturn` instead of `when` as argument matching fails
        doReturn(statuses).when(jobsEngine).getJobStatuses((Job[]) any());
        JobsPoller poller = new JobsPoller(jobs, pollConf, octopus);

        poller.poll();

        verify(jobsEngine).cancelJob(job);
        verify(sb).download();
        verify(sb).delete();
    }

    @Test
    public void testPoll_PendingStateOnDeleteTimeout_JobDeleted() throws OctopusIOException, OctopusException {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "11111111-1111-1111-1111-111111111111";
        Job job = new JobImplementation(mock(JobDescription.class), mock(Scheduler.class), identifier);
        JobStatus jobstatus = new JobStatusImplementation(job, "PENDING", 0, null, false, null);
        Sandbox sb = mock(Sandbox.class);
        SandboxedJob sjob = new SandboxedJob(sb, job, null, null, jobstatus, 5);
        jobs.put(identifier, sjob);
        PollConfiguration pollConf = new PollConfiguration(1, 2, 5);
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        JobStatus new_jobstatus = new JobStatusImplementation(job, "RUNNING", 0, null, false, null);
        JobStatus[] statuses = { new_jobstatus };
        // use `doReturn` instead of `when` as argument matching fails
        doReturn(statuses).when(jobsEngine).getJobStatuses((Job[]) any());
        JobsPoller poller = new JobsPoller(jobs, pollConf, octopus);

        poller.poll();

        verify(jobsEngine).cancelJob(job);
        verify(sb).download();
        verify(sb).delete();
        assertThat(jobs).doesNotContainKey(identifier);
    }

    @Test
    public void testPoll_DoneState_JobStatusNotCalledAndIterationIncreased() {
        Map<String, SandboxedJob> jobs = new HashMap<String, SandboxedJob>();
        String identifier = "11111111-1111-1111-1111-111111111111";
        Job job = new JobImplementation(mock(JobDescription.class), mock(Scheduler.class), identifier);
        JobStatus jobstatus = new JobStatusImplementation(job, "DONE", 0, null, true, null);
        SandboxedJob sjob = new SandboxedJob(null, job, null, null, jobstatus, 5);
        jobs.put(identifier, sjob);
        PollConfiguration pollConf = new PollConfiguration();
        Octopus octopus = mock(Octopus.class);
        Jobs jobsEngine = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobsEngine);
        JobsPoller poller = new JobsPoller(jobs, pollConf, octopus);

        poller.poll();

        verify(jobsEngine, never()).getJobStatuses(any(Job.class));
        assertThat(sjob.getPollIterations()).isEqualTo(6);
    }

    @Test
    public void testCancelJob() throws OctopusIOException, OctopusException {
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
    public void testCleanSandbox() throws OctopusIOException {
        Octopus octopus = mock(Octopus.class);
        JobsPoller poller = new JobsPoller(null, null, octopus);
        SandboxedJob job = mock(SandboxedJob.class);
        Sandbox sb = mock(Sandbox.class);
        when(job.getSandbox()).thenReturn(sb);

        poller.cleanSandbox(job);

        verify(sb).download();
        verify(sb).delete();
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

}