package nl.esciencecenter.osmium.callback;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.setup.Environment;
import nl.esciencecenter.osmium.api.JobStatusResponse;

public class CallbackClient {
    private HttpClient httpClient;
    private HttpContext context;

    public CallbackClient(Environment environment, CallbackConfiguration config) throws HttpClientSSLSetupException {
        httpClient = new HttpClientBuilder(environment).using(config.getHttpClientConfiguration()).using(config.getSocketFactory()).build("callback");
        // the http client builder does not support authcache so use context for each http request
        context = config.getPreemptiveContext();
    }

    public CallbackClient(HttpClient httpClient, HttpContext context) {
        this.httpClient = httpClient;
        this.context = context;
    }

    public HttpContext getContext() {
        return context;
    }

    public void putState(URI uri, JobStatusResponse status) throws ClientProtocolException, IOException {
        String body = status.toJson();
        HttpPut put = new HttpPut(uri);
        HttpEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
        put.setEntity(entity);
        httpClient.execute(put, context);
    }
}
