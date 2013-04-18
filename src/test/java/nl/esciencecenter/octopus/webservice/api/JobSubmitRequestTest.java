package nl.esciencecenter.octopus.webservice.api;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;
import org.junit.Before;
import org.junit.Test;

import static com.yammer.dropwizard.testing.JsonHelpers.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

public class JobSubmitRequestTest {
    private JobSubmitRequest request;

    @Before
    public void setUp() {
        request = sampleRequest();
    }

    @Test
    public void testJobSubmitRequest_AllArguments() {
        assertNotNull(request);
    }

    @Test
    public void testGetStatus_callback_url() throws URISyntaxException {
        assertEquals(request.getStatus_callback_url(), new URI("http://localhost/status"));
    }

    @Test
    public void testJobSubmitRequest() {
        assertNotNull(new JobSubmitRequest());
    }

    private JobSubmitRequest sampleRequest() {
        List<String> arguments = new ArrayList<String>();
        arguments.add("runme.sh");
        List<String> prestaged = new ArrayList<String>();
        prestaged.add("runme.sh");
        prestaged.add("input.dat");
        List<String> poststaged = new ArrayList<String>();
        poststaged.add("output.dat");
        URI cb = null;
        try {
            cb = new URI("http://localhost/status");
        } catch (URISyntaxException e) {
        }
        return new JobSubmitRequest("/tmp/jobdir/", "/bin/sh", arguments, prestaged, poststaged, "stderr.txt", "stdout.txt", cb);
    }

    @Test
    public void serializesToJSON() throws IOException {
        assertThat("a JobSubmitRequest can be serialized to JSON", asJson(request),
                is(equalTo(jsonFixture("fixtures/request.json"))));
    }

    @Test
    public void deserializesFromJSON() throws IOException {
        assertThat("a JobSubmitRequest can be deserialized from JSON",
                fromJson(jsonFixture("fixtures/request.json"), JobSubmitRequest.class), is(request));
    }

    @Test
    public void deserializedFromJson_WithoutCallback() throws IOException {
        request.status_callback_url = null;
        assertThat("a JobSubmitRequest can be deserialized from JSON",
                fromJson(jsonFixture("fixtures/request.nocallback.json"), JobSubmitRequest.class), is(request));
    }

    @Test
    public void testEquals() {
        assertThat(request.equals(null)).isFalse();

        assertThat(request.equals("string")).isFalse();

        assertTrue(request.equals(request));

        assertTrue(request.equals(sampleRequest()));

        JobSubmitRequest r2 = sampleRequest();
        r2.executable = "/bin/bash";
        assertFalse(request.equals(r2));

        JobSubmitRequest r3 = sampleRequest();
        r3.jobdir = "/tmp/jobdir2";
        assertFalse(request.equals(r3));

        JobSubmitRequest r4 = sampleRequest();
        r4.stderr = "error.log";
        assertFalse(request.equals(r4));

        JobSubmitRequest r5 = sampleRequest();
        r5.stdout = "out.log";
        assertFalse(request.equals(r5));

        JobSubmitRequest r6 = sampleRequest();
        r6.arguments.add("bla");
        assertFalse(request.equals(r6));

        JobSubmitRequest r7 = sampleRequest();
        r7.prestaged.add("somefile");
        assertFalse(request.equals(r7));

        JobSubmitRequest r8 = sampleRequest();
        r8.poststaged.add("somefile");
        assertFalse(request.equals(r8));

        try {
            JobSubmitRequest r9 = sampleRequest();
            r9.status_callback_url = new URI("http://example.com");
            assertFalse(request.equals(r9));
        } catch (URISyntaxException e) {
            fail();
        }
    }

    @Test
    public void testToString() {
        String s =
                "JobSubmitRequest{jobdir=/tmp/jobdir/, executable=/bin/sh, stderr=stderr.txt, stdout=stdout.txt, arguments=[runme.sh], prestaged=[runme.sh, input.dat], poststaged=[output.dat], status_callback_url=http://localhost/status}";
        assertEquals(s, request.toString());
    }
}