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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import io.jitstatic.client.MetaData.Role;
import io.jitstatic.client.MetaData.User;

public class ModifyDataEntityTest {

    @Test
    public void testModifyDataEntityTest() throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 1 });
        JsonEntity data = new ModifyKeyEntity(bis, "msg", "usr", "mail");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        data.writeTo(baos);
        assertEquals("{\"message\":\"msg\",\"userInfo\":\"usr\",\"userMail\":\"mail\",\"data\":\"AQ==\"}", baos.toString("UTF-8"));
    }

    @Test
    public void testModifyUserKey() throws IOException {
        JsonEntity data = new ModifyUserKeyEntity(new ModifyUserKeyData(new MetaData("application/json"), "msg", "mail", "ui"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        data.writeTo(baos);
        assertEquals(
                "{\"message\":\"msg\",\"userInfo\":\"ui\",\"userMail\":\"mail\",\"metaData\":{\"users\":[],\"contentType\":\"application/json\",\"protected\":false,\"hidden\":false,\"roles\":[]}}",
                baos.toString("UTF-8"));
    }

    @Test
    public void testModifyMetaKey() throws IOException {
        Set<User> users = new HashSet<>();
        users.add(new User("u", "p"));
        List<HeaderPair> list = Arrays.asList(new HeaderPair[] { HeaderPair.of("h", "v") });
        JsonEntity data = new ModifyUserKeyEntity(new ModifyUserKeyData(new MetaData(users, "application/json", list), "msg", "mail", "ui"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        data.writeTo(baos);
        assertEquals(
                "{\"message\":\"msg\",\"userInfo\":\"ui\",\"userMail\":\"mail\",\"metaData\":{\"users\":[{\"user\":\"u\",\"password\":\"p\"}],\"contentType\":\"application/json\",\"protected\":false,\"hidden\":false,\"headers\":[{\"header\":\"h\",\"value\":\"v\"}],\"roles\":[]}}",
                baos.toString());
    }
    
    @Test
    public void testModifyMetaKeyWithRoles() throws IOException {
        Set<Role> roles = new HashSet<>();
        roles.add(new Role("update"));
        roles.add(new Role("insert"));
        Set<User> users = new HashSet<>();
        users.add(new User("u", "p"));
        List<HeaderPair> list = Arrays.asList(new HeaderPair[] { HeaderPair.of("h", "v") });
        JsonEntity data = new ModifyUserKeyEntity(new ModifyUserKeyData(new MetaData(users, "application/json", list, roles), "msg", "mail", "ui"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        data.writeTo(baos);
        assertEquals(
                "{\"message\":\"msg\",\"userInfo\":\"ui\",\"userMail\":\"mail\",\"metaData\":{\"users\":[{\"user\":\"u\",\"password\":\"p\"}],\"contentType\":\"application/json\",\"protected\":false,\"hidden\":false,\"headers\":[{\"header\":\"h\",\"value\":\"v\"}],\"roles\":[{\"role\":\"update\"},{\"role\":\"insert\"}]}}",
                baos.toString());
    }
}
