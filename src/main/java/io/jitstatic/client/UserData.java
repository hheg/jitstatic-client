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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.jitstatic.client.MetaData.Role;

public class UserData {

    private final String basicPassword;

    private final Set<Role> roles;

    public UserData(final Set<Role> roles) {
        this(roles, null);
    }

    public UserData(final Set<Role> roles, final String basicPassword) {
        this.roles = Collections.unmodifiableSet(Objects.requireNonNull(new HashSet<>(roles)));
        this.basicPassword = basicPassword;
    }

    public String getBasicPassword() {
        return basicPassword;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}
