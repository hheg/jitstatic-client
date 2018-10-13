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

import java.io.File;
import java.net.URISyntaxException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;

public class JitStaticUpdaterClientBuilder {
    private int port = 80;
    private String scheme = JitStaticUpdaterClientImpl.HTTP;
    private CacheConfig cacheConfig;
    private RequestConfig requestConfig;
    private HttpClientBuilder httpClientBuilder;
    private String host;
    private String appContext;
    private String user;
    private String password;
    private File cacheDirectory;

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    public JitStaticUpdaterClientBuilder setCacheConfig(final CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        return this;
    }

    public RequestConfig getRequestConfig() {
        return requestConfig;
    }

    public JitStaticUpdaterClientBuilder setRequestConfig(final RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }

    public String getHost() {
        return host;
    }

    public JitStaticUpdaterClientBuilder setHost(final String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public JitStaticUpdaterClientBuilder setPort(final int port) {
        this.port = port;
        return this;
    }

    public String getScheme() {
        return scheme;
    }

    public JitStaticUpdaterClientBuilder setScheme(final String scheme) {
        this.scheme = scheme;
        return this;
    }

    public String getAppContext() {
        return appContext;
    }

    public JitStaticUpdaterClientBuilder setAppContext(final String appContext) {
        this.appContext = appContext;
        return this;
    }

    public String getUser() {
        return user;
    }

    public JitStaticUpdaterClientBuilder setUser(final String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public JitStaticUpdaterClientBuilder setPassword(final String password) {
        this.password = password;
        return this;
    }

    public HttpClientBuilder getHttpClientBuilder() {
        return httpClientBuilder;
    }

    public JitStaticUpdaterClientBuilder setHttpClientBuilder(final HttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder;
        return this;
    }

    public JitStaticUpdaterClient build() throws URISyntaxException {
        if (httpClientBuilder == null) {
            if (cacheConfig != null) {
                httpClientBuilder = CachingHttpClients.custom();
            } else {
                httpClientBuilder = HttpClients.custom();
            }
        }
        return new JitStaticUpdaterClientImpl(host, port, scheme, appContext, user, password, cacheConfig, requestConfig, httpClientBuilder,
                cacheDirectory);
    }

    public File getCacheDirectory() {
        return cacheDirectory;
    }

    public void setCacheDirectory(final File cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }
}
