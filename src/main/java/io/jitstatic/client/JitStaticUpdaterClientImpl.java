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
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.message.BasicHeader;

class JitStaticUpdaterClientImpl implements JitStaticUpdaterClient {

    private static final String HTTPS = "https";
    static final String HTTP = "http";
    private static final String UTF_8 = "utf-8";
    private static final String APPLICATION_JSON = "application/json";
    private static final Header[] HEADERS = new Header[] { new BasicHeader(HttpHeaders.ACCEPT, APPLICATION_JSON),
            new BasicHeader(HttpHeaders.ACCEPT, "*/*;q=0.8"), new BasicHeader(HttpHeaders.ACCEPT_CHARSET, UTF_8),
            new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "deflate, gzip;q=1.0, *;q=0.5"),
            new BasicHeader(HttpHeaders.USER_AGENT, String.format("jitstatic-client_%s-%s", ProjectVersion.INSTANCE.getBuildVersion(),
                    ProjectVersion.INSTANCE.getCommitIdAbbrev())) };

    private static final String REF = "ref";
    private final CloseableHttpClient client;
    private final HttpClientContext context;
    private final URI baseURL;

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

    /*
     * (non-Javadoc)
     * 
     * @see io.jitstatic.client.JitStaticUpdaterClientInterface#modifyKey(byte[],
     * io.jitstatic.client.CommitData, java.lang.String)
     */
    @Override
    public String modifyKey(final byte[] data, final CommitData commitData, final String version)
            throws URISyntaxException, ClientProtocolException, IOException, APIException {
        return modifyKey(new ByteArrayInputStream(data), commitData, version);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.jitstatic.client.JitStaticUpdaterClientInterface#modifyKey(java.io.
     * InputStream, io.jitstatic.client.CommitData, java.lang.String)
     */
    @Override
    public String modifyKey(final InputStream data, final CommitData commitData, final String version)
            throws URISyntaxException, ClientProtocolException, IOException, APIException {
        Objects.requireNonNull(commitData, "commitData cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        Objects.requireNonNull(version, "version cannot be null");

        final URIBuilder uriBuilder = new URIBuilder(baseURL.resolve(commitData.getKey()));
        addRefParameter(commitData.getBranch(), uriBuilder);
        final URI uri = uriBuilder.build();
        final HttpPut putRequest = new HttpPut(uri);
        putRequest.setHeaders(HEADERS);
        putRequest.addHeader(HttpHeaders.CONTENT_ENCODING, UTF_8);
        putRequest.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        putRequest.addHeader(HttpHeaders.IF_MATCH, checkVersion(version));

        final KeyEntity modify = new ModifyKeyEntity(data, commitData.getMessage(), commitData.getUserInfo(), commitData.getUserMail());
        putRequest.setEntity(modify);
        try (final CloseableHttpResponse httpResponse = client.execute(putRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            checkPUTStatusCode(uri, putRequest, statusLine);
            return getSingleHeader(httpResponse, HttpHeaders.ETAG);
        }
    }

    private String checkVersion(String version) {
        if (!version.startsWith("\"")) {
            version = "\"" + version;
        }
        if (!version.endsWith("\"")) {
            version += "\"";
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.jitstatic.client.JitStaticUpdaterClientInterface#getKey(java.lang.String,
     * io.jitstatic.client.TriFunction)
     */
    @Override
    public <T> T getKey(final String key, final TriFunction<InputStream, String, String, T> entityFactory)
            throws ClientProtocolException, URISyntaxException, IOException, APIException {
        return getKey(key, null, entityFactory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.jitstatic.client.JitStaticUpdaterClientInterface#getKey(java.lang.String,
     * java.lang.String, io.jitstatic.client.TriFunction)
     */
    @Override
    public <T> T getKey(final String key, final String ref, final TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, ClientProtocolException, IOException, APIException {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(entityFactory, "entityFactory cannot be null");

        final URIBuilder uriBuilder = new URIBuilder(baseURL.resolve(key));
        addRefParameter(Utils.checkRef(ref), uriBuilder);
        final URI url = uriBuilder.build();
        final HttpGet getRequest = new HttpGet(url);
        getRequest.setHeaders(HEADERS);
        try (final CloseableHttpResponse httpResponse = client.execute(getRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            checkGETresponse(url, getRequest, statusLine);
            final String etagValue = getSingleHeader(httpResponse, HttpHeaders.ETAG);
            final String contentType = getSingleHeader(httpResponse, HttpHeaders.CONTENT_TYPE);
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
}
