package io.jitstatic.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;

public interface JitStaticUpdaterClient extends AutoCloseable {

    <T extends BaseEntity> T modifyKey(byte[] data, CommitData commitData, String version, String contentType,
            TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, ClientProtocolException, IOException, APIException;

    <T extends BaseEntity> T modifyKey(InputStream data, CommitData commitData, String version, String contentType,
            TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, ClientProtocolException, IOException, APIException;

    <T extends BaseEntity> T getKey(String key, String contenttype, TriFunction<InputStream, String, String, T> entityFactory)
            throws ClientProtocolException, URISyntaxException, IOException, APIException;

    <T extends BaseEntity> T getKey(String key, String ref, String contentType, TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, ClientProtocolException, IOException, APIException;

    static JitStaticUpdaterClientBuilder create() {
        return new JitStaticUpdaterClientBuilder();
    }

}