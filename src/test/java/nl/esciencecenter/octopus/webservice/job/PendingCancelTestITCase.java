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

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;
import nl.esciencecenter.octopus.webservice.api.SandboxedJob;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class PendingCancelTestITCase {
    protected static final Logger LOGGER = LoggerFactory.getLogger(PendingCancelTestITCase.class);

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private OctopusManager manager;

    private HttpClient httpClient = new DefaultHttpClient();

    @Test
    public void test() throws Exception {
        URI scheduler = new URI("local:///");
        PollConfiguration pollConfiguration = new PollConfiguration();
        ImmutableMap<String, String> preferences =
                ImmutableMap.of("octopus.adaptors.local.queue.multi.maxConcurrentJobs", "1");
        URI sandboxRoot = testFolder.newFolder("sandboxes").toURI();
        String queue = "multi";
        OctopusConfiguration configuration =
                new OctopusConfiguration(scheduler, queue, sandboxRoot, preferences, pollConfiguration);

        manager = new OctopusManager(configuration);

        testFolder.create();
        String jobdir = testFolder.newFolder("job1").getPath();
        List<String> arguments = new ArrayList<String>();
        arguments.add("60");
        JobSubmitRequest submit =
                new JobSubmitRequest(jobdir, "/bin/sleep", arguments, new ArrayList<String>(), new ArrayList<String>(),
                        "stderr.txt", "stdout.txt", null);

        // when 1 job is submmited -> test passes,
        // when 2 jobs are submitted and second cancelled -> test fails.
        manager.submitJob(submit, httpClient);
        SandboxedJob job = manager.submitJob(submit, httpClient);

        manager.start();
        Thread.sleep(500); // allow poller to update status

        manager.cancelJob(job.getIdentifier());

        SandboxedJob job_out = manager.getJob(job.getIdentifier());

        assertEquals("KILLED", job_out.getStatus().getState());

        manager.stop();
    }
}
