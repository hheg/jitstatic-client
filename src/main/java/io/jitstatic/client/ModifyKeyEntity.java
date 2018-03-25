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

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;


class ModifyKeyEntity extends KeyEntity {

    private final String message;
    private final InputStream data;
    private final String userMail;
    private final String userInfo;
    private final String contentType;
    
    public ModifyKeyEntity(final InputStream data, final String message, final String userInfo, final String userMail) {
        this.data = data;
        this.message = message;
        this.userInfo = userInfo;
        this.userMail = userMail;
        this.contentType = "application/json";
    }

    public String getMessage() {
        return message;
    }

    public String getUserMail() {
        return userMail;
    }

    public String getUserInfo() {
        return userInfo;
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
        return -1; // For now we don't know the content length
    }

    @Override
    public Header getContentType() {
        return new BasicHeader(HttpHeaders.CONTENT_TYPE, contentType);
    }

    @Override
    public Header getContentEncoding() {
        return new BasicHeader(HttpHeaders.CONTENT_ENCODING, UTF_8);
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("this is a send entity");
    }

    @Override
    public void writeTo(final OutputStream o) throws IOException {        
        bool.set(true);
        try {
            o.write(LEFTBRACKET);
            writeField(MESSAGE, message, o);
            o.write(COMMA);
            writeField(USERINFO, userInfo, o);
            o.write(COMMA);
            writeField(USERMAIL, userMail, o);
            o.write(COMMA);
            writeDataField(o);
            o.write(RIGHTBRACKET);
        } finally {
            bool.set(false);
        }
    }

    protected void writeData(final OutputStream o) throws IOException {
        int read = 0;
        byte[] buf = new byte[4096];
        while ((read = data.read(buf)) != -1) {
            if(read != buf.length) {
                buf = Arrays.copyOf(buf, read);
            }
            byte[] encode = ENCODER.encode(buf);
            o.write(encode, 0, encode.length);                
        }
    }

    @Override
    public boolean isStreaming() {
        return bool.get();
    }

    @Override
    public void consumeContent() throws IOException {
    }
}
