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
import java.io.File;
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
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.message.BasicHeader;

class JitStaticUpdaterClient implements JitStaticUpdaterClientInterface {

    private static final String HTTPS = "https";
    private static final String HTTP = "http";
    private static final String UTF_8 = "utf-8";
    private static final String APPLICATION_JSON = "application/json";
    private static final Header[] HEADERS = new Header[] { new BasicHeader(HttpHeaders.ACCEPT, APPLICATION_JSON),
            new BasicHeader(HttpHeaders.ACCEPT_CHARSET, UTF_8),
            new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "deflate, gzip;q=1.0, *;q=0.5"),
            new BasicHeader(HttpHeaders.USER_AGENT, String.format("jitstatic-client_%s-%s", ProjectVersion.INSTANCE.getBuildVersion(),
                    ProjectVersion.INSTANCE.getCommitIdAbbrev())) };

    private static final String REF = "ref";
    private final CloseableHttpClient client;
    private final HttpClientContext context;
    private final URI baseURL;

    JitStaticUpdaterClient(final String host, final int port, final String scheme, final String appContext, final String user,
            final String password, final CacheConfig cacheConfig, final RequestConfig requestConfig,
            final HttpClientBuilder httpClientBuilder, final File cacheDir) throws URISyntaxException {
        Objects.requireNonNull(host, "host cannot be null");
        Objects.requireNonNull(appContext, "appContext cannot be null");
        Objects.requireNonNull(httpClientBuilder, "httpClientBuilder cannot be null");
        if (host.isEmpty()) {
            throw new IllegalArgumentException("host cannot be empty");
        }
        if (!appContext.startsWith("/")) {
            throw new IllegalArgumentException("appContext " + appContext + " doesn't start with an '/'");
        }
        if (!appContext.endsWith("/")) {
            throw new IllegalArgumentException("appContext " + appContext + " doesn't end with an '/'");
        }
        if (!(Objects.requireNonNull(scheme).equalsIgnoreCase(HTTP) || HTTPS.equalsIgnoreCase(scheme))) {
            throw new IllegalArgumentException("Not supported protocol " + String.valueOf(scheme));
        }
        final HttpHost target = new HttpHost(host, port, scheme);
        if (requestConfig != null) {
            httpClientBuilder.setDefaultRequestConfig(requestConfig);
        }
        HttpCacheContext cacheContext = null;
        if (cacheConfig != null) {
            if (!(httpClientBuilder instanceof CachingHttpClientBuilder)) {
                throw new IllegalArgumentException(
                        "A CacheConfig is specified but HttpClientBuilder is not an instance of CachingHttpClientBuilder");
            }
            final CachingHttpClientBuilder chcb = ((CachingHttpClientBuilder) httpClientBuilder);
            chcb.setCacheConfig(cacheConfig);
            if (cacheDir != null) {
                chcb.setCacheDir(cacheDir);
            }
            cacheContext = HttpCacheContext.create();
        }

        if (user != null) {
            final CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()),
                    new UsernamePasswordCredentials(user, password));
            httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
            this.context = getHostContext(target, credsProvider, cacheContext);
        } else {
            this.context = cacheContext;
        }
        client = httpClientBuilder.build();

        this.baseURL = new URIBuilder().setHost(host).setScheme(scheme).setPort(port).build().resolve(appContext);
    }

    private HttpClientContext getHostContext(final HttpHost target, final CredentialsProvider credsProvider,
            final HttpCacheContext cacheContext) {
        final AuthCache authCache = new BasicAuthCache();
        authCache.put(target, new BasicScheme());
        HttpClientContext context;
        if (cacheContext != null) {
            context = HttpClientContext.adapt(cacheContext);
        } else {
            context = HttpClientContext.create();
        }
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        return context;
    }

    /* (non-Javadoc)
     * @see io.jitstatic.client.JitStaticUpdaterClientInterface#modifyKey(byte[], io.jitstatic.client.CommitData, java.lang.String, java.lang.String, io.jitstatic.client.TriFunction)
     */
    @Override
    public <T extends BaseEntity> T modifyKey(final byte[] data, final CommitData commitData, final String version,
            final String contentType, final TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, ClientProtocolException, IOException, APIException {
        return modifyKey(new ByteArrayInputStream(data), commitData, version, contentType, entityFactory);
    }

    /* (non-Javadoc)
     * @see io.jitstatic.client.JitStaticUpdaterClientInterface#modifyKey(java.io.InputStream, io.jitstatic.client.CommitData, java.lang.String, java.lang.String, io.jitstatic.client.TriFunction)
     */
    @Override
    public <T extends BaseEntity> T modifyKey(final InputStream data, final CommitData commitData, final String version,
            final String contentType, final TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, ClientProtocolException, IOException, APIException {
        Objects.requireNonNull(commitData, "commitData cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        Objects.requireNonNull(version, "version cannot be null");
        Objects.requireNonNull(contentType,"contentType cannot be null");
        Objects.requireNonNull(entityFactory, "enityFactory cannot be null");

        final URIBuilder uriBuilder = new URIBuilder(baseURL.resolve(commitData.getKey()));
        addRefParameter(commitData.getBranch(), uriBuilder);
        final URI uri = uriBuilder.build();        
        final HttpPut putRequest = new HttpPut(uri);
        putRequest.setHeaders(HEADERS);
        putRequest.addHeader(HttpHeaders.CONTENT_ENCODING, UTF_8);
        putRequest.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        putRequest.addHeader(HttpHeaders.IF_MATCH, checkVersion(version));

        final KeyEntity modify = new ModifyKeyEntity(data, commitData.getMessage(), commitData.getUserInfo(), commitData.getUserMail(),
                contentType);
        putRequest.setEntity(modify);
        try (final CloseableHttpResponse httpResponse = client.execute(putRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            checkPUTStatusCode(uri, putRequest, statusLine);
            final String etagValue = getSingleHeader(httpResponse, HttpHeaders.ETAG);
            try (final InputStream content = modify.getContent()) {
                return entityFactory.apply(content, etagValue, contentType);
            }
        }
    }

    private String checkVersion(String version) {
        if(!version.startsWith("\"")) {
            version = "\""+version;
        }
        if(!version.endsWith("\"")) {
            version +="\"";
        }
        return version;
    }

    private void addRefParameter(final String ref, final URIBuilder uriBuilder) {
        if (ref != null) {
            uriBuilder.addParameter(REF, ref);
        }
    }

    private void checkPUTStatusCode(final URI uri, final HttpPut putRequest, final StatusLine statusLine) throws APIException {
        switch (statusLine.getStatusCode()) {
        case HttpStatus.SC_OK:
        case HttpStatus.SC_ACCEPTED:
            break;
        default:
            throw new APIException(statusLine, uri.toString(), putRequest.getMethod());
        }
    }

    /* (non-Javadoc)
     * @see io.jitstatic.client.JitStaticUpdaterClientInterface#getKey(java.lang.String, java.lang.String, io.jitstatic.client.TriFunction)
     */
    @Override
    public <T extends BaseEntity> T getKey(final String key, final String contenttype,
            final TriFunction<InputStream, String, String, T> entityFactory)
            throws ClientProtocolException, URISyntaxException, IOException, APIException {
        return getKey(key, null, contenttype, entityFactory);
    }

    /* (non-Javadoc)
     * @see io.jitstatic.client.JitStaticUpdaterClientInterface#getKey(java.lang.String, java.lang.String, java.lang.String, io.jitstatic.client.TriFunction)
     */
    @Override
    public <T extends BaseEntity> T getKey(final String key, final String ref, final String contentType,
            final TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, ClientProtocolException, IOException, APIException {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(contentType, "contentTpye cannot be null");
        Objects.requireNonNull(entityFactory, "entityFactory cannot be null");

        final URIBuilder uriBuilder = new URIBuilder(baseURL.resolve(key));
        addRefParameter(Utils.checkRef(ref), uriBuilder);
        final URI url = uriBuilder.build();
        final HttpGet getRequest = new HttpGet(url);
        getRequest.setHeaders(HEADERS);
        if (!APPLICATION_JSON.equals(contentType)) {
            getRequest.addHeader(HttpHeaders.ACCEPT, contentType);
        }
        try (final CloseableHttpResponse httpResponse = client.execute(getRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            checkGETresponse(url, getRequest, statusLine);
            final String etagValue = getSingleHeader(httpResponse, HttpHeaders.ETAG);
            try (final InputStream content = httpResponse.getEntity().getContent()) {
                return entityFactory.apply(content, etagValue, contentType);
            }
        }
    }

    private void checkGETresponse(final URI url, final HttpGet getRequest, final StatusLine statusLine) throws APIException {
        switch (statusLine.getStatusCode()) {
        case HttpStatus.SC_OK:
        case HttpStatus.SC_ACCEPTED:
        case HttpStatus.SC_NOT_MODIFIED:
            break;
        default:
            throw new APIException(statusLine, url.toString(), getRequest.getMethod());
        }
    }

    private String getSingleHeader(final CloseableHttpResponse httpResponse, final String headerTag) {
        final Header[] headers = httpResponse.getHeaders(headerTag);
        if (headers.length != 1) {
            return null;
        }
        return headers[0].getValue();
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (final IOException ignore) {
        }
    }

    public static class JitStaticUpdaterClientBuilder {
        private int port = 80;
        private String scheme = HTTP;
        private CacheConfig cacheConfig;
        private RequestConfig requestConfig;
        private HttpClientBuilder httpClientBuilder;
        private String host;
        private String appContext;
        private String user;
        private String password;
        private File cacheDirectory;

        public static JitStaticUpdaterClientBuilder create() {
            return new JitStaticUpdaterClientBuilder();
        }

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
            return new JitStaticUpdaterClient(host, port, scheme, appContext, user, password, cacheConfig, requestConfig, httpClientBuilder,
                    cacheDirectory);
        }

        public File getCacheDirectory() {
            return cacheDirectory;
        }

        public void setCacheDirectory(final File cacheDirectory) {
            this.cacheDirectory = cacheDirectory;
        }
    }
}
