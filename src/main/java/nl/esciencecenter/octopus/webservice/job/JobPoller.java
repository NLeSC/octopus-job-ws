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

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.util.Sandbox;

public class JobPoller extends Thread {
    protected final static Logger logger = LoggerFactory
            .getLogger(JobPoller.class);

    private int timeout;
    private int interval;
    private Octopus octopus;
    private Job job;
    private Sandbox sandbox;
    private URI callback;
    private HttpClient httpClient;
    private JobStatus status;

    public JobPoller(int timeout, int interval, Octopus octopus, Job job, Sandbox sandbox, URI callback, HttpClient httpClient) {
        super();
        this.timeout = timeout;
        this.interval = interval;
        this.octopus = octopus;
        this.job = job;
        this.sandbox = sandbox;
        this.callback = callback;
        this.httpClient = httpClient;
        this.status = null; // fill in run
    }

    public void run() {
        int iterations = timeout / interval;
        boolean isDone = false;
        try {
            JobStatus newStatus;
            for (int i = 0; i < iterations; i++) {
                newStatus = octopus.jobs().getJobStatus(job);
                isDone = newStatus.isDone();
                // put state if changed
                if (!status.equals(newStatus)) {
                    String state = newStatus.getState();
                    putState2Callback(state);
                }
                status = newStatus;
                if (isDone) break;
                Thread.sleep(interval);
            }
        } catch (OctopusException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!isDone) {
            try {
                octopus.jobs().cancelJob(job);
            } catch (OctopusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                putState2Callback("ERROR, Timeout");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            sandbox.download();
            sandbox.delete();
        } catch (OctopusIOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param state
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws ClientProtocolException
     */
    public void putState2Callback(String state) throws UnsupportedEncodingException, IOException, ClientProtocolException {
        HttpPut put = new HttpPut(callback);
        put.setEntity(new StringEntity(state));
        HttpResponse response = httpClient.execute(put);
        logger.info("Send '" + state + "' to " + callback
                + " returned " + response.getStatusLine());
    }
}
