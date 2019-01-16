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

public class ModifyKeyEntity extends KeyEntity {

    private final String message;
    private final String userMail;
    private final String userInfo;
    
    public ModifyKeyEntity(final InputStream data, final String message, final String userInfo, final String userMail) {
        super(null, data);
        this.message = message;
        this.userInfo = userInfo;
        this.userMail = userMail;
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
}
