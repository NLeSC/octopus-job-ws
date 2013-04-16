package nl.esciencecenter.octopus.webservice.job;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.util.Sandbox;

public class SandboxedJob {
    protected final static Logger logger = LoggerFactory
            .getLogger(SandboxedJob.class);

    private final Sandbox sandbox;
    private final Job job;
    private final URI callback;
    private final HttpClient httpClient;

    public SandboxedJob(Sandbox sandbox, Job job, URI callback, HttpClient httpClient) {
        super();
        this.sandbox = sandbox;
        this.job = job;
        this.callback = callback;
        this.httpClient = httpClient;
    }

    public Sandbox getSandbox() {
        return sandbox;
    }

    public Job getJob() {
        return job;
    }

    public URI getCallback() {
        return callback;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * @param state
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws ClientProtocolException
     */
    public void putState2Callback(String state) throws UnsupportedEncodingException, IOException, ClientProtocolException {
        if (callback != null) {
            HttpPut put = new HttpPut(callback);
            put.setEntity(new StringEntity(state));
            HttpResponse response = httpClient.execute(put);
            logger.info("Send '" + state + "' to " + callback
                    + " returned " + response.getStatusLine());
        }
    }
}
