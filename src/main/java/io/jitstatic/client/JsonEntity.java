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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;

abstract class JsonEntity implements HttpEntity {

    protected static final Charset UTF_8 = StandardCharsets.UTF_8;
    protected static final byte[] LEFTBRACKET = getBytes("{");
    protected static final byte[] RIGHTBRACKET = getBytes("}");
    protected static final byte[] COLON = getBytes(":");
    protected static final byte[] DOUBLEQUOTE = getBytes("\"");
    protected static final byte[] COMMA = getBytes(",");
    protected static final byte[] LEFTSQBRACKET = getBytes("[");
    protected static final byte[] RIGHTSQBRACKET = getBytes("]");
    private static final byte[] TRUE = getBytes("true");
    private static final byte[] FALSE = getBytes("false");
    private static final byte[] NIL = new byte[0];

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
        return new BasicHeader(HttpHeaders.CONTENT_ENCODING, UTF_8.name());
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
        return tokens != null ? tokens.getBytes(UTF_8) : NIL;        
    }

    protected void writeBool(byte[] field, boolean value, OutputStream o) throws IOException {
        o.write(DOUBLEQUOTE);
        o.write(field);
        o.write(DOUBLEQUOTE);
        o.write(COLON);
        o.write((value ? TRUE : FALSE));
    }

}
