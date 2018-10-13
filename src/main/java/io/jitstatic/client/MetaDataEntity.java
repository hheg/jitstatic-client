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
import java.util.List;
import java.util.Set;

import io.jitstatic.client.MetaData.Role;
import io.jitstatic.client.MetaData.User;

abstract class MetaDataEntity extends JsonEntity {

    private static final byte[] METADATA = getBytes("metaData");
    private static final byte[] USERS = getBytes("users");
    private static final byte[] PASSWORD = getBytes("password");
    private static final byte[] CONTENTTYPE = getBytes("contentType");
    private static final byte[] PROTECTED = getBytes("protected");
    private static final byte[] HIDDEN = getBytes("hidden");
    private static final byte[] HEADERS = getBytes("headers");
    private static final byte[] HEADER = getBytes("header");
    private static final byte[] VALUE = getBytes("value");
    protected static final byte[] USER = getBytes("user");
    protected static final byte[] USERINFO = getBytes("userInfo");
    protected static final byte[] USERMAIL = getBytes("userMail");
    protected static final byte[] MESSAGE = getBytes("message");
    protected static final byte[] ROLES = getBytes("roles");
    protected static final byte[] ROLE = getBytes("role");

    private final MetaData data;

    public MetaDataEntity(final MetaData data) {
        this.data = data;
    }

    protected void writeMetaDataField(final OutputStream o) throws IOException {
        o.write(DOUBLEQUOTE);
        o.write(METADATA);
        o.write(DOUBLEQUOTE);
        o.write(COLON);
        o.write(LEFTBRACKET);
        if (data != null) {
            o.write(DOUBLEQUOTE);
            o.write(USERS);
            o.write(DOUBLEQUOTE);
            o.write(COLON);            
            writeUsers(o);
            o.write(COMMA);
            writeField(CONTENTTYPE, data.getContentType(), o);
            o.write(COMMA);
            writeBool(PROTECTED, data.isProtected(), o);
            o.write(COMMA);
            writeBool(HIDDEN, data.isHidden(), o);
            if (data.getHeaders() != null) {
                o.write(COMMA);
                writeHeaders(data.getHeaders(), o);
            }
            if (data.getRoles() != null) {
                o.write(COMMA);
                writeRoles(data.getRoles(), o);
            }
        }
        o.write(RIGHTBRACKET);
    }

    private void writeRoles(Set<Role> roles2, OutputStream o) throws IOException {
        o.write(DOUBLEQUOTE);
        o.write(ROLES);
        o.write(DOUBLEQUOTE);
        o.write(COLON);
        o.write(LEFTSQBRACKET);
        byte[] b = NIL;
        for (Role r : roles2) {
            o.write(b);
            o.write(LEFTBRACKET);
            writeField(ROLE, r.getRole(), o);
            o.write(RIGHTBRACKET);
            b = COMMA;
        }
        o.write(RIGHTSQBRACKET);
    }

    private void writeHeaders(List<HeaderPair> headers, OutputStream o) throws IOException {
        o.write(DOUBLEQUOTE);
        o.write(HEADERS);
        o.write(DOUBLEQUOTE);
        o.write(COLON);
        o.write(LEFTSQBRACKET);
        byte[] b = NIL;
        for (HeaderPair hp : headers) {
            o.write(b);
            o.write(LEFTBRACKET);
            writeField(HEADER, hp.getHeader(), o);
            o.write(COMMA);
            writeField(VALUE, hp.getValue(), o);
            o.write(RIGHTBRACKET);
            b = COMMA;
        }
        o.write(RIGHTSQBRACKET);
    }

    private void writeUsers(final OutputStream o) throws IOException {
        final Set<User> users = data.getUsers();
        byte[] b = NIL;
        o.write(LEFTSQBRACKET);
        for (User user : users) {
            o.write(b);
            o.write(LEFTBRACKET);
            writeField(USER, user.getUser(), o);
            o.write(COMMA);
            writeField(PASSWORD, user.getPassword(), o);
            o.write(RIGHTBRACKET);
            b = COMMA;
        }
        o.write(RIGHTSQBRACKET);
    }
}
