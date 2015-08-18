package nl.esciencecenter.osmium.callback;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;

public class BasicCredentialTest {
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private BasicCredential cred;

    @Before
    public void setUp() {
        cred = new BasicCredential("someone", "mypassword", "http", "somehost", 8080);
    }

    @Test
    public void testSerialization() throws IOException {
        String result = MAPPER.writeValueAsString(cred);

        String expected = "{\"username\":\"someone\",\"password\":\"mypassword\",\"scheme\":\"http\",\"hostname\":\"somehost\",\"port\":8080}";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testDeserialization() throws IOException {
        String input = "{\"username\":\"someone\",\"password\":\"mypassword\",\"scheme\":\"http\",\"hostname\":\"somehost\",\"port\":8080}";

        BasicCredential result = MAPPER.readValue(input, BasicCredential.class);
        assertThat(result).isEqualTo(cred);
    }

    @Test
    public void testGetHttpHost() {
        HttpHost result = cred.getHttpHost();

        HttpHost expected = new HttpHost("somehost", 8080, "http");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testGetAuthScope() {
        AuthScope result = cred.getAuthScope();

        AuthScope expected = new AuthScope("somehost", 8080);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testGetUsernamePasswordCredentials() {
        UsernamePasswordCredentials result = cred.getUsernamePasswordCredentials();

        UsernamePasswordCredentials expected = new UsernamePasswordCredentials("someone", "mypassword");
        assertThat(result).isEqualTo(expected);
    }
}
