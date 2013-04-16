package nl.esciencecenter.octopus.webservice.job;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.JobStatus;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobPoller extends Thread {
    protected final static Logger logger = LoggerFactory
            .getLogger(JobPoller.class);

    private SandboxedJob job;
    private PollConfiguration pollConfiguration;
    private Octopus octopus;
    private JobStatus status = null;

    public JobPoller(SandboxedJob sjob, PollConfiguration pollConfiguration, Octopus octopus) {
        this.pollConfiguration = pollConfiguration;
        this.octopus = octopus;
        this.job = sjob;
    }

    public void run() {
        int interval = pollConfiguration.getInterval();
        int timeout = pollConfiguration.getTimeout();
        int iterations = timeout / interval;
        boolean isDone = false;
        try {
            JobStatus newStatus;
            for (int i = 0; i < iterations; i++) {
                newStatus = octopus.jobs().getJobStatus(job.getJob());
                isDone = newStatus.isDone();
                // put state if changed
                if (!status.equals(newStatus)) {
                    String state = newStatus.getState();
                    job.putState2Callback(state);
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
                octopus.jobs().cancelJob(job.getJob());
            } catch (OctopusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (OctopusIOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                job.putState2Callback("ERROR, Timeout");
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
            job.getSandbox().download();
            job.getSandbox().delete();
        } catch (OctopusIOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TODO remove job from OctopusManager.jobs
    }
}
