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
import java.util.Base64;
import java.util.Base64.Encoder;

import org.apache.commons.codec.binary.Base64OutputStream;

abstract class KeyEntity extends MetaDataEntity {

    private final InputStream dataStream;
    private static final byte[] CHUNK_SEPARATOR = { '\r', '\n' };

    public KeyEntity(final MetaData data, InputStream dataStream) {
        super(data);
        this.dataStream = dataStream;
    }

    private static final byte[] DATA = getBytes("data");

    protected static final Encoder ENCODER = Base64.getEncoder();

    protected void writeDataField(final OutputStream o) throws IOException {
        o.write(DOUBLEQUOTE);
        o.write(DATA);
        o.write(DOUBLEQUOTE);
        o.write(COLON);
        o.write(DOUBLEQUOTE);
        writeData(o);
        o.write(DOUBLEQUOTE);
    }

    protected void writeData(final OutputStream o) throws IOException {
        int read = 0;
        byte[] buf = new byte[4096];
        try (Base64OutputStream b64 = new Base64OutputStream(new NoCloseWrappingOutputStream(o), true, -1, CHUNK_SEPARATOR);) {
            while ((read = dataStream.read(buf)) != -1) {
                b64.write(buf, 0, read);
            }
        }
    }

}
