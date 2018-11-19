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

import java.util.Objects;

public class CommitData {

    private final String branch;
    private final String key;
    private final String message;
    private final String user;
    private final String userMail;

    public CommitData(final String key, final String message, final String userInfo, final String userMail) {
        this(key, null, message, userInfo, userMail);
    }

    public CommitData(final String key, final String branch, final String message, final String userInfo, final String userMail) {
        this.branch = Utils.checkRef(branch);
        this.key = Objects.requireNonNull(key);
        this.message = Objects.requireNonNull(message);
        this.user = Objects.requireNonNull(userInfo);
        this.userMail = Objects.requireNonNull(userMail);
    }

    public final String getBranch() {
        return branch;
    }

    public final String getKey() {
        return key;
    }

    public final String getMessage() {
        return message;
    }

    public final String getUserInfo() {
        return user;
    }

    public final String getUserMail() {
        return userMail;
    }
}
