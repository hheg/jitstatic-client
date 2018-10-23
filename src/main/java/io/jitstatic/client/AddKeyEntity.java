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

class AddKeyEntity extends KeyEntity {

    private final CommitData commitData;

    private final InputStream data;

    public AddKeyEntity(final InputStream is, final CommitData commitData, final MetaData metaData) {
        super(metaData);
        this.commitData = commitData;
        this.data = is;
    }

    @Override
    public void writeTo(final OutputStream o) throws IOException {
        bool.set(true);
        try {
            o.write(LEFTBRACKET);            
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