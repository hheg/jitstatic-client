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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

class JitStaticCreatorClientImpl implements JitStaticCreatorClient {
    private static final String HTTPS = "https";
    private static final String HTTP = "http";
    private static final String UTF_8 = "utf-8";
    private static final String APPLICATION_JSON = "application/json";
    private static final String JITSTATIC_ENDPOINT = "storage/";
    private static final Header[] HEADERS = new Header[] { new BasicHeader(HttpHeaders.ACCEPT, APPLICATION_JSON),
            new BasicHeader(HttpHeaders.ACCEPT_CHARSET, UTF_8),
            new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "deflate, gzip;q=1.0, *;q=0.5"),
            new BasicHeader(HttpHeaders.USER_AGENT, String.format("jitstatic-client_%s-%s", ProjectVersion.INSTANCE.getBuildVersion(),
                    ProjectVersion.INSTANCE.getCommitIdAbbrev())) };

    private final CloseableHttpClient client;
    private final HttpClientContext context;
    private final URI baseURL;

    JitStaticCreatorClientImpl(final String host, final int port, final String scheme, final String appContext, final String user,
            final String password, final HttpClientBuilder httpClientBuilder, final RequestConfig requestConfig) throws URISyntaxException {
        Objects.requireNonNull(httpClientBuilder);
        Objects.requireNonNull(host);
        Objects.requireNonNull(appContext);

        if (host.isEmpty()) {
            throw new IllegalArgumentException("host cannot be empty");
        }
        if (!appContext.startsWith("/")) {
            throw new IllegalArgumentException("appContext " + appContext + " doesn't start with an '/'");
        }
        if (!appContext.endsWith("/")) {
            throw new IllegalArgumentException("appContext " + appContext + " doesn't end with an '/'");
        }

        if (!(HTTP.equalsIgnoreCase(Objects.requireNonNull(scheme)) || HTTPS.equalsIgnoreCase(scheme))) {
            throw new IllegalArgumentException("Not supported protocol " + String.valueOf(scheme));
        }

        if (requestConfig != null) {
            httpClientBuilder.setDefaultRequestConfig(requestConfig);
        }

        final HttpHost target = new HttpHost(host, port, scheme);

        if (user != null) {
            final CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()),
                    new UsernamePasswordCredentials(user, password));
            httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
            this.context = getHostContext(target, credsProvider);
        } else {
            this.context = null;
        }

        client = httpClientBuilder.build();

        this.baseURL = new URIBuilder().setHost(host).setScheme(scheme).setPort(port).build().resolve(appContext)
                .resolve(JITSTATIC_ENDPOINT);
    }

    private HttpClientContext getHostContext(final HttpHost target, final CredentialsProvider credsProvider) {
        final AuthCache authCache = new BasicAuthCache();
        authCache.put(target, new BasicScheme());
        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        return context;
    }

    /* (non-Javadoc)
     * @see io.jitstatic.client.JitStaticCreatorClientInterface#createKey(byte[], io.jitstatic.client.CommitData, io.jitstatic.client.MetaData, io.jitstatic.client.TriFunction)
     */
    @Override
    public <T extends BaseEntity> T createKey(final byte[] data, final CommitData commitData, final MetaData metaData,
            final TriFunction<InputStream, String, String, T> entityFactory) throws ClientProtocolException, IOException, APIException {
        return createKey(new ByteArrayInputStream(data), commitData, metaData, entityFactory);
    }

    /* (non-Javadoc)
     * @see io.jitstatic.client.JitStaticCreatorClientInterface#createKey(java.io.InputStream, io.jitstatic.client.CommitData, io.jitstatic.client.MetaData, io.jitstatic.client.TriFunction)
     */
    @Override
    public <T extends BaseEntity> T createKey(final InputStream data, final CommitData commitData, final MetaData metaData,
            final TriFunction<InputStream, String, String, T> entityFactory) throws ClientProtocolException, IOException, APIException {

        final HttpPost postRequest = new HttpPost(baseURL);
        postRequest.setHeaders(HEADERS);
        if (!APPLICATION_JSON.equals(metaData.getContentType())) {
            postRequest.addHeader(HttpHeaders.ACCEPT, metaData.getContentType());
        }
        postRequest.addHeader(HttpHeaders.CONTENT_ENCODING, UTF_8);
        postRequest.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);

        final AddKeyEntity modify = new AddKeyEntity(data, commitData, metaData);
        postRequest.setEntity(modify);
        try (final CloseableHttpResponse httpResponse = client.execute(postRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            checkPOStStatusCode(postRequest, statusLine);
            final String etagValue = getSingleHeader(httpResponse, HttpHeaders.ETAG);
            final String contentTypeValue = getSingleHeader(httpResponse, HttpHeaders.CONTENT_TYPE);
            try (final InputStream content = httpResponse.getEntity().getContent()) {
                return entityFactory.apply(content, etagValue, contentTypeValue);
            }
        }
    }

    private String getSingleHeader(final CloseableHttpResponse httpResponse, final String headerTag) {
        final Header[] headers = httpResponse.getHeaders(headerTag);
        if (headers.length != 1) {
            return null;
        }
        return headers[0].getValue();
    }

    private void checkPOStStatusCode(final HttpPost postRequest, final StatusLine statusLine) throws APIException {
        switch (statusLine.getStatusCode()) {
        case HttpStatus.SC_OK:
            break;
        default:
            throw new APIException(statusLine, baseURL.toString(), postRequest.getMethod());
        }
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (final IOException ignore) {
        }
    }

    public static class JitStaticCreatorClientBuilder {
        private int port = 80;
        private String scheme = HTTP;
        private HttpClientBuilder httpClientBuilder;
        private RequestConfig requestConfig;
        private String host;
        private String appContext;
        private String user;
        private String password;

        public static JitStaticCreatorClientBuilder create() {
            return new JitStaticCreatorClientBuilder();
        }

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
}
