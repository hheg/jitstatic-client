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
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;

import io.jitstatic.client.MetaData.User;

class AddKeyEntity extends KeyEntity {

    private static final byte[] KEY = getBytes("key");
    private static final byte[] BRANCH = getBytes("branch");
    private static final byte[] METADATA = getBytes("metaData");
    private static final byte[] USERS = getBytes("users");
    private static final byte[] PASSWORD = getBytes("password");
    private static final byte[] CONTENTTYPE = getBytes("contentType");
    private static final byte[] LEFTSQBRACKET = getBytes("[");
    private static final byte[] RIGHTSQBRACKET = getBytes("]");

    private final CommitData commitData;
    private final MetaData medaData;

    private final AtomicBoolean bool = new AtomicBoolean(false);
    private final InputStream data;

    public AddKeyEntity(final InputStream is, final CommitData commitData, final MetaData metaData) {
        this.medaData = metaData;
        this.commitData = commitData;
        this.data = is;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public Header getContentType() {
        return new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    }

    @Override
    public Header getContentEncoding() {
        return new BasicHeader(HttpHeaders.CONTENT_ENCODING, "UTF-8");
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("getContent is unsupported");
    }

    @Override
    public void writeTo(final OutputStream o) throws IOException {
        bool.set(true);
        try {
            o.write(LEFTBRACKET);
            writeField(KEY, commitData.getKey(), o);
            o.write(COMMA);
            writeField(BRANCH, commitData.getBranch(), o);
            o.write(COMMA);
            writeField(MESSAGE, commitData.getMessage(), o);
            o.write(COMMA);
            writeField(USERINFO, commitData.getUserInfo(), o);
            o.write(COMMA);
            writeField(USERMAIL, commitData.getUserMail(), o);
            o.write(COMMA);
            writeMetaDataField(o);
            o.write(COMMA);
            writeDataField(o);
            o.write(RIGHTBRACKET);
        } finally {
            bool.set(false);
        }
    }

    private void writeMetaDataField(final OutputStream o) throws IOException {
        o.write(DOUBLEQUOTE);
        o.write(METADATA);
        o.write(DOUBLEQUOTE);
        o.write(COLON);
        o.write(LEFTBRACKET);
        o.write(DOUBLEQUOTE);
        o.write(USERS);
        o.write(DOUBLEQUOTE);
        o.write(COLON);
        o.write(LEFTSQBRACKET);
        writeUsers(o);
        o.write(RIGHTSQBRACKET);
        o.write(COMMA);
        writeField(CONTENTTYPE,medaData.getContentType(),o);
        o.write(RIGHTBRACKET);
    }

    private void writeUsers(final OutputStream o) throws IOException {
        final Set<User> users = medaData.getUsers();
        byte[] b = new byte[0];
        for (User user : users) {
            o.write(b);
            o.write(LEFTBRACKET);
            writeField(USER, user.getUser(), o);
            o.write(COMMA);
            writeField(PASSWORD, user.getPassword(), o);
            o.write(RIGHTBRACKET);
            b = COMMA;
        }
    }

    @Override
    public boolean isStreaming() {
        return bool.get();
    }

    @Override
    public void consumeContent() throws IOException {
    }

    @Override
    protected void writeData(final OutputStream o) throws IOException {
        int read = 0;
        byte[] buf = new byte[4096];
        while ((read = data.read(buf)) != -1) {
            if (read != buf.length) {
                buf = Arrays.copyOf(buf, read);
            }
            byte[] encode = ENCODER.encode(buf);
            o.write(encode, 0, encode.length);
        }

    }
}