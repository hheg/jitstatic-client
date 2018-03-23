package io.jitstatic.client;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;

public interface JitStaticCreatorClient extends AutoCloseable {

    <T extends BaseEntity> T createKey(byte[] data, CommitData commitData, MetaData metaData,
            TriFunction<InputStream, String, String, T> entityFactory) throws ClientProtocolException, IOException, APIException;

    <T extends BaseEntity> T createKey(InputStream data, CommitData commitData, MetaData metaData,
            TriFunction<InputStream, String, String, T> entityFactory) throws ClientProtocolException, IOException, APIException;

    static JitStaticCreatorClientBuilder create() {
        return new JitStaticCreatorClientBuilder();
    }

}