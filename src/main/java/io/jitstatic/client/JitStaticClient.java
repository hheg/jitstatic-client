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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Function;

public interface JitStaticClient extends AutoCloseable {

    String createKey(byte[] data, CommitData commitData, MetaData metaData) throws  IOException, APIException, URISyntaxException;

    String createKey(InputStream data, CommitData commitData, MetaData metaData) throws  IOException, APIException, URISyntaxException;

    <T> T getMetaKey(String key, String ref, TriFunction<InputStream, String, String, T> entityFactory)
            throws  URISyntaxException, IOException;

    <T> T getMetaKey(String key, String ref, String version, TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException,  IOException;

    String modifyMetaKey(String key, String ref, String version, ModifyUserKeyData data) throws  IOException, URISyntaxException;

    String modifyKey(byte[] data, CommitData commitData, String version) throws URISyntaxException, IOException, APIException;

    String modifyKey(InputStream data, CommitData commitData, String version) throws URISyntaxException, IOException, APIException;

    <T> T getKey(String key, TriFunction<InputStream, String, String, T> entityFactory) throws URISyntaxException, IOException, APIException;

    <T> T getKey(String key, String ref, TriFunction<InputStream, String, String, T> entityFactory) throws URISyntaxException, IOException, APIException;

    <T> T getKey(String key, TriFunction<InputStream, String, String, T> entityFactory, String currentVersion)
            throws URISyntaxException, IOException, APIException;

    <T> T getKey(String key, String ref, String currentVersion, TriFunction<InputStream, String, String, T> entityFactory)
            throws URISyntaxException, IOException, APIException;

    <T> T listAll(String key, Function<InputStream, T> entityFactory) throws URISyntaxException, IOException;

    <T> T listAll(String key, String ref, Function<InputStream, T> entityFactory) throws URISyntaxException, IOException;

    <T> T listAll(String key, boolean recursive, Function<InputStream, T> entityFactory) throws URISyntaxException, IOException;

    <T> T listAll(String key, String ref, boolean recursive, Function<InputStream, T> entityFactory) throws URISyntaxException, IOException;

    <T> T listAll(String key, boolean recursive, boolean light, Function<InputStream, T> entityFactory) throws URISyntaxException, IOException;

    <T> T listAll(String key, String ref, boolean recursive, boolean light, Function<InputStream, T> entityFactory) throws URISyntaxException, IOException;

    <T> T search(List<BulkSearch> search, Function<InputStream, T> entityFactory) throws URISyntaxException, IOException;

    void delete(CommitData commitData) throws URISyntaxException, APIException, IOException;

    <T> T getUser(String user, String ref, String currentVersion, TriFunction<InputStream, String, String, T> entityFactory) throws  URISyntaxException, IOException;

    String addUser(String user, String ref, UserData data) throws  URISyntaxException, IOException;

    String modifyUser(String user, String ref, UserData data, String currentVersion) throws  URISyntaxException, IOException;

    void deleteUser(String user, String ref) throws  URISyntaxException, IOException;

    <T> T getAdminUser(String user, String ref, String currentVersion, TriFunction<InputStream, String, String, T> entityFactory) throws  URISyntaxException, IOException;

    String addAdminUser(String user, String ref, UserData data) throws  URISyntaxException, IOException;

    String modifyAdminUser(String user, String ref, UserData data, String currentVersion) throws  URISyntaxException, IOException;

    void deleteAdminUser(String user, String ref) throws  URISyntaxException, IOException;

    void close();

    static JitStaticClientBuilder create() {
        return new JitStaticClientBuilder();
    }

}
