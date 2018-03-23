package io.jitstatic.client;

import java.net.URISyntaxException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

public class JitStaticCreatorClientBuilder {
    private int port = 80;
    private String scheme = JitStaticCreatorClientImpl.HTTP;
    private HttpClientBuilder httpClientBuilder;
    private RequestConfig requestConfig;
    private String host;
    private String appContext;
    private String user;
    private String password;

    public RequestConfig getRequestConfig() {
        return requestConfig;
    }

    public JitStaticCreatorClientBuilder setRequestConfig(final RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }

    public String getHost() {
        return host;
    }

    public JitStaticCreatorClientBuilder setHost(final String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public JitStaticCreatorClientBuilder setPort(final int port) {
        this.port = port;
        return this;
    }

    public String getScheme() {
        return scheme;
    }

    public JitStaticCreatorClientBuilder setScheme(final String scheme) {
        this.scheme = scheme;
        return this;
    }

    public String getAppContext() {
        return appContext;
    }

    public JitStaticCreatorClientBuilder setAppContext(final String appContext) {
        this.appContext = appContext;
        return this;
    }

    public String getUser() {
        return user;
    }

    public JitStaticCreatorClientBuilder setUser(final String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public JitStaticCreatorClientBuilder setPassword(final String password) {
        this.password = password;
        return this;
    }

    public JitStaticCreatorClientImpl build() throws URISyntaxException {
        if (httpClientBuilder == null) {
            httpClientBuilder = HttpClients.custom();
        }
        return new JitStaticCreatorClientImpl(host, port, scheme, appContext, user, password, httpClientBuilder, requestConfig);
    }

    public HttpClientBuilder getHttpClientBuilder() {
        return httpClientBuilder;
    }

    public JitStaticCreatorClientBuilder setHttpClientBuilder(final HttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder;
        return this;
    }
}