package nl.esciencecenter.osmium.callback;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import io.dropwizard.client.HttpClientConfiguration;

public class CallbackConfigurationTest {
    @Test
    public void testGetHttpClientConfiguration() {
        HttpClientConfiguration expected = new HttpClientConfiguration();
        ImmutableList<BasicCredential> basicCredentials = ImmutableList.of();
        CallbackConfiguration conf = new CallbackConfiguration(expected, basicCredentials, false);

        assertThat(conf.getHttpClientConfiguration()).isEqualTo(expected);
    }

    public CallbackConfiguration getSampleWithCreds() {
        ImmutableList<BasicCredential> basicCredentials = ImmutableList
                .of(new BasicCredential("someone", "mypassword", "https", "example.com", 443));
        return new CallbackConfiguration(new HttpClientConfiguration(), basicCredentials, false);
    }

    @Test
    public void testGetPreemptiveContext_filledCredProv() {
        CallbackConfiguration conf = getSampleWithCreds();

        HttpClientContext context = conf.getPreemptiveContext();
        CredentialsProvider credsProvider = context.getCredentialsProvider();

        UsernamePasswordCredentials expected = new UsernamePasswordCredentials("someone", "mypassword");
        assertThat(credsProvider.getCredentials(new AuthScope("example.com", 443))).isEqualTo(expected);
    }

    @Test
    public void testGetPreemptiveContext_filledAuthCache() {
        CallbackConfiguration conf = getSampleWithCreds();

        HttpClientContext context = conf.getPreemptiveContext();
        AuthCache authCache = context.getAuthCache();

        assertThat(authCache.get(new HttpHost("example.com", 443, "https")).getSchemeName()).isEqualTo(new BasicScheme().getSchemeName());
    }
}
