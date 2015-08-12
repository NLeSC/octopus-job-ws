package nl.esciencecenter.osmium.callback;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;

public class BasicCredentialTest {
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void test() throws IOException {
        BasicCredential bs = new BasicCredential("someone", "mypassword", "http", "somehost", 8080);

        String result = MAPPER.writeValueAsString(bs);
        BasicCredential expected = MAPPER.readValue(result, BasicCredential.class);
        assertThat(bs).isEqualTo(expected);
    }

}
