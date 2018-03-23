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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;

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

import io.jitstatic.client.JitStaticUpdaterClient.JitStaticUpdaterClientBuilder;

public class JitStaticUpdaterClientTest {

    @Rule
    public ExpectedException ex = ExpectedException.none();

    @Test
    public void testJitStaticUpdaterClientGetKey() throws UnsupportedOperationException, IOException, URISyntaxException, APIException {
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

        JitStaticUpdaterClient client = JitStaticUpdaterClientBuilder.create().setAppContext("/app/").setHost("localhost").setPort(80)
                .setUser("user").setPassword("pass").setScheme("http").setHttpClientBuilder(clientBuilderMock).build();

        Entity entity = client.getKey("key", null, "application/test", entityFactory);
        assertNotNull(entity);
        assertEquals("application/test", entity.getContentType());
        assertEquals("\"1234\"", entity.getTag());
        assertArrayEquals(data, entity.getData());
    }

    @Test
    public void testJitStaticUpdaterClientPutKey() throws UnsupportedOperationException, IOException, URISyntaxException, APIException {
        final byte[] data = new byte[] { 1 };
        HttpClientBuilder clientBuilderMock = Mockito.mock(HttpClientBuilder.class);
        CloseableHttpClient clientMock = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse closableResponseMock = Mockito.mock(CloseableHttpResponse.class);
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);

        Mockito.when(closableResponseMock.getHeaders(Mockito.eq(HttpHeaders.ETAG)))
                .thenReturn(new Header[] { new BasicHeader(HttpHeaders.ETAG, "\"1234\"") });
        Mockito.when(closableResponseMock.getHeaders(Mockito.eq(HttpHeaders.CONTENT_TYPE)))
                .thenReturn(new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/test") });
        Mockito.when(statusLineMock.getReasonPhrase()).thenReturn("OK");
        Mockito.when(statusLineMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(closableResponseMock.getStatusLine()).thenReturn(statusLineMock);
        Mockito.when(clientMock.execute(Mockito.any(HttpUriRequest.class), Mockito.any(HttpContext.class)))
                .thenReturn(closableResponseMock);
        Mockito.when(clientBuilderMock.build()).thenReturn(clientMock);

        JitStaticUpdaterClient client = JitStaticUpdaterClientBuilder.create().setAppContext("/app/").setHost("localhost").setPort(80)
                .setUser("user").setPassword("pass").setScheme("http").setHttpClientBuilder(clientBuilderMock).build();

        Entity entity = client.modifyKey(data, new CommitData("master", "key", "message", "user", "mail"), "4321", "application/test",
                entityFactory);

        assertNotNull(entity);
        assertEquals("application/test", entity.getContentType());
        assertEquals("\"1234\"", entity.getTag());
        assertArrayEquals(new byte[] { 0 }, entity.getData());
    }

    @Test
    public void testJitStaticUpdaterClientGetKeyNotFound() throws ClientProtocolException, URISyntaxException, IOException, APIException {
        ex.expect(APIException.class);
        ex.expectMessage("http://localhost:80/app/key failed with: 404 NOT FOUND");
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
        Mockito.when(statusLineMock.getReasonPhrase()).thenReturn("NOT FOUND");
        Mockito.when(statusLineMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(closableResponseMock.getStatusLine()).thenReturn(statusLineMock);
        Mockito.when(clientMock.execute(Mockito.any(HttpUriRequest.class), Mockito.any(HttpContext.class)))
                .thenReturn(closableResponseMock);
        Mockito.when(clientBuilderMock.build()).thenReturn(clientMock);

        JitStaticUpdaterClient client = JitStaticUpdaterClientBuilder.create().setAppContext("/app/").setHost("localhost").setPort(80)
                .setUser("user").setPassword("pass").setScheme("http").setHttpClientBuilder(clientBuilderMock).build();

        client.getKey("key", null, "application/test", entityFactory);
    }

    @Test
    public void testJitStaticUpdaterClientPutKeyError()
            throws UnsupportedOperationException, IOException, URISyntaxException, APIException {
        ex.expect(APIException.class);
        ex.expectMessage("http://localhost:80/app/key?ref=refs%2Fheads%2Fmaster failed with: 412 PRECONDITION FAILED");
        final byte[] data = new byte[] { 1 };
        HttpClientBuilder clientBuilderMock = Mockito.mock(HttpClientBuilder.class);
        CloseableHttpClient clientMock = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse closableResponseMock = Mockito.mock(CloseableHttpResponse.class);
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);

        Mockito.when(closableResponseMock.getHeaders(Mockito.eq(HttpHeaders.ETAG)))
                .thenReturn(new Header[] { new BasicHeader(HttpHeaders.ETAG, "\"1234\"") });
        Mockito.when(closableResponseMock.getHeaders(Mockito.eq(HttpHeaders.CONTENT_TYPE)))
                .thenReturn(new Header[] { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/test") });
        Mockito.when(statusLineMock.getReasonPhrase()).thenReturn("PRECONDITION FAILED");
        Mockito.when(statusLineMock.getStatusCode()).thenReturn(HttpStatus.SC_PRECONDITION_FAILED);
        Mockito.when(closableResponseMock.getStatusLine()).thenReturn(statusLineMock);
        Mockito.when(clientMock.execute(Mockito.any(HttpUriRequest.class), Mockito.any(HttpContext.class)))
                .thenReturn(closableResponseMock);
        Mockito.when(clientBuilderMock.build()).thenReturn(clientMock);

        JitStaticUpdaterClient client = JitStaticUpdaterClientBuilder.create().setAppContext("/app/").setHost("localhost").setPort(80)
                .setUser("user").setPassword("pass").setScheme("http").setHttpClientBuilder(clientBuilderMock).build();

        client.modifyKey(data, new CommitData("master", "key", "message", "user", "mail"), "4321", "application/test", entityFactory);
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

    private static class Entity extends BaseEntity {

        private final byte[] data;

        public Entity(String tag, String contentType, byte[] data) {
            super(tag, contentType);
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

}
