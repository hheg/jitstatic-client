package io.jitstatic.client;

import java.util.ArrayList;

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
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MetaData {

    private final Set<User> users;
    private final String contentType;
    private final boolean hidden;
    private final boolean isProtected;
    private final List<HeaderPair> headers;
    private final Set<Role> read;
    private final Set<Role> write;

    public MetaData(final String contenttype) {
        this(new HashSet<>(), contenttype);
    }

    public MetaData(final Set<User> users, final String contentType, final List<HeaderPair> headers) {
        this(users, contentType, false, false, headers);
    }

    public MetaData(final Set<User> users, final String contentType, final boolean isProtected, final boolean hidden, final List<HeaderPair> headers,
            final Set<Role> read, Set<Role> write) {
        this.users = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNull(users)));
        this.contentType = Objects.requireNonNull(contentType);
        this.isProtected = isProtected;
        this.hidden = hidden;
        this.headers = headers != null ? Collections.unmodifiableList(headers) : null;
        this.read = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNull(read)));
        this.write = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNull(write)));
    }
    @Deprecated
    public MetaData(final Set<User> users, final String contentType, final boolean isProtected, final boolean hidden, final List<HeaderPair> headers) {
        this(users, contentType, isProtected, hidden, headers, new HashSet<>(), new HashSet<>());

    }

    public MetaData(final Set<User> users, final String contentType) {
        this(users, contentType, false, false, null);
    }

    public MetaData(Set<User> users, String type, List<HeaderPair> headers, Set<Role> read, Set<Role> write) {
        this(users, type, false, false, headers, read, write);
    }

    public MetaData(Set<User> users, String type, Set<Role> read, Set<Role> write) {
        this(users, type, new ArrayList<>(1), read, write);
    }
    @Deprecated
    public final Set<User> getUsers() {
        return users;
    }

    public final String getContentType() {
        return contentType;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public List<HeaderPair> getHeaders() {
        return headers;
    }

    public Set<Role> getRead() {
        return read;
    }

    public Set<Role> getWrite() {
        return write;
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

    public static class Role {
        private final String role;

        public Role(final String role) {
            this.role = role;
        }

        public String getRole() {
            return role;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((role == null) ? 0 : role.hashCode());
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
            Role other = (Role) obj;
            if (role == null) {
                if (other.role != null)
                    return false;
            } else if (!role.equals(other.role))
                return false;
            return true;
        }

    }
}
