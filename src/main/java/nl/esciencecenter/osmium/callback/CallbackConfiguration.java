package nl.esciencecenter.osmium.callback;


import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import io.dropwizard.client.HttpClientConfiguration;
import nl.esciencecenter.osmium.JobLauncherConfiguration;


public class CallbackConfiguration {
    /**
     * List of preemptive Basic credentials
     * The auth cache will be filled with these credentials.
     * The callback url does not need to have credentials encode in it and
     * the callback server does not need to give a auth challenge.
     */
    @JsonProperty
    private ImmutableList<BasicCredential> basicCredentials = ImmutableList.of();

    /**
     * Http client used to PUT state changes of jobs the launcher is monitoring to the status callback url of the job
     */
    @Valid
    @NotNull
    @JsonProperty
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    /**
     * Http Client can be configured so self signed ssl certificates work.
     */
    @Valid
    @NotNull
    @JsonProperty
    private Boolean useInsecureSSL = false;

    public CallbackConfiguration() {
    }

    public CallbackConfiguration(HttpClientConfiguration httpClient, ImmutableList<BasicCredential> basicCredentials,
            Boolean useInsecureSSL) {
        this.httpClient = httpClient;
        this.basicCredentials = basicCredentials;
        this.useInsecureSSL = useInsecureSSL;
    }

    /**
     * @return Http client configuration
     */
    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

    protected CredentialsProvider getCredentialsProvider() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        for (BasicCredential basicCredential : basicCredentials) {
            credsProvider.setCredentials(basicCredential.getAuthScope(), basicCredential.getUsernamePasswordCredentials());
        }
        return credsProvider;
    }

    protected AuthCache getAuthCache() {
        AuthCache authCache = new BasicAuthCache();
        BasicScheme authScheme = new BasicScheme();
        for (BasicCredential basicCredential : basicCredentials) {
            authCache.put(basicCredential.getHttpHost(), authScheme);
        }
        return authCache;
    }

    public HttpClientContext getPreemptiveContext() {
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(getCredentialsProvider());
        context.setAuthCache(getAuthCache());
        return context;
    }

    public Registry<ConnectionSocketFactory> getSocketFactory() throws HttpClientSSLSetupException {
        SSLConnectionSocketFactory sslSocketFactory;
        if (useInsecureSSL) {
            SSLContext ctx;
            try {
                X509TrustManager x509TrustManager = new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }
                    @Override
                        public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                };
                ctx = SSLContext.getInstance("TLS");
                ctx.init(new KeyManager[0], new TrustManager[]{x509TrustManager}, new SecureRandom());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new HttpClientSSLSetupException(e);
            }
            sslSocketFactory = new SSLConnectionSocketFactory(
                ctx,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
            );
        } else {
            sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        }
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(httpClient, basicCredentials, useInsecureSSL);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CallbackConfiguration other = (CallbackConfiguration) obj;
        // httpClient is missing equals() so not using it
        return Objects.equal(this.basicCredentials, other.basicCredentials)
                && Objects.equal(this.useInsecureSSL, other.useInsecureSSL);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(CallbackConfiguration.class)
                .addValue(httpClient)
                .addValue(basicCredentials)
                .addValue(useInsecureSSL)
                .toString();
    }
}
