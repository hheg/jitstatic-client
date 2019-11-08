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
import org.apache.http.client.methods.HttpDelete;
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

class JitStaticClientImpl implements JitStaticClient {

    private static final String ENTITY_FACTORY_CANNOT_BE_NULL = "entityFactory cannot be null";
    private static final String KEY_CANNOT_BE_NULL = "key cannot be null";
    protected static final String HTTPS = "https";
    protected static final String BULK = "bulk/";

    static final String HTTP = "http";
    protected static final String UTF_8 = "utf-8";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String JITSTATIC_METAKEY_ENDPOINT = "metakey/";
    protected static final String JITSTATIC_STORAGE_ENDPOINT = "storage/";
    protected static final String JITSTATIC_USERS_ENDPOINT = "users/";
    protected static final String JITSTATIC_KEYADMIN_ENDPOINT = "keyadmin/";
    protected static final String JITSTATIC_KEYUSER_ENDPOINT = "keyuser/";
    protected static final String JITSTATIC_GITUSER_ENDPOINT = "git/";

    protected static final Header[] HEADERS = new Header[] {
            new BasicHeader(HttpHeaders.ACCEPT, APPLICATION_JSON),
            new BasicHeader(HttpHeaders.ACCEPT, "*/*;q=0.8"),
            new BasicHeader(HttpHeaders.ACCEPT_CHARSET, UTF_8),
            new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "deflate, gzip;q=1.0, *;q=0.5"),
            new BasicHeader(HttpHeaders.USER_AGENT,
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
    private final URI keyUserURL;
    private final URI keyAdminURL;
    private final URI keyGitUserURL;

    JitStaticClientImpl(final String host, final int port, final String scheme, final String appContext, final String user, final String password,
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
        this.keyUserURL = new URIBuilder().setHost(host).setScheme(scheme).setPort(port).build().resolve(appContext).resolve(JITSTATIC_USERS_ENDPOINT)
                .resolve(JITSTATIC_KEYUSER_ENDPOINT);
        this.keyAdminURL = new URIBuilder().setHost(host).setScheme(scheme).setPort(port).build().resolve(appContext).resolve(JITSTATIC_USERS_ENDPOINT)
                .resolve(JITSTATIC_KEYADMIN_ENDPOINT);
        this.keyGitUserURL = new URIBuilder().setHost(host).setScheme(scheme).setPort(port).build().resolve(appContext).resolve(JITSTATIC_USERS_ENDPOINT)
                .resolve(JITSTATIC_GITUSER_ENDPOINT);
    }

    @Deprecated
    JitStaticClientImpl(final String host, final int port, final String scheme, final String appContext, final String user, final String password,
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
    public <T> T getMetaKey(final String key, final String ref, final TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, IOException {
        return getMetaKey(key, ref, null, entityFactory);
    }

    @Override
    public <T> T getMetaKey(final String key, final String ref, final String currentVersion, final TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, IOException {
        Objects.requireNonNull(key, KEY_CANNOT_BE_NULL);
        Objects.requireNonNull(entityFactory, ENTITY_FACTORY_CANNOT_BE_NULL);
        return getKey(key, ref, currentVersion, metakeyURL, entityFactory);
    }

    @Override
    public String modifyMetaKey(final String key, final String ref, final String version, final ModifyUserKeyData data) throws IOException, URISyntaxException {
        Objects.requireNonNull(data, "data cannot be null");
        Objects.requireNonNull(version, "version cannot be null");
        ModifyUserKeyEntity entity = new ModifyUserKeyEntity(data);
        return modifyKey(key, ref, version, metakeyURL, entity);
    }

    protected URIBuilder resolve(final String key, URI url) {
        if ("/".equals(key)) {
            return new URIBuilder(url);
        }
        return new URIBuilder(url.resolve(key));
    }

    @Override
    public String createKey(byte[] data, CommitData commitData, MetaData metaData) throws IOException, APIException, URISyntaxException {
        return createKey(new ByteArrayInputStream(data), commitData, metaData);
    }

    @Override
    public String createKey(InputStream data, CommitData commitData, MetaData metaData) throws IOException, APIException, URISyntaxException {
        Objects.requireNonNull(commitData);
        Objects.requireNonNull(metaData);

        final MetaDataEntity entity = new AddKeyEntity(data, commitData, metaData);
        return addKey(commitData.getKey(), commitData.getBranch(), storageURL, entity);
    }

    @Override
    public String modifyKey(final byte[] data, final CommitData commitData, final String version) throws URISyntaxException, IOException, APIException {
        return modifyKey(new ByteArrayInputStream(data), commitData, version);
    }

    @Override
    public String modifyKey(final InputStream data, final CommitData commitData, final String version) throws URISyntaxException, IOException, APIException {
        Objects.requireNonNull(commitData, "commitData cannot be null");
        Objects.requireNonNull(version, "version cannot be null");
        final JsonEntity modify = new ModifyKeyEntity(data, commitData.getMessage(), commitData.getUserInfo(), commitData.getUserMail());
        return modifyKey(commitData.getKey(), commitData.getBranch(), version, storageURL, modify);
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
        return getKey(key, ref, currentVersion, storageURL, entityFactory);
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
        Objects.requireNonNull(key, KEY_CANNOT_BE_NULL);
        Objects.requireNonNull(entityFactory, ENTITY_FACTORY_CANNOT_BE_NULL);
        if (!key.endsWith("/")) {
            throw new IllegalArgumentException(String.format("%s must end with / to be able to perform list operation", key));
        }
        final URIBuilder uriBuilder = resolve(key, storageURL);
        APIHelper.addRefParameter(Utils.checkRef(ref), uriBuilder);
        if (recursive) {
            uriBuilder.addParameter(JitStaticClientImpl.RECURSIVE, "true");
        }
        if (light) {
            uriBuilder.addParameter(JitStaticClientImpl.LIGHT, "true");
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

    @Override
    public String modifyUser(String user, String ref, UserData data, String currentVersion) throws URISyntaxException, IOException {
        return modifyKey(user, ref, currentVersion, keyUserURL, new UserDataEntity(data));
    }

    @Override
    public <T> T getUser(String user, String ref, String currentVersion, TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, IOException {
        return getKey(user, ref, currentVersion, keyUserURL, entityFactory);
    }

    @Override
    public String addUser(String user, String ref, UserData data) throws URISyntaxException, IOException {
        return addKey(user, ref, keyUserURL, new UserDataEntity(data));
    }

    @Override
    public void deleteUser(String user, String ref) throws URISyntaxException, IOException {
        deleteUser(user, ref, keyUserURL);
    }

    @Override
    public <T> T getAdminUser(String user, String ref, String currentVersion, TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, IOException {
        return getKey(user, ref, currentVersion, keyAdminURL, entityFactory);
    }

    @Override
    public String addAdminUser(String user, String ref, UserData data) throws URISyntaxException, IOException {
        return addKey(user, ref, keyAdminURL, new UserDataEntity(data));
    }

    @Override
    public String modifyAdminUser(String user, String ref, UserData data, String currentVersion)
            throws URISyntaxException, IOException {
        return modifyKey(user, ref, currentVersion, keyAdminURL, new UserDataEntity(data));
    }

    @Override
    public void deleteAdminUser(String user, String ref) throws URISyntaxException, IOException {
        deleteUser(user, ref, keyAdminURL);
    }

    @Override
    public <T> T getGitUser(String user, TriFunction<InputStream, String, String, T> entityFactory) throws URISyntaxException, IOException {
        return getGitUser(user, null, entityFactory);
    }

    @Override
    public <T> T getGitUser(String user, String currentVersion, TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, IOException {
        return getKey(user, null, currentVersion, keyGitUserURL, entityFactory);
    }

    @Override
    public String addGitUser(String user, UserData userData) throws URISyntaxException, IOException {
        return addKey(user, null, keyGitUserURL, new UserDataEntity(userData));
    }

    @Override
    public String modifyGitUser(String user, UserData userData, String currentVersion) throws URISyntaxException, IOException {
        return modifyKey(user, null, currentVersion, keyGitUserURL, new UserDataEntity(userData));
    }

    @Override
    public void deleteGitUser(String user) throws URISyntaxException, IOException {
        deleteUser(user, null, keyGitUserURL);
    }

    private String modifyKey(String key, String ref, String version, URI realm, HttpEntity entity)
            throws URISyntaxException, IOException {
        Objects.requireNonNull(key, KEY_CANNOT_BE_NULL);
        Objects.requireNonNull(entity, "entity cannot be null");
        Objects.requireNonNull(version, "version cannot be null");

        final URIBuilder uriBuilder = resolve(key, realm);
        APIHelper.addRefParameter(Utils.checkRef(ref), uriBuilder);
        final URI uri = uriBuilder.build();
        final HttpPut putRequest = new HttpPut(uri);
        putRequest.setHeaders(HEADERS);
        putRequest.addHeader(HttpHeaders.IF_MATCH, APIHelper.checkVersion(version));

        final HttpClientContext context = getHostContext(target, credentialsProvider);
        putRequest.setEntity(entity);
        try (final CloseableHttpResponse httpResponse = client.execute(putRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            APIHelper.checkPUTStatusCode(uri, putRequest, statusLine, httpResponse.getEntity());
            return APIHelper.getSingleHeader(httpResponse, HttpHeaders.ETAG);
        }
    }

    private String addKey(String key, String ref, URI realm, HttpEntity entity) throws URISyntaxException, IOException {
        Objects.requireNonNull(key, KEY_CANNOT_BE_NULL);
        Objects.requireNonNull(entity, "entity cannot be null");

        final URIBuilder uriBuilder = resolve(key, realm);
        APIHelper.addRefParameter(Utils.checkRef(ref), uriBuilder);
        final URI url = uriBuilder.build();
        final HttpPost postRequest = new HttpPost(url);
        postRequest.setHeaders(HEADERS);
        final HttpClientContext context = getHostContext(target, credentialsProvider);

        postRequest.setEntity(entity);
        try (final CloseableHttpResponse httpResponse = client.execute(postRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            checkPOStStatusCode(postRequest, statusLine, httpResponse.getEntity());
            return APIHelper.getSingleHeader(httpResponse, HttpHeaders.ETAG);
        }
    }

    private <T> T getKey(String key, String ref, String currentVersion, URI realm, TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, IOException {
        Objects.requireNonNull(key, KEY_CANNOT_BE_NULL);
        Objects.requireNonNull(entityFactory, ENTITY_FACTORY_CANNOT_BE_NULL);
        Objects.requireNonNull(realm, "realm cannot be null");

        final URIBuilder uriBuilder = resolve(key, realm);
        APIHelper.addRefParameter(Utils.checkRef(ref), uriBuilder);
        final URI url = uriBuilder.build();
        final HttpGet getRequest = new HttpGet(url);
        getRequest.setHeaders(HEADERS);
        if (currentVersion != null) {
            getRequest.addHeader(HttpHeaders.IF_NONE_MATCH, APIHelper.escapeVersion(currentVersion));
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

    private void deleteUser(String user, String ref2, URI realm) throws URISyntaxException, IOException {
        final URIBuilder uriBuilder = resolve(user, realm);
        APIHelper.addRefParameter(Utils.checkRef(ref2), uriBuilder);
        final URI url = uriBuilder.build();
        final HttpDelete deleteRequest = new HttpDelete(url);
        deleteRequest.setHeaders(HEADERS);
        final HttpClientContext context = getHostContext(target, credentialsProvider);
        try (final CloseableHttpResponse httpResponse = client.execute(deleteRequest, context)) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            APIHelper.checkDELETEresponse(url, deleteRequest, statusLine, httpResponse.getEntity());
        }
    }
}
