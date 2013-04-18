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
import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;
import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;

import nl.esciencecenter.octopus.webservice.api.JobSubmitResponse;

import org.junit.Test;

public class JobSubmitResponseTest {

    private JobSubmitResponse sampleResponse() {
        return new JobSubmitResponse("1234");
    }

    @Test
    public void testJobSubmitResponseString() {
        assertEquals("1234", sampleResponse().jobid);
    }

    @Test
    public void testJobSubmitResponse() {
        JobSubmitResponse r = new JobSubmitResponse();
        assertNull(r.jobid);
    }

    @Test
    public void serializesToJSON() throws IOException {
        assertThat("a JobSubmitResponse can be serialized to JSON",
                asJson(sampleResponse()),
                is(equalTo(jsonFixture("fixtures/response.json"))));
    }

    @Test
    public void deserializesFromJSON() throws IOException {
        assertThat(
                "a JobSubmitResponse can be deserialized from JSON",
                fromJson(jsonFixture("fixtures/response.json"),
                        JobSubmitResponse.class), is(sampleResponse()));
    }

    @Test
    public void hasAWorkingEqualsMethod() throws Exception {
        JobSubmitResponse response = sampleResponse();
        assertThat(response.equals(response)).isTrue();

        assertThat(response.equals(new JobSubmitResponse("1234"))).isTrue();

        assertThat(response.equals(null)).isFalse();

        assertThat(response.equals("string")).isFalse();

        assertThat(response.equals(new JobSubmitResponse("u1"))).isFalse();
    }

}
