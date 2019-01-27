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

import io.jitstatic.client.MetaData.Role;

class UserDataEntity extends JsonEntity {

    private static final byte[] BASICPWD = getBytes("basicPassword");
    private static final byte[] ROLES = getBytes("roles");
    private static final byte[] ROLE = getBytes("role");

    private final UserData userData;

    public UserDataEntity(final UserData data) {
        this.userData = data;
    }

    @Override
    public void writeTo(OutputStream o) throws IOException {
        o.write(LEFTBRACKET);
        o.write(DOUBLEQUOTE);
        o.write(ROLES);
        o.write(DOUBLEQUOTE);
        o.write(COLON);
        o.write(LEFTSQBRACKET);
        byte[] b = NIL;
        for (Role role : userData.getRoles()) {
            o.write(b);
            o.write(LEFTBRACKET);
            writeField(ROLE, role.getRole(), o);
            o.write(RIGHTBRACKET);
            b = COMMA;
        }
        o.write(RIGHTSQBRACKET);
        if (userData.getBasicPassword() != null) {
            o.write(COMMA);
            writeField(BASICPWD, userData.getBasicPassword(), o);
        }
        o.write(RIGHTBRACKET);
    }

}
