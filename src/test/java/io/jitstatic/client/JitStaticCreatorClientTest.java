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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import io.jitstatic.client.MetaData.User;

public class JitStaticCreatorClientTest {

    @Rule
    public ExpectedException ex = ExpectedException.none();

    @Test
    public void testJitStaticCreatorClient() throws URISyntaxException, ClientProtocolException, IOException, APIException {
        final byte[] data = new byte[] { 1 };
        HttpClientBuilder clientBuilderMock = Mockito.mock(HttpClientBuilder.class);
        CloseableHttpClient clientMock = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse closableResponseMock = Mockito.mock(CloseableHttpResponse.class);
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        HttpEntity entityMock = Mockito.mock(HttpEntity.class);
        Mockito.when(entityMock.getContent()).thenReturn(new ByteArrayInputStream(data));
        Mockito.when(closableResponseMock.getHeaders(Mockito.eq(HttpHeaders.ETAG)))
                .thenReturn(new Header[] { new BasicHeader(HttpHeaders.ETAG, "\"1234\"") });
        Mockito.when(closableResponseMock.getHeaders(Mockito.eq(HttpHeaders.CONTENT_TYPE)))
                .thenReturn(new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/test") });
        Mockito.when(closableResponseMock.getEntity()).thenReturn(entityMock);
        Mockito.when(statusLineMock.getReasonPhrase()).thenReturn("OK");
        Mockito.when(statusLineMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(closableResponseMock.getStatusLine()).thenReturn(statusLineMock);
        Mockito.when(clientMock.execute(Mockito.any(HttpUriRequest.class), Mockito.any(HttpContext.class)))
                .thenReturn(closableResponseMock);
        Mockito.when(clientBuilderMock.build()).thenReturn(clientMock);

        JitStaticCreatorClient client = JitStaticCreatorClient.create().setAppContext("/app/").setHost("localhost").setPort(80)
                .setUser("user").setPassword("pass").setScheme("http").setHttpClientBuilder(clientBuilderMock).build();
        Set<User> users = new HashSet<>();
        users.add(new User("user", "pass"));

        String entity = client.createKey(data, new CommitData("key", "master", "message", "user", "mail"),
                new MetaData(users, "application/test"));
        assertNotNull(entity);        
    }

    @Test
    public void testJitStaticCreatorClientPOSTError() throws UnsupportedOperationException, IOException, URISyntaxException, APIException {
        ex.expect(APIException.class);
        ex.expectMessage("http://localhost:80/app/storage/ failed with: 500 Test error");
        final byte[] data = new byte[] { 1 };
        HttpClientBuilder clientBuilderMock = Mockito.mock(HttpClientBuilder.class);
        CloseableHttpClient clientMock = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse closableResponseMock = Mockito.mock(CloseableHttpResponse.class);
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        HttpEntity entityMock = Mockito.mock(HttpEntity.class);
        Mockito.when(entityMock.getContent()).thenReturn(new ByteArrayInputStream(data));
        Mockito.when(closableResponseMock.getHeaders(Mockito.eq(HttpHeaders.ETAG)))
                .thenReturn(new Header[] { new BasicHeader(HttpHeaders.ETAG, "\"1234\"") });
        Mockito.when(closableResponseMock.getHeaders(Mockito.eq(HttpHeaders.CONTENT_TYPE)))
                .thenReturn(new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/test") });
        Mockito.when(closableResponseMock.getEntity()).thenReturn(entityMock);
        Mockito.when(statusLineMock.getReasonPhrase()).thenReturn("Test error");
        Mockito.when(statusLineMock.getStatusCode()).thenReturn(500);
        Mockito.when(closableResponseMock.getStatusLine()).thenReturn(statusLineMock);
        Mockito.when(clientMock.execute(Mockito.any(HttpUriRequest.class), Mockito.any(HttpContext.class)))
                .thenReturn(closableResponseMock);
        Mockito.when(clientBuilderMock.build()).thenReturn(clientMock);

        try (JitStaticCreatorClient client = JitStaticCreatorClient.create().setAppContext("/app/").setHost("localhost").setPort(80)
                .setUser("user").setPassword("pass").setScheme("http").setHttpClientBuilder(clientBuilderMock).build();) {
            Set<User> users = new HashSet<>();
            users.add(new User("user", "pass"));

            client.createKey(data, new CommitData("key", "master", "message", "user", "mail"), new MetaData(users, "application/test"));
            fail();
        }

    }

    @Test
    public void testGetUserKey() throws UnsupportedOperationException, IOException, URISyntaxException {
        final byte[] data = new byte[] { 1 };
        HttpClientBuilder clientBuilderMock = Mockito.mock(HttpClientBuilder.class);
        CloseableHttpClient clientMock = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse closableResponseMock = Mockito.mock(CloseableHttpResponse.class);
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        HttpEntity entityMock = Mockito.mock(HttpEntity.class);
        Mockito.when(entityMock.getContent()).thenReturn(new ByteArrayInputStream(data));
        Mockito.when(closableResponseMock.getHeaders(Mockito.eq(HttpHeaders.ETAG)))
                .thenReturn(new Header[] { new BasicHeader(HttpHeaders.ETAG, "\"1234\"") })
                .thenReturn(new Header[] { new BasicHeader(HttpHeaders.ETAG, "\"4321\"") });
        Mockito.when(closableResponseMock.getHeaders(Mockito.eq(HttpHeaders.CONTENT_TYPE)))
                .thenReturn(new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/test") });
        Mockito.when(closableResponseMock.getEntity()).thenReturn(entityMock);
        Mockito.when(statusLineMock.getReasonPhrase()).thenReturn("OK");
        Mockito.when(statusLineMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(closableResponseMock.getStatusLine()).thenReturn(statusLineMock);
        Mockito.when(clientMock.execute(Mockito.any(HttpUriRequest.class), Mockito.any(HttpContext.class)))
                .thenReturn(closableResponseMock);
        Mockito.when(clientBuilderMock.build()).thenReturn(clientMock);

        try (JitStaticCreatorClient client = JitStaticCreatorClient.create().setAppContext("/app/").setHost("localhost").setPort(80)
                .setUser("user").setPassword("pass").setScheme("http").setHttpClientBuilder(clientBuilderMock).build();) {
            Entity entity = client.getMetaKey("key", null, entityFactory);
            assertNotNull(entity);
            assertEquals("application/test", entity.getContentType());
            assertEquals("\"1234\"", entity.getTag());
            User u = new User("user", "pass");
            Set<User> users = new HashSet<>();
            users.add(u);
            String newVersion = client.modifyMetaKey("key", null, entity.getTag(),
                    new ModifyUserKeyData(new MetaData(users, "application/test2"), "msg", "mail", "info"));
            assertNotNull(newVersion);
            assertNotEquals(entity.getTag(), newVersion);
        }
    }

    private TriFunction<InputStream, String, String, Entity> entityFactory = (is, version, content) -> {
        byte[] b = new byte[1];
        try {
            is.read(b);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new Entity(version, content, b);
    };

    private static class Entity {

        private final String contentType;
        private final String tag;

        public Entity(String tag, String contentType, byte[] data) {
            this.tag = tag;
            this.contentType = contentType;
        }

        public String getContentType() {
            return contentType;
        }

        public String getTag() {
            return tag;
        }
    }
}
