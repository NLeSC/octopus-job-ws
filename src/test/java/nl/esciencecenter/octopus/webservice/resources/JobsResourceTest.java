package nl.esciencecenter.octopus.webservice.resources;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;
import nl.esciencecenter.octopus.webservice.api.JobSubmitResponse;
import nl.esciencecenter.octopus.webservice.job.OctopusManager;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

public class JobsResourceTest {

    @Test
    public void testSubmitJob() throws Exception {
        JobSubmitRequest request = mock(JobSubmitRequest.class);
        OctopusManager manager = mock(OctopusManager.class);
        Job job = mock(Job.class);
        HttpClient httpClient = new DefaultHttpClient();
        when(manager.submitJob(request, httpClient)).thenReturn(job);
        when(job.getIdentifier()).thenReturn("1234");

        JobsResource resource = new JobsResource(manager, httpClient);

        JobSubmitResponse response = resource.submitJob(request);

        assertEquals(response.jobid, "1234");
    }

}
