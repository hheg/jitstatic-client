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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpEntity;

abstract class KeyEntity implements HttpEntity {

    protected static final String UTF_8 = "UTF-8";
    static final byte[] LEFTBRACKET = getBytes("{");
    static final byte[] RIGHTBRACKET = getBytes("}");
    static final byte[] COLON = getBytes(":");
    static final byte[] DOUBLEQUOTE = getBytes("\"");
    static final byte[] USER = getBytes("user");
    static final byte[] USERINFO = getBytes("userInfo");
    static final byte[] USERMAIL = getBytes("userMail");
    static final byte[] MESSAGE = getBytes("message");
    static final byte[] COMMA = getBytes(",");
    private static final byte[] DATA = getBytes("data");

    protected static final Encoder ENCODER = Base64.getEncoder();
    protected final AtomicBoolean bool = new AtomicBoolean(false);

    protected void writeDataField(final OutputStream o) throws IOException {
        o.write(DOUBLEQUOTE);
        o.write(DATA);
        o.write(DOUBLEQUOTE);
        o.write(COLON);
        o.write(DOUBLEQUOTE);
        writeData(o);
        o.write(DOUBLEQUOTE);
    }

    protected abstract void writeData(final OutputStream o) throws IOException;

    protected void writeField(final byte[] field, final String value, final OutputStream o) throws IOException {
        writeFieldToStream(field, getBytes(value), o);
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
