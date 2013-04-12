package nl.esciencecenter.octopus.webservice.job;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;

import nl.esciencecenter.octopus.webservice.job.OctopusConfiguration;
import nl.esciencecenter.octopus.webservice.job.OctopusManager;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.ResourceBroker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableMap;

/**
 * GATManager calls static methods of org.gridlab.gat.GAT.
 * Using PowerMock to test those calls.
 *
 * @author verhoes
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(GAT.class)
public class OctopusManagerTest {
    public OctopusManager sampleGATManager() throws GATObjectCreationException, URISyntaxException {
        ImmutableMap<String, Object> prefs = ImmutableMap.of("localq.max.concurrent.jobs", (Object) 1);
        OctopusConfiguration conf = new OctopusConfiguration("localq://localhost", prefs);
        return new OctopusManager(conf);
    }

    @Test
    public void testGATManager() throws GATObjectCreationException, URISyntaxException {
        GATContext context = new GATContext();
        ResourceBroker broker = mock(ResourceBroker.class);
        PowerMockito.mockStatic(GAT.class);
        when(GAT.getDefaultGATContext()).thenReturn(context);
        when(GAT.createResourceBroker(any(URI.class))).thenReturn(broker);

        sampleGATManager();

        assertThat(context.getPreferences().get("localq.max.concurrent.jobs")).isEqualTo(1);

        PowerMockito.verifyStatic();
        GAT.getDefaultGATContext();

        PowerMockito.verifyStatic();
        GAT.createResourceBroker(new URI("localq://localhost"));

        PowerMockito.verifyStatic();
        GAT.setDefaultGATContext(context);
    }

    @Test
    public void testStop() throws Exception {
        PowerMockito.mockStatic(GAT.class);
        GATContext context = new GATContext();
        when(GAT.getDefaultGATContext()).thenReturn(context);

        OctopusManager g = sampleGATManager();
        g.stop();

        PowerMockito.verifyStatic();
        GAT.end();
    }

    @Test
    public void testGetBroker() throws GATObjectCreationException, URISyntaxException {
        GATContext context = new GATContext();
        ResourceBroker broker = mock(ResourceBroker.class);
        PowerMockito.mockStatic(GAT.class);
        when(GAT.getDefaultGATContext()).thenReturn(context);
        when(GAT.createResourceBroker(any(URI.class))).thenReturn(broker);

        OctopusManager g = sampleGATManager();

        assertThat(g.getBroker()).isEqualTo(broker);
    }
}
