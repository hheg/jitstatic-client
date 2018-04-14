package io.jitstatic.client;

/*-
 * #%L
 * jitstatic client
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
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;

abstract class Entity implements HttpEntity {

    protected static final String UTF_8 = "UTF-8";
    protected static final byte[] LEFTBRACKET = getBytes("{");
    protected static final byte[] RIGHTBRACKET = getBytes("}");
    protected static final byte[] COLON = getBytes(":");
    protected static final byte[] DOUBLEQUOTE = getBytes("\"");
    protected static final byte[] COMMA = getBytes(",");
    protected static final byte[] USERINFO = getBytes("userInfo");
    protected static final byte[] USERMAIL = getBytes("userMail");
    protected static final byte[] MESSAGE = getBytes("message");
    protected final AtomicBoolean bool = new AtomicBoolean(false);

    protected void writeField(final byte[] field, final String value, final OutputStream o) throws IOException {
        writeFieldToStream(field, getBytes(value), o);
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
        return new BasicHeader(HttpHeaders.CONTENT_ENCODING, UTF_8);
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("getContent is unsupported");
    }

    @Override
    public boolean isStreaming() {
        return bool.get();
    }
    
    @Deprecated
    @Override
    public void consumeContent() throws IOException {
        throw new UnsupportedOperationException("consumeContent is unsupported");
    }
    
    private void writeFieldToStream(final byte[] field, final byte[] value, final OutputStream o) throws IOException {
        o.write(DOUBLEQUOTE);
        o.write(field);
        o.write(DOUBLEQUOTE);
        o.write(COLON);
        o.write(DOUBLEQUOTE);
        o.write(value);
        o.write(DOUBLEQUOTE);
    }

    protected static byte[] getBytes(final String tokens) {
        try {
            return tokens.getBytes(UTF_8);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
