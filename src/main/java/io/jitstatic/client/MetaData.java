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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class MetaData {

    private final Set<User> users;
    private final String contentType;

    public MetaData(final String contenttype) {
        this(new HashSet<>(), contenttype);
    }

    public MetaData(final Set<User> users, final String contentType) {
        this.users = Collections.unmodifiableSet(new HashSet<>(users));
        this.contentType = contentType;
    }

    public final Set<User> getUsers() {
        return users;
    }

    public final String getContentType() {
        return contentType;
    }

    public static final class User {
        private final String user;
        private final String password;

        public User(final String user, final String password) {
            this.user = Objects.requireNonNull(user);
            this.password = Objects.requireNonNull(password);
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((user == null) ? 0 : user.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            User other = (User) obj;
            if (user == null) {
                if (other.user != null)
                    return false;
            } else if (!user.equals(other.user))
                return false;
            return true;
        }

    }
}
