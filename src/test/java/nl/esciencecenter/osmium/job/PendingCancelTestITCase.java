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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.osmium.api.JobSubmitRequest;
import nl.esciencecenter.osmium.api.SandboxedJob;
import nl.esciencecenter.osmium.job.XenonConfiguration;
import nl.esciencecenter.osmium.job.XenonManager;
import nl.esciencecenter.osmium.job.PollConfiguration;
import nl.esciencecenter.osmium.job.SandboxConfiguration;
import nl.esciencecenter.osmium.job.SchedulerConfiguration;

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

    private XenonManager manager;

    private HttpClient httpClient = new DefaultHttpClient();

    @Test
    public void test() throws Exception {
        PollConfiguration pollConfiguration = new PollConfiguration();
        ImmutableMap<String, String> preferences =
                ImmutableMap.of("xenon.adaptors.local.queue.multi.maxConcurrentJobs", "1");
        SchedulerConfiguration scheduler = new SchedulerConfiguration("local", null, "multi", null);
        String sandboxRoot = testFolder.newFolder("sandboxes").getAbsolutePath();
        SandboxConfiguration sandbox = new SandboxConfiguration("file", null, sandboxRoot, null);

        XenonConfiguration configuration =
                new XenonConfiguration(scheduler, sandbox, preferences, pollConfiguration);

        manager = new XenonManager(configuration);

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
