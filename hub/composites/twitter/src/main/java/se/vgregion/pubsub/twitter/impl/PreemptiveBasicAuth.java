package se.vgregion.pubsub.twitter.impl;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.protocol.HttpContext;

public class PreemptiveBasicAuth implements HttpRequestInterceptor {

    private String username;
    private String password;
    
    public PreemptiveBasicAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void process(HttpRequest request, HttpContext context) {
        AuthState authState = (AuthState) context.getAttribute(
                ClientContext.TARGET_AUTH_STATE);
        
        // If not auth scheme has been initialized yet
        if (authState.getAuthScheme() == null) {
            Credentials creds = new UsernamePasswordCredentials(username, password);
            authState.setAuthScheme(new BasicScheme());
            authState.setCredentials(creds);
        }
    }
}