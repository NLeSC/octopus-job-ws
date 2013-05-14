package nl.esciencecenter.octopus.webservice;

/*
 * #%L
 * Octopus Job Webservice
 * %%
 * Copyright (C) 2013 Nederlands eScience Center
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.AbstractHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.webservice.health.JobLauncherHealthCheck;
import nl.esciencecenter.octopus.webservice.job.OctopusManager;
import nl.esciencecenter.octopus.webservice.mac.MacCredential;
import nl.esciencecenter.octopus.webservice.mac.MacScheme;
import nl.esciencecenter.octopus.webservice.mac.MacSchemeFactory;
import nl.esciencecenter.octopus.webservice.resources.JobResource;
import nl.esciencecenter.octopus.webservice.resources.JobsResource;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.HttpClientBuilder;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

/**
 * Service to submit jobs using a Octopus scheduler and sandbox.
 *
 * @author verhoes
 *
 */
public class JobLauncherService extends Service<JobLauncherConfiguration> {
    protected static final Logger logger = LoggerFactory.getLogger(JobLauncherService.class);

    /**
     * Entry point
     *
     * @param args
     *            CLI arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new JobLauncherService().run(args);
    }

    @Override
    public void initialize(Bootstrap<JobLauncherConfiguration> bootstrap) {
        bootstrap.setName("joblauncher");
    }

    @Override
    public void run(JobLauncherConfiguration configuration, Environment environment) throws Exception {
        OctopusManager octopus = new OctopusManager(configuration.getOctopusConfiguration());
        environment.manage(octopus);
        HttpClient httpClient = new HttpClientBuilder().using(configuration.getHttpClientConfiguration()).build();

        httpClient = macifyHttpClient((AbstractHttpClient) httpClient, configuration.getMacs());

        if (configuration.isUseInsecureSSL()) {
            useInsecureSSL(httpClient);
        }

        environment.addResource(new JobsResource(octopus, httpClient));
        environment.addResource(new JobResource(octopus));
        environment.addHealthCheck(new JobLauncherHealthCheck("joblauncher"));
    }

    /**
     * Enable insecure SSL in http client like self signed certificates.
     *
     * @param httpClient
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     */
    public void useInsecureSSL(HttpClient httpClient) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException,
            UnrecoverableKeyException {
        SSLSocketFactory socketFactory;
        socketFactory = new SSLSocketFactory(new TrustStrategy() {

            public boolean isTrusted(final X509Certificate[] chain, String authType) throws CertificateException {
                // Oh, I am easy...
                return true;
            }

        }, org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        SchemeRegistry registry = httpClient.getConnectionManager().getSchemeRegistry();
        registry.register(new Scheme("https", 443, socketFactory));
    }

    /**
     * Adds MAC Access Authentication scheme to http client and registers list of MAC credentials with http client.
     *
     * Http client will use MAC Access Authentication when url is in scope of given MAC credentials.
     *
     * @param httpClient
     * @param macs
     * @return httpClient with MAC access authentication and credentials injected.
     */
    public static AbstractHttpClient macifyHttpClient(AbstractHttpClient httpClient, ImmutableList<MacCredential> macs) {

        // Add MAC scheme
        httpClient.getAuthSchemes().register(MacScheme.SCHEME_NAME, new MacSchemeFactory());

        // Add configured MAC id/key pairs.
        CredentialsProvider credentialProvider = httpClient.getCredentialsProvider();
        for (MacCredential mac : macs) {
            credentialProvider.setCredentials(mac.getAuthScope(), mac);
        }

        // Add MAC scheme to ordered list of supported authentication schemes
        // See HTTP authentication parameters chapter on
        // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/authentication.html
        List<String> authSchemes =
                Collections.unmodifiableList(Arrays.asList(new String[] { MacScheme.SCHEME_NAME, AuthPolicy.SPNEGO,
                        AuthPolicy.KERBEROS, AuthPolicy.NTLM, AuthPolicy.DIGEST, AuthPolicy.BASIC }));
        httpClient.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authSchemes);

        return httpClient;
    }

}
