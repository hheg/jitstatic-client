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
import java.util.function.Function;

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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.message.BasicHeader;

class JitStaticUpdaterClientImpl implements JitStaticUpdaterClient {

    private static final String HTTPS = "https";
    static final String HTTP = "http";
    private static final String UTF_8 = "utf-8";
    private static final String APPLICATION_JSON = "application/json";
    private static final String JITSTATIC_ENDPOINT = "storage/";
    private static final Header[] HEADERS = new Header[] { new BasicHeader(HttpHeaders.ACCEPT, APPLICATION_JSON),
            new BasicHeader(HttpHeaders.ACCEPT, "*/*;q=0.8"), new BasicHeader(HttpHeaders.ACCEPT_CHARSET, UTF_8),
            new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "deflate, gzip;q=1.0, *;q=0.5"),
            new BasicHeader(HttpHeaders.USER_AGENT, String.format("jitstatic-client_%s-%s", ProjectVersion.INSTANCE.getBuildVersion(),
                    ProjectVersion.INSTANCE.getCommitIdAbbrev())) };

    static final String REF = "ref";
    private static final String RECURSIVE = "recursive";
    private static final String LIGHT = "light";
    private final CloseableHttpClient client;
    private final URI baseURL;
    private final CredentialsProvider credentialsProvider;
    private final HttpHost target;

    JitStaticUpdaterClientImpl(final String host, final int port, final String scheme, final String appContext, final String user,
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
        this.target = target;
        if (requestConfig != null) {
            httpClientBuilder.setDefaultRequestConfig(requestConfig);
        }

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
        }

        if (user != null) {
            final CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()),
                    new UsernamePasswordCredentials(user, password));
            httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
            this.credentialsProvider = credsProvider;
        } else {
            this.credentialsProvider = null;
        }
        client = httpClientBuilder.build();

        this.baseURL = new URIBuilder().setHost(host).setScheme(scheme).setPort(port).build().resolve(appContext)
                .resolve(JITSTATIC_ENDPOINT);
    }

    private static HttpClientContext getHostContext(final HttpHost target, final CredentialsProvider credsProvider) {
        if (credsProvider != null) {
            final AuthCache authCache = new BasicAuthCache();
            authCache.put(target, new BasicScheme());
            final HttpClientContext context = HttpClientContext.create();
            context.setCredentialsProvider(credsProvider);
            context.setAuthCache(authCache);
            return context;
        }
        return null;
    }

    @Override
    public String modifyKey(final byte[] data, final CommitData commitData, final String version)
            throws URISyntaxException, ClientProtocolException, IOException, APIException {
        return modifyKey(new ByteArrayInputStream(data), commitData, version);
    }

    @Override
    public String modifyKey(final InputStream data, final CommitData commitData, final String version)
            throws URISyntaxException, ClientProtocolException, IOException, APIException {
        Objects.requireNonNull(commitData, "commitData cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        Objects.requireNonNull(version, "version cannot be null");

        final URIBuilder uriBuilder = resolve(commitData.getKey());
        APIHelper.addRefParameter(commitData.getBranch(), uriBuilder);
        final URI uri = uriBuilder.build();
        final HttpPut putRequest = new HttpPut(uri);
        putRequest.setHeaders(HEADERS);
        putRequest.addHeader(HttpHeaders.CONTENT_ENCODING, UTF_8);
        putRequest.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        putRequest.addHeader(HttpHeaders.IF_MATCH, APIHelper.checkVersion(version));

        final HttpClientContext context = getHostContext(target, credentialsProvider);
        final Entity modify = new ModifyKeyEntity(data, commitData.getMessage(), commitData.getUserInfo(), commitData.getUserMail());
        putRequest.setEntity(modify);
        try (final CloseableHttpResponse httpResponse = client.execute(putRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            APIHelper.checkPUTStatusCode(uri, putRequest, statusLine);
            return APIHelper.getSingleHeader(httpResponse, HttpHeaders.ETAG);
        }
    }

    @Override
    public <T> T getKey(final String key, final TriFunction<InputStream, String, String, T> entityFactory)
            throws ClientProtocolException, URISyntaxException, IOException, APIException {
        return getKey(key, null, entityFactory);
    }

    @Override
    public <T> T getKey(final String key, final TriFunction<InputStream, String, String, T> entityFactory, final String currentVersion)
            throws ClientProtocolException, URISyntaxException, IOException, APIException {
        return getKey(key, null, currentVersion, entityFactory);
    }

    @Override
    public <T> T getKey(final String key, final String ref, final TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, ClientProtocolException, IOException, APIException {
        return getKey(key, ref, null, entityFactory);
    }

    @Override
    public <T> T getKey(final String key, final String ref, final String currentVersion,
            final TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, ClientProtocolException, IOException, APIException {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(entityFactory, "entityFactory cannot be null");

        final URIBuilder uriBuilder = resolve(key);
        APIHelper.addRefParameter(Utils.checkRef(ref), uriBuilder);
        final URI url = uriBuilder.build();
        final HttpGet getRequest = new HttpGet(url);
        getRequest.setHeaders(HEADERS);
        if (currentVersion != null) {
            getRequest.addHeader(HttpHeaders.IF_MATCH, APIHelper.escapeVersion(currentVersion));
        }
        final HttpClientContext context = getHostContext(target, credentialsProvider);
        try (final CloseableHttpResponse httpResponse = client.execute(getRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            APIHelper.checkGETresponse(url, getRequest, statusLine);
            final String etagValue = APIHelper.getSingleHeader(httpResponse, HttpHeaders.ETAG);
            final String contentType = APIHelper.getSingleHeader(httpResponse, HttpHeaders.CONTENT_TYPE);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
                return entityFactory.apply(null, etagValue, contentType);
            } else {
                try (final InputStream content = httpResponse.getEntity().getContent()) {
                    return entityFactory.apply(content, etagValue, contentType);
                }
            }
        }
    }

    public <T> T listAll(final String key, final Function<InputStream, T> entityFactory)
            throws URISyntaxException, ClientProtocolException, IOException {
        return listAll(key, null, entityFactory);
    }

    public <T> T listAll(final String key, final String ref, final Function<InputStream, T> entityFactory)
            throws ClientProtocolException, URISyntaxException, IOException {
        return listAll(key, ref, false, entityFactory);
    }

    @Override
    public <T> T listAll(final String key, final boolean recursive, final Function<InputStream, T> entityFactory)
            throws URISyntaxException, ClientProtocolException, IOException {
        return listAll(key, null, recursive, entityFactory);
    }

    @Override
    public <T> T listAll(final String key, final boolean recursive, final boolean light, final Function<InputStream, T> entityFactory)
            throws URISyntaxException, ClientProtocolException, IOException {
        return listAll(key, null, recursive, light, entityFactory);
    }

    @Override
    public <T> T listAll(final String key, final String ref, final boolean recursive, final Function<InputStream, T> entityFactory)
            throws URISyntaxException, ClientProtocolException, IOException {
        return listAll(key, ref, recursive, false, entityFactory);
    }

    public <T> T listAll(final String key, final String ref, final boolean recursive, final boolean light,
            final Function<InputStream, T> entityFactory) throws URISyntaxException, ClientProtocolException, IOException {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(entityFactory, "entityFactory cannot be null");

        final URIBuilder uriBuilder = resolve(key);
        APIHelper.addRefParameter(Utils.checkRef(ref), uriBuilder);
        if (recursive) {
            uriBuilder.addParameter(JitStaticUpdaterClientImpl.RECURSIVE, "true");
        }
        if (light) {
            uriBuilder.addParameter(JitStaticUpdaterClientImpl.LIGHT, "true");
        }
        final URI url = uriBuilder.build();
        final HttpGet getRequest = new HttpGet(url);
        final HttpClientContext context = getHostContext(target, credentialsProvider);
        try (final CloseableHttpResponse httpResponse = client.execute(getRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            APIHelper.checkGETresponse(url, getRequest, statusLine);
            try (final InputStream content = httpResponse.getEntity().getContent()) {
                return entityFactory.apply(content);
            }
        }
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (final IOException ignore) {
        }
    }

    @Override
    public void delete(final CommitData commitData) throws URISyntaxException, ClientProtocolException, IOException {
        Objects.requireNonNull(commitData);
        final URIBuilder uriBuilder = resolve(commitData.getKey());
        APIHelper.addRefParameter(Utils.checkRef(commitData.getBranch()), uriBuilder);
        final URI url = uriBuilder.build();
        final HttpDelete deleteRequest = new HttpDelete(url);
        deleteRequest.setHeaders(HEADERS);
        deleteRequest.addHeader("X-jitstatic-name", commitData.getUserInfo());
        deleteRequest.addHeader("X-jitstatic-message", commitData.getMessage());
        deleteRequest.addHeader("X-jitstatic-mail", commitData.getUserMail());
        final HttpClientContext context = getHostContext(target, credentialsProvider);
        try (final CloseableHttpResponse httpResponse = client.execute(deleteRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            APIHelper.checkDELETEresponse(url, deleteRequest, statusLine);
        }
    }

    private URIBuilder resolve(final String key) {
        if ("/".equals(key)) {
            return new URIBuilder(baseURL);
        }
        return new URIBuilder(baseURL.resolve(key));
    }
}
