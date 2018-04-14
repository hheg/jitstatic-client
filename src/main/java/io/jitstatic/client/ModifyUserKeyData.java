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

import java.util.Objects;

public class ModifyUserKeyData {

    private final String message;
    private final MetaData data;
    private final String userMail;
    private final String userInfo;

    public ModifyUserKeyData(final MetaData data, final String message, final String userMail, final String userInfo) {
        this.data = Objects.requireNonNull(data);
        this.message = Objects.requireNonNull(message);
        this.userMail = Objects.requireNonNull(userMail);
        this.userInfo = Objects.requireNonNull(userInfo);
    }
    
    public String getMessage() {
        return message;
    }

    public MetaData getStorageData() {
        return data;
    }
    
    public String getUserMail() {
        return userMail;
    }

    public String getUserInfo() {
        return userInfo;
    }
}
