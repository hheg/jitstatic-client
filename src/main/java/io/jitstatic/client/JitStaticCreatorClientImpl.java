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
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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

class JitStaticCreatorClientImpl implements JitStaticCreatorClient {

    protected static final String HTTPS = "https";
    protected static final String BULK = "bulk/";

    static final String HTTP = "http";
    protected static final String UTF_8 = "utf-8";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String JITSTATIC_METAKEY_ENDPOINT = "metakey/";
    protected static final String JITSTATIC_STORAGE_ENDPOINT = "storage/";
    protected static final Header[] HEADERS = new Header[] { new BasicHeader(HttpHeaders.ACCEPT, APPLICATION_JSON),
            new BasicHeader(HttpHeaders.ACCEPT, "*/*;q=0.8"), new BasicHeader(HttpHeaders.ACCEPT_CHARSET, UTF_8),
            new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "deflate, gzip;q=1.0, *;q=0.5"), new BasicHeader(HttpHeaders.USER_AGENT,
                    String.format("jitstatic-client_%s-%s", ProjectVersion.INSTANCE.getBuildVersion(), ProjectVersion.INSTANCE.getCommitIdAbbrev())) };

    static final String REF = "ref";
    protected static final String RECURSIVE = "recursive";
    protected static final String LIGHT = "light";
    protected final CloseableHttpClient client;
    protected final URI storageURL;
    protected final URI metakeyURL;
    protected final URI baseURL;
    protected final URI bulkURL;
    protected final CredentialsProvider credentialsProvider;
    protected final HttpHost target;

    JitStaticCreatorClientImpl(final String host, final int port, final String scheme, final String appContext, final String user, final String password,
            final CacheConfig cacheConfig, final RequestConfig requestConfig, final HttpClientBuilder httpClientBuilder, final File cacheDir)
            throws URISyntaxException {
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
            throw new IllegalArgumentException("Not supported protocol " + scheme);
        }
        this.target = new HttpHost(host, port, scheme);
        if (requestConfig != null) {
            httpClientBuilder.setDefaultRequestConfig(requestConfig);
        }

        if (cacheConfig != null) {
            if (!(httpClientBuilder instanceof CachingHttpClientBuilder)) {
                throw new IllegalArgumentException("A CacheConfig is specified but HttpClientBuilder is not an instance of CachingHttpClientBuilder");
            }
            final CachingHttpClientBuilder chcb = ((CachingHttpClientBuilder) httpClientBuilder);
            chcb.setCacheConfig(cacheConfig);
            if (cacheDir != null) {
                chcb.setCacheDir(cacheDir);
            }
        }

        if (user != null) {
            final CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()), new UsernamePasswordCredentials(user, password));
            httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
            this.credentialsProvider = credsProvider;
        } else {
            this.credentialsProvider = null;
        }
        client = httpClientBuilder.build();

        this.baseURL = new URIBuilder().setHost(host).setScheme(scheme).setPort(port).build().resolve(appContext).resolve(JITSTATIC_STORAGE_ENDPOINT);
        this.bulkURL = new URIBuilder().setHost(host).setScheme(scheme).setPort(port).build().resolve(appContext).resolve(BULK);
        this.storageURL = new URIBuilder().setHost(host).setScheme(scheme).setPort(port).build().resolve(appContext).resolve(JITSTATIC_STORAGE_ENDPOINT);
        this.metakeyURL = new URIBuilder().setHost(host).setScheme(scheme).setPort(port).build().resolve(appContext).resolve(JITSTATIC_METAKEY_ENDPOINT);

    }

    @Deprecated
    JitStaticCreatorClientImpl(final String host, final int port, final String scheme, final String appContext, final String user, final String password,
            final HttpClientBuilder httpClientBuilder, final RequestConfig requestConfig) throws URISyntaxException {
        this(host, port, scheme, appContext, user, password, null, requestConfig, httpClientBuilder, null);
    }

    protected static HttpClientContext getHostContext(final HttpHost target, final CredentialsProvider credsProvider) {
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

    private void checkPOStStatusCode(final HttpPost postRequest, final StatusLine statusLine, HttpEntity httpEntity) throws IOException {
        int statusCode = statusLine.getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
        } else {
            throw new APIException(statusLine, storageURL.toString(), postRequest.getMethod(), httpEntity);
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
    public <T> T getMetaKey(final String key, final String ref, final TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, IOException {
        return getMetaKey(key, ref, null, entityFactory);
    }

    @Override
    public <T> T getMetaKey(final String key, final String ref, final String currentVersion, final TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, IOException {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(entityFactory, "entityFactory cannot be null");

        final URIBuilder uriBuilder = resolve(key, metakeyURL);
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
            APIHelper.checkGETresponse(url, getRequest, statusLine, httpResponse.getEntity());
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

    @Override
    public String modifyMetaKey(final String key, final String ref, final String version, final ModifyUserKeyData data) throws IOException, URISyntaxException {
        Objects.requireNonNull(data, "data cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        Objects.requireNonNull(version, "version cannot be null");

        final URIBuilder uriBuilder = resolve(key, metakeyURL);
        APIHelper.addRefParameter(Utils.checkRef(ref), uriBuilder);
        final URI uri = uriBuilder.build();
        final HttpPut putRequest = new HttpPut(uri);
        putRequest.setHeaders(HEADERS);
        putRequest.addHeader(HttpHeaders.CONTENT_ENCODING, UTF_8);
        putRequest.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        putRequest.addHeader(HttpHeaders.IF_MATCH, APIHelper.checkVersion(version));

        final HttpClientContext context = getHostContext(target, credentialsProvider);
        putRequest.setEntity(new ModifyUserKeyEntity(data));
        try (final CloseableHttpResponse httpResponse = client.execute(putRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            APIHelper.checkPUTStatusCode(uri, putRequest, statusLine, httpResponse.getEntity());
            return APIHelper.getSingleHeader(httpResponse, HttpHeaders.ETAG);
        }
    }

    protected URIBuilder resolve(final String key, URI url) {
        if ("/".equals(key)) {
            return new URIBuilder(url);
        }
        return new URIBuilder(url.resolve(key));
    }

    @Override
    @Deprecated
    public String createKey(byte[] data, CommitData commitData, MetaData metaData) throws IOException, APIException {
        return createKey(new ByteArrayInputStream(data), commitData, metaData);
    }

    @Override
    @Deprecated
    public String createKey(InputStream data, CommitData commitData, MetaData metaData) throws IOException, APIException {
        final HttpPost postRequest = new HttpPost(storageURL);
        postRequest.setHeaders(HEADERS);
        if (!APPLICATION_JSON.equals(metaData.getContentType())) {
            postRequest.addHeader(HttpHeaders.ACCEPT, metaData.getContentType());
        }
        postRequest.addHeader(HttpHeaders.CONTENT_ENCODING, UTF_8);
        postRequest.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        final HttpClientContext context = getHostContext(target, credentialsProvider);

        final MetaDataEntity modify = new AddKeyEntity(data, commitData, metaData);
        postRequest.setEntity(modify);
        try (final CloseableHttpResponse httpResponse = client.execute(postRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            checkPOStStatusCode(postRequest, statusLine, httpResponse.getEntity());
            return APIHelper.getSingleHeader(httpResponse, HttpHeaders.ETAG);
        }
    }
}
