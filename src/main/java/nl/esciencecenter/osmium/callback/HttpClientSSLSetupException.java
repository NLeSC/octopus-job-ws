package nl.esciencecenter.osmium.callback;


public class HttpClientSSLSetupException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 6332719904156844773L;

    public HttpClientSSLSetupException(Exception e) {
        super.addSuppressed(e);
    }

}
