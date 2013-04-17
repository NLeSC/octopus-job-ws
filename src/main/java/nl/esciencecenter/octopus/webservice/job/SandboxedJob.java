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

import com.google.common.base.Objects;

import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.util.Sandbox;

public class SandboxedJob {
    protected final static Logger logger = LoggerFactory
            .getLogger(SandboxedJob.class);

    private final Sandbox sandbox;
    private final Job job;
    private final URI callback;
    private final HttpClient httpClient;
    private JobStatus status = null;
    private int pollIterations = 0;

    public SandboxedJob(Sandbox sandbox, Job job, URI callback, HttpClient httpClient) {
        super();
        this.sandbox = sandbox;
        this.job = job;
        this.callback = callback;
        this.httpClient = httpClient;
    }

    protected SandboxedJob(Sandbox sandbox, Job job, URI callback, HttpClient httpClient, JobStatus status, int pollIterations) {
        super();
        this.sandbox = sandbox;
        this.job = job;
        this.callback = callback;
        this.httpClient = httpClient;
        this.status = status;
        this.pollIterations = pollIterations;
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

    public JobStatus getStatus() {
        return status;
    }

    public int getPollIterations() {
        return pollIterations;
    }

    public void setPollIterations(int polliterations) {
        this.pollIterations = polliterations;
    }

    /**
     * Increase poll iteration
     */
    public void incrPollIterations() {
        this.pollIterations++;
    }

    public void setStatus(JobStatus status) throws UnsupportedEncodingException, ClientProtocolException, IOException {
        if (!Objects.equal(this.status, status)) {
            this.status = status;
            putState2Callback(status.getState());
        }
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
