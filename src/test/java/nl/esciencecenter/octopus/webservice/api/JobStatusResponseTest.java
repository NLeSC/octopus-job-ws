package nl.esciencecenter.octopus.webservice.api;

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

import static com.yammer.dropwizard.testing.JsonHelpers.asJson;
import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class JobStatusResponseTest {
    /**
     * @param state
     * @param exitCode
     * @param exception
     * @param scheduler_status
     * @param done
     * @return
     * @throws URISyntaxException
     */
    public JobStatusResponse sampleStatus(String state, int exitCode, Exception exception, String scheduler_status, Boolean done)
            throws URISyntaxException {
        Map<String, String> info = new HashMap<String, String>();
        info.put("status", scheduler_status);
        return new JobStatusResponse(state, done, exitCode, exception, info);
    }

    @Test
    public void serializesToJSON_Done() throws IOException, URISyntaxException {
        String state = "DONE";
        int exitCode = 0;
        Exception exception = null;
        String scheduler_status = "STOPPED";
        Boolean done = true;
        JobStatusResponse status = sampleStatus(state, exitCode, exception, scheduler_status, done);
        assertThat(asJson(status)).isEqualTo(jsonFixture("fixtures/status.done.json"));
    }

    @Test
    public void serializeToJson_Null() throws IOException {
        JobStatusResponse status = new JobStatusResponse(null);

        assertThat(asJson(status)).isEqualTo(jsonFixture("fixtures/status.initial.json"));
    }

}
