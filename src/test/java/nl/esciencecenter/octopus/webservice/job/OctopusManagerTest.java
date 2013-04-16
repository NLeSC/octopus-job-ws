package nl.esciencecenter.octopus.webservice.job;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.util.Sandbox;
import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;
import nl.esciencecenter.octopus.webservice.job.OctopusConfiguration;
import nl.esciencecenter.octopus.webservice.job.OctopusManager;

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

    public OctopusManager sampleGATManager() throws URISyntaxException, OctopusIOException, OctopusException {
        ImmutableMap<String, Object> prefs = ImmutableMap.of("octopus.adaptors.local.queue.multi.maxConcurrentJobs", (Object) 1);
        OctopusConfiguration conf =
                new OctopusConfiguration(new URI("local:///"), "multi", new URI("file:///tmp/sandboxes"), prefs);
        return new OctopusManager(conf);
    }

    @Test
    public void testGATManager() throws URISyntaxException, OctopusException, OctopusIOException {
        PowerMockito.mockStatic(OctopusFactory.class);
        Octopus octopus = mock(Octopus.class);
        Jobs jobs = mock(Jobs.class);
        when(octopus.jobs()).thenReturn(jobs);
        when(OctopusFactory.newOctopus(any(Properties.class))).thenReturn(octopus);

        sampleGATManager();

        // verify octopus created
        PowerMockito.verifyStatic();
        Properties expected_props = new Properties();
        expected_props.setProperty("octopus.adaptors.local.queue.multi.maxConcurrentJobs", "1");
        OctopusFactory.newOctopus(expected_props);

        // verify scheduler created
        when(jobs.newScheduler(new URI("local:///"), null, expected_props));
    }

    @Test
    public void testStop() throws Exception {
        OctopusConfiguration configuration = new OctopusConfiguration();
        Octopus octopus = mock(Octopus.class);
        Scheduler scheduler = mock(Scheduler.class);
        OctopusManager manager = new OctopusManager(configuration, octopus, scheduler);

        manager.stop();

        verify(octopus).end();
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
        when(files.newFileSystem(new URI("file:///tmp/sandboxes"), null, null)).thenReturn(filesystem);
        when(files.newPath(filesystem, new RelativePath("/tmp/sandboxes"))).thenReturn(sandboxPath);
        JobSubmitRequest request = mock(JobSubmitRequest.class);
        JobDescription description = mock(JobDescription.class);
        when(request.toJobDescription(octopus)).thenReturn(description);
        Sandbox sandbox = mock(Sandbox.class);
        when(request.toSandbox(octopus, sandboxPath, null)).thenReturn(sandbox);
        when(sandbox.getPath()).thenReturn(sandboxPath);
        when(sandboxPath.getPath()).thenReturn("/tmp/sandboxes");
        HttpClient httpClient = mock(HttpClient.class);
        Job job = mock(Job.class);
        when(job.getIdentifier()).thenReturn("11111111-1111-1111-1111-111111111111");
        when(jobs.submitJob(scheduler, description)).thenReturn(job);

        OctopusManager manager = new OctopusManager(conf, octopus, scheduler);
        Job result = manager.submitJob(request, httpClient);

        assertThat(result.getIdentifier()).isEqualTo("11111111-1111-1111-1111-111111111111");
        verify(sandbox).upload();
        verify(jobs.submitJob(scheduler, description));
    }

    @Test
    public void testStateOfJob() {
        fail("Not yet implemented");
    }

    @Test
    public void testCancelJob() {
        fail("Not yet implemented");
    }
}
