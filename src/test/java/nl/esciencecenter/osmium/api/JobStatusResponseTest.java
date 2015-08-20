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


import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import nl.esciencecenter.xenon.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class JobStatusResponseTest {
    private JobStatusResponse getRunningJobStatus() {
        Map<String, String> info = new HashMap<>(2);
        info.put("status", "EXECUTING");
        JobStatusResponse status = new JobStatusResponse("RUNNING", true, false, null, null, info);
        return status;
    }

    @Test
    public void construct_JobStatus() {
        Job job = mock(Job.class);
        JobStatus jobstatus = new JobStatusImplementation(job, "PENDING", null, null, false, false, null);
        JobStatusResponse status = new JobStatusResponse(jobstatus);

        JobStatusResponse expected = new JobStatusResponse("PENDING", false, false, null, null, null);
        assertThat(status).isEqualTo(expected);
    }

    @Test
    public void construct_Null() {
        JobStatusResponse status = new JobStatusResponse(null);

        assertThat(status.getState()).isEqualTo("INITIAL");
        assertThat(status.getException()).isNull();
        assertThat(status.getSchedulerSpecficInformation()).isNull();
        assertThat(status.getExitCode()).isNull();
        assertThat(status.isDone()).isFalse();
        assertThat(status.isRunning()).isFalse();
    }

    @Test
    public void test_hashCode() {
        JobStatusResponse status = getRunningJobStatus();

        assertThat(status.hashCode()).isEqualTo(-481497476);
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void testEqual_null_false() {
        JobStatusResponse status = getRunningJobStatus();

        assertThat(status.equals(null)).isFalse();
    }

    @Test
    public void testEqual_sameInstance_true() {
        JobStatusResponse status = getRunningJobStatus();

        assertThat(status.equals(status)).isTrue();
    }

    @Test
    public void testEqual_sameConstruct_true() {
        JobStatusResponse status = getRunningJobStatus();
        JobStatusResponse expected = getRunningJobStatus();

        assertThat(status.equals(expected)).isTrue();
    }

    @Test
    @SuppressWarnings("IncompatibleEquals")
    public void testEqual_diffClass_false() {
        JobStatusResponse status = getRunningJobStatus();

        assertThat(status.equals(42)).isFalse();
    }

    @Test
    public void testEqual_otherState_false() {
        JobStatusResponse status = getRunningJobStatus();
        Map<String, String> info = new HashMap<>(2);
        info.put("status", "EXECUTING");
        JobStatusResponse expected = new JobStatusResponse("EXECUTING", true, false, null, null, info);

        assertThat(status.equals(expected)).isFalse();
    }

    @Test
    public void testEqual_otherNotRunning_false() {
        JobStatusResponse status = getRunningJobStatus();
        Map<String, String> info = new HashMap<>(2);
        info.put("status", "EXECUTING");
        JobStatusResponse expected = new JobStatusResponse("RUNNING", false, false, null, null, info);

        assertThat(status.equals(expected)).isFalse();
    }

    @Test
    public void testEqual_otherDone_false() {
        JobStatusResponse status = getRunningJobStatus();
        Map<String, String> info = new HashMap<>(2);
        info.put("status", "EXECUTING");
        JobStatusResponse expected = new JobStatusResponse("RUNNING", true, true, null, null, info);

        assertThat(status.equals(expected)).isFalse();
    }

    @Test
    public void testEqual_otherExitCode_false() {
        JobStatusResponse status = getRunningJobStatus();
        Map<String, String> info = new HashMap<>(2);
        info.put("status", "EXECUTING");
        JobStatusResponse expected = new JobStatusResponse("RUNNING", true, false, 0, null, info);

        assertThat(status.equals(expected)).isFalse();
    }

    @Test
    public void testEqual_otherException_false() {
        JobStatusResponse status = getRunningJobStatus();
        Map<String, String> info = new HashMap<>(2);
        info.put("status", "EXECUTING");
        Exception exception = new Exception();
        JobStatusResponse expected = new JobStatusResponse("RUNNING", true, false, null, exception, info);

        assertThat(status.equals(expected)).isFalse();
    }

    @Test
    public void testEqual_otherSchedulerInfo_false() {
        JobStatusResponse status = getRunningJobStatus();
        Map<String, String> info = new HashMap<>(2);
        info.put("status", "r");
        JobStatusResponse expected = new JobStatusResponse("RUNNING", true, false, null, null, info);

        assertThat(status.equals(expected)).isFalse();
    }

    @Test
    public void test_toString() {
        JobStatusResponse status = getRunningJobStatus();

        String expected = "JobStatusResponse{RUNNING, true, false, null, null, {status=EXECUTING}}";
        assertThat(status.toString()).isEqualTo(expected);
    }

    @Test
    public void toJson_Done() throws IOException, URISyntaxException {
        String state = "DONE";
        Boolean done = true;
        int exitCode = 0;
        Exception exception = null;
        String scheduler_status = "STOPPED";
        Map<String, String> info = new HashMap<>(2);
        info.put("status", scheduler_status);
        JobStatusResponse status = new JobStatusResponse(state, false, done, exitCode, exception, info);
        assertThat(status.toJson()).isEqualTo(fixture("fixtures/status.done.json"));
    }

    @Test
    public void toJson_Null() throws IOException {
        JobStatusResponse status = new JobStatusResponse(null);

        assertThat(status.toJson()).isEqualTo(fixture("fixtures/status.initial.json"));
    }

    @Test
    public void toJson_Exception() throws IOException, URISyntaxException {
        Exception exception = new Exception("Process cancelled by user.");
        JobStatusResponse status = new JobStatusResponse("KILLED", false, true, null, exception , null);
        assertThat(status.toJson()).isEqualTo(fixture("fixtures/status.exception.json"));
    }
}
