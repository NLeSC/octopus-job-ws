package nl.esciencecenter.osmium.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import nl.esciencecenter.osmium.api.JobStatusResponse;


public class CallbackClientTest {

    @Test
    public void testPutState() throws URISyntaxException, ClientProtocolException, IOException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpContext context = new BasicHttpContext();
        CallbackClient client = new CallbackClient(httpClient, context);
        URI uri = new URI("https://www.example.com/job/1234/status");
        JobStatusResponse status = new JobStatusResponse("RUNNING", true, false, null, null, null);

        client.putState(uri, status);

        ArgumentCaptor<HttpUriRequest> argument = ArgumentCaptor.forClass(HttpUriRequest.class);
        verify(httpClient).execute(argument.capture(), eq(context));
        HttpPut request = (HttpPut) argument.getValue();
        assertThat(request.getURI()).isEqualTo(uri);
        assertThat(request.getEntity().getContentType().getValue()).isEqualTo(ContentType.APPLICATION_JSON.toString());
        assertThat(request.getEntity().getContentLength()).isEqualTo(status.toJson().length());
    }
}
