package nl.esciencecenter.octopus.webservice.api;

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


}
