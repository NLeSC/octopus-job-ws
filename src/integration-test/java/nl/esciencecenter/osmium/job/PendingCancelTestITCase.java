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
import java.util.HashMap;
import java.util.List;

import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import nl.esciencecenter.osmium.api.JobSubmitRequest;
import nl.esciencecenter.osmium.api.SandboxedJob;
import nl.esciencecenter.osmium.callback.CallbackClient;

public class PendingCancelTestITCase {
    protected static final Logger LOGGER = LoggerFactory.getLogger(PendingCancelTestITCase.class);

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private XenonManager manager;

    private final CallbackClient callbackClient = new CallbackClient(HttpClients.createDefault(), new BasicHttpContext());

    @Test
    public void test() throws Exception {
        PollConfiguration pollConfiguration = new PollConfiguration();
        ImmutableMap<String, String> preferences =
                ImmutableMap.of("xenon.adaptors.local.queue.multi.maxConcurrentJobs", "1");
        SchedulerConfiguration scheduler = new SchedulerConfiguration("local", null, "multi", null);
        String sandboxRoot = testFolder.newFolder("sandboxes").getAbsolutePath();
        SandboxConfiguration sandbox = new SandboxConfiguration("file", "/", sandboxRoot, null);

        XenonConfiguration configuration =
                new XenonConfiguration(scheduler, sandbox, "default", preferences, pollConfiguration);

        manager = new XenonManager(configuration);

        testFolder.create();
        String jobdir = testFolder.newFolder("job1").getPath();
        List<String> arguments = new ArrayList<String>();
        arguments.add("60");
        JobSubmitRequest submit =
                new JobSubmitRequest(null, jobdir, "/bin/sleep", arguments, new ArrayList<String>(), new ArrayList<String>(),
                        "stderr.txt", "stdout.txt", new HashMap<String, String>(), null, -1);

        // when 1 job is submmited -> test passes,
        // when 2 jobs are submitted and second cancelled -> test fails.
        manager.submitJob(submit, callbackClient);
        SandboxedJob job = manager.submitJob(submit, callbackClient);

        manager.start();
        Thread.sleep(500); // allow poller to update status

        manager.cancelJob(job.getIdentifier());

        SandboxedJob job_out = manager.getJob(job.getIdentifier());

        assertEquals("KILLED", job_out.getStatus().getState());

        manager.stop();
    }
}
