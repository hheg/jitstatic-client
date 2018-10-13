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
import java.io.OutputStream;
import java.util.Objects;

class ModifyUserKeyEntity extends MetaDataEntity {

    private final String message;
    private final String userInfo;
    private final String userMail;

    public ModifyUserKeyEntity(final ModifyUserKeyData data) {
        super(data.getStorageData());
        this.message = Objects.requireNonNull(data.getMessage());
        this.userInfo = Objects.requireNonNull(data.getUserInfo());
        this.userMail = Objects.requireNonNull(data.getUserMail());
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
            writeMetaDataField(o);
            o.write(RIGHTBRACKET);
        } finally {
            bool.set(false);
        }
    }
}
