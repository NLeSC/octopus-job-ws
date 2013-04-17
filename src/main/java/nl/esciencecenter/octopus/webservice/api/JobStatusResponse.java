package nl.esciencecenter.octopus.webservice.api;

import java.util.Map;

import javax.validation.constraints.NotNull;

import nl.esciencecenter.octopus.jobs.JobStatus;

public class JobStatusResponse {
    @NotNull
    private final String state;
    @NotNull
    private final Integer exitCode;
    private final Exception exception;
    @NotNull
    private final boolean done;
    private final Map<String, String> schedulerSpecficInformation;

    public JobStatusResponse(JobStatus jobStatus) {
        state = jobStatus.getState();
        exitCode = jobStatus.getExitCode();
        exception = jobStatus.getException();
        done = jobStatus.isDone();
        schedulerSpecficInformation = jobStatus.getSchedulerSpecficInformation();
    }

    public JobStatusResponse(String state, boolean done, Integer exitCode, Exception exception,
            Map<String, String> schedulerSpecficInformation) {
        super();
        this.state = state;
        this.done = done;
        this.exitCode = exitCode;
        this.exception = exception;
        this.schedulerSpecficInformation = schedulerSpecficInformation;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public String getState() {
        return state;
    }

    public Exception getException() {
        return exception;
    }

    public boolean isDone() {
        return done;
    }

    public Map<String, String> getSchedulerSpecficInformation() {
        return schedulerSpecficInformation;
    }
}
