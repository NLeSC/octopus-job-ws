package nl.esciencecenter.octopus.webservice.api;

import java.util.Map;

import javax.validation.constraints.NotNull;

import nl.esciencecenter.octopus.jobs.JobStatus;

import com.google.common.base.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hashCode(state, done, exitCode, exception, schedulerSpecficInformation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JobStatusResponse other = (JobStatusResponse) obj;
        return Objects.equal(this.state, other.state) && Objects.equal(this.done, other.done)
                && Objects.equal(this.exitCode, other.exitCode) && Objects.equal(this.exception, other.exception)
                && Objects.equal(this.schedulerSpecficInformation, other.schedulerSpecficInformation);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .addValue(this.state)
                .addValue(this.done)
                .addValue(this.exitCode)
                .addValue(this.exception)
                .addValue(this.schedulerSpecficInformation)
                .toString();
    }
}
