/*
 * #%L
 * Osmium
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
package nl.esciencecenter.osmium.mac;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.ContextAwareAuthScheme;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MAC Access Authentication scheme as defined in https://tools.ietf.org/html/draft-ietf-oauth-v2-http-mac-02
 *
 * Sign {@link HttpRequest} with a MAC key.
 *
 * @author verhoes
 *
 */
public class MacScheme implements ContextAwareAuthScheme {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MacScheme.class);

    /** The name of this authorization scheme. */
    public static final String SCHEME_NAME = "MAC";

    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;
    /**
     * Used to generate timestamp in Auth header.
     */
    private Date date = new Date();
    /**
     * Used to generate nonce in Auth header.
     */
    private Random random = new SecureRandom();

    public void processChallenge(Header header)
            throws MalformedChallengeException {
        // Not challenge based
    }

    public String getSchemeName() {
        return SCHEME_NAME;
    }

    public String getParameter(String name) {
        return null;
    }

    public String getRealm() {
        return null;
    }

    public boolean isConnectionBased() {
        return false;
    }

    public boolean isComplete() {
        return true;
    }

    @Deprecated
    public Header authenticate(Credentials credentials, HttpRequest request)
            throws AuthenticationException {
        return authenticate(credentials, request, null);
    }

    /**
     * Generates normalized request string and encrypt is using a secret key
     *
     * @param credentials Username/Password to use
     * @param request Request to derive key
     * @param context HTTP context
     * @return MAC Authentication header
     * @throws AuthenticationException when signature generation fails
     */
    public Header authenticate(Credentials credentials, HttpRequest request,
            HttpContext context) throws AuthenticationException {
        String id = credentials.getUserPrincipal().getName();
        String key = credentials.getPassword();
        Long timestamp = getTimestamp();
        String nonce = getNonce();
        String data = getNormalizedRequestString((HttpUriRequest) request,
                nonce, timestamp);

        String request_mac = calculateRFC2104HMAC(data, key,
                getAlgorithm(credentials));

        return new BasicHeader(AUTH.WWW_AUTH_RESP, headerValue(id, timestamp,
                nonce, request_mac));
    }

    private String headerValue(String id, Long timestamp, String nonce,
            String request_mac) {
        String headerValue = "MAC id=\"" + id + "\",";
        headerValue += "ts=\"" + timestamp + "\",";
        headerValue += "nonce=\"" + nonce + "\",";
        headerValue += "mac=\"" + request_mac + "\"";
        return headerValue;
    }

    /**
     * Computes RFC 2104-compliant HMAC signature.
     *
     * @param data
     *            The data to be signed.
     * @param key
     *            The signing key.
     * @param algorithm
     *            MAC algorithm implemented by javax.crypto.MAC
     * @return The Base64-encoded RFC 2104-compliant HMAC signature.
     * @throws AuthenticationException
     *             when signature generation fails
     */
    private String calculateRFC2104HMAC(String data, String key,
            String algorithm) throws AuthenticationException {
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec macKey = new SecretKeySpec(key.getBytes(StandardCharsets.US_ASCII), "RAW");
            mac.init(macKey);
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.US_ASCII));
            return Base64.encodeBase64String(signature);
        } catch (InvalidKeyException e) {
            throw new AuthenticationException("Failed to generate HMAC: "
                    + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException("Algorithm is not supported", e);
        }
    }

    private String getNormalizedRequestString(HttpUriRequest request,
            String nonce, Long timestamp) {
        URI uri = request.getURI();
        // request can become wrapped, causing the request.getURI() to miss host
        // and port
        if (request instanceof RequestWrapper) {
            uri = ((HttpUriRequest) ((RequestWrapper) request).getOriginal())
                    .getURI();
        }
        String normalized_request_string = timestamp + "\n";
        normalized_request_string += nonce + "\n";
        normalized_request_string += request.getMethod() + "\n";
        normalized_request_string += uri.getPath() + "\n";
        normalized_request_string += uri.getHost().toLowerCase() + "\n";
        normalized_request_string += getPort(uri) + "\n";
        normalized_request_string += "" + "\n";
        return normalized_request_string;
    }

    /**
     * Random getter
     * @return Random
     */
    public Random getRandom() {
        return random;
    }

    /**
     * Random setter
     * @param random Random object
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    private String getNonce() {
        return new BigInteger(130, random).toString(32);
    }

    /**
     * Date setter
     * @param date The date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Date getter
     *
     * @return Date
     */
    public Date getDate() {
        return date;
    }

    private Long getTimestamp() {
        return TimeUnit.SECONDS.convert(date.getTime(), TimeUnit.MILLISECONDS);
    }

    /**
     * @param uri Uri from which to extract port
     * @return Port of `uri` based on explicit port or derived from scheme
     */
    public static int getPort(URI uri) {
        int port = uri.getPort();
        if (port == -1) {
            String scheme = uri.getScheme();
            if (scheme.equals("http")) {
                port = HTTP_PORT;
            } else if (scheme.equals("https")) {
                port = HTTPS_PORT;
            }
        }
        return port;
    }

    private String getAlgorithm(Credentials credentials) {
        String standardAlgo = ((MacCredential) credentials).getAlgorithm();
        return algorithmMapper(standardAlgo);
    }

    /**
     * The MAC Access authentication standard uses different names for algorithms then
     * {@link javax.crypto.Mac}
     *
     * This maps from standard 2 java name.
     *
     * @param standard name for algorithm
     * @return java name for algorithm
     */
    public static String algorithmMapper(String standard) {
        String java = standard;
        if (standard.equals("hmac-sha-1")) {
            java = "HmacSHA1";
        } else if (standard.equals("hmac-sha-256")) {
            java = "HmacSHA256";
        }
        // TODO implement registered extension algorithm
        return java;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(date);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }
}
