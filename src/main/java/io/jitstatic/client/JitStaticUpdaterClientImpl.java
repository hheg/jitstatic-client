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
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.cache.CacheConfig;

class JitStaticUpdaterClientImpl extends JitStaticCreatorClientImpl implements JitStaticUpdaterClient {

    JitStaticUpdaterClientImpl(final String host, final int port, final String scheme, final String appContext, final String user, final String password,
            final CacheConfig cacheConfig, final RequestConfig requestConfig, final HttpClientBuilder httpClientBuilder, final File cacheDir)
            throws URISyntaxException {
        super(host, port, scheme, appContext, user, password, cacheConfig, requestConfig, httpClientBuilder, cacheDir);
    }

    @Override
    public String modifyKey(final byte[] data, final CommitData commitData, final String version) throws URISyntaxException, IOException, APIException {
        return modifyKey(new ByteArrayInputStream(data), commitData, version);
    }

    @Override
    public String modifyKey(final InputStream data, final CommitData commitData, final String version) throws URISyntaxException, IOException, APIException {
        Objects.requireNonNull(commitData, "commitData cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        Objects.requireNonNull(version, "version cannot be null");

        final URIBuilder uriBuilder = resolve(commitData.getKey(), metakeyURL);
        APIHelper.addRefParameter(commitData.getBranch(), uriBuilder);
        final URI uri = uriBuilder.build();
        final HttpPut putRequest = new HttpPut(uri);
        putRequest.setHeaders(HEADERS);
        putRequest.addHeader(HttpHeaders.CONTENT_ENCODING, UTF_8);
        putRequest.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        putRequest.addHeader(HttpHeaders.IF_MATCH, APIHelper.checkVersion(version));

        final HttpClientContext context = getHostContext(target, credentialsProvider);
        final JsonEntity modify = new ModifyKeyEntity(data, commitData.getMessage(), commitData.getUserInfo(), commitData.getUserMail());
        putRequest.setEntity(modify);
        try (final CloseableHttpResponse httpResponse = client.execute(putRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            APIHelper.checkPUTStatusCode(uri, putRequest, statusLine, httpResponse.getEntity());
            return APIHelper.getSingleHeader(httpResponse, HttpHeaders.ETAG);
        }
    }

    @Override
    public <T> T getKey(final String key, final TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, IOException, APIException {
        return getKey(key, null, entityFactory);
    }

    @Override
    public <T> T getKey(final String key, final TriFunction<InputStream, String, String, T> entityFactory, final String currentVersion)
            throws URISyntaxException, IOException, APIException {
        return getKey(key, null, currentVersion, entityFactory);
    }

    @Override
    public <T> T getKey(final String key, final String ref, final TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, IOException, APIException {
        return getKey(key, ref, null, entityFactory);
    }

    @Override
    public <T> T getKey(final String key, final String ref, final String currentVersion, final TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, IOException, APIException {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(entityFactory, "entityFactory cannot be null");

        final URIBuilder uriBuilder = resolve(key, storageURL);
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

    public <T> T listAll(final String key, final Function<InputStream, T> entityFactory) throws URISyntaxException, IOException {
        return listAll(key, null, entityFactory);
    }

    public <T> T listAll(final String key, final String ref, final Function<InputStream, T> entityFactory) throws URISyntaxException, IOException {
        return listAll(key, ref, false, entityFactory);
    }

    @Override
    public <T> T listAll(final String key, final boolean recursive, final Function<InputStream, T> entityFactory) throws URISyntaxException, IOException {
        return listAll(key, null, recursive, entityFactory);
    }

    @Override
    public <T> T listAll(final String key, final boolean recursive, final boolean light, final Function<InputStream, T> entityFactory)
            throws URISyntaxException, IOException {
        return listAll(key, null, recursive, light, entityFactory);
    }

    @Override
    public <T> T listAll(final String key, final String ref, final boolean recursive, final Function<InputStream, T> entityFactory)
            throws URISyntaxException, IOException {
        return listAll(key, ref, recursive, false, entityFactory);
    }

    public <T> T listAll(final String key, final String ref, final boolean recursive, final boolean light, final Function<InputStream, T> entityFactory)
            throws URISyntaxException, IOException {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(entityFactory, "entityFactory cannot be null");
        if (!key.endsWith("/")) {
            throw new IllegalArgumentException(String.format("%s must end with / to be able to perform list operation", key));
        }
        final URIBuilder uriBuilder = resolve(key, storageURL);
        APIHelper.addRefParameter(Utils.checkRef(ref), uriBuilder);
        if (recursive) {
            uriBuilder.addParameter(JitStaticUpdaterClientImpl.RECURSIVE, "true");
        }
        if (light) {
            uriBuilder.addParameter(JitStaticUpdaterClientImpl.LIGHT, "true");
        }
        final URI url = uriBuilder.build();
        final HttpGet getRequest = new HttpGet(url);
        getRequest.setHeaders(HEADERS);
        final HttpClientContext context = getHostContext(target, credentialsProvider);
        try (final CloseableHttpResponse httpResponse = client.execute(getRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            APIHelper.checkGETresponse(url, getRequest, statusLine, httpResponse.getEntity());
            try (final InputStream content = httpResponse.getEntity().getContent()) {
                return entityFactory.apply(content);
            }
        }
    }

    @Override
    public <T> T search(final List<BulkSearch> search, final Function<InputStream, T> entityFactory) throws URISyntaxException, IOException {
        final URI url = bulkURL.resolve("fetch");
        final HttpPost postRequest = new HttpPost(url);
        postRequest.setHeaders(HEADERS);
        postRequest.setEntity(new BulkSearchEntity(search));
        final HttpClientContext context = getHostContext(target, credentialsProvider);
        try (final CloseableHttpResponse httpResponse = client.execute(postRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            APIHelper.checkPOSTresponse(url, postRequest, statusLine, httpResponse.getEntity());
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
    public void delete(final CommitData commitData) throws URISyntaxException, IOException {
        Objects.requireNonNull(commitData);
        final URIBuilder uriBuilder = resolve(commitData.getKey(), storageURL);
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
            APIHelper.checkDELETEresponse(url, deleteRequest, statusLine, httpResponse.getEntity());
        }
    }

}
