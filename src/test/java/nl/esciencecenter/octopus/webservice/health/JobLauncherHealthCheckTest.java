package nl.esciencecenter.octopus.webservice.health;

import static org.junit.Assert.*;

import nl.esciencecenter.octopus.webservice.health.JobLauncherHealthCheck;

import org.junit.Test;

import com.yammer.metrics.core.HealthCheck.Result;

public class JobLauncherHealthCheckTest {

    @Test
    public void testCheck() throws Exception {
        JobLauncherHealthCheck hc = new JobLauncherHealthCheck("gat");
        assertEquals(Result.healthy(), hc.check());
    }

}
