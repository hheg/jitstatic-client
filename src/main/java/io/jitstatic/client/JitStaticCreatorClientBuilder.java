package io.jitstatic.client;

/*-
 * #%L
 * jitstatic
 * %%
 * Copyright (C) 2017 - 2018 H.Hegardt
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

import java.net.URISyntaxException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

@Deprecated
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

    public JitStaticCreatorClient build() throws URISyntaxException {
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
