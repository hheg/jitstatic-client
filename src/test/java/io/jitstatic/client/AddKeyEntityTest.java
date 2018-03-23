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
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import io.jitstatic.client.MetaData.User;

public class AddKeyEntityTest {

    @Test
    public void testAddKeyEntity() throws IOException {
        Set<User> users = new HashSet<>();
        users.add(new User("user", "pass"));
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 1 });
        KeyEntity data = new AddKeyEntity(bis, new CommitData("master", "key", "msg", "usr", "mail"),
                new MetaData(users, "application/json"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        data.writeTo(baos);
        assertEquals(
                "{\"key\":\"key\",\"branch\":\"refs/heads/master\",\"message\":\"msg\",\"userInfo\":\"usr\",\"userMail\":\"mail\",\"metaData\":{\"users\":[{\"user\":\"user\",\"password\":\"pass\"}],\"contentType\":\"application/json\"},\"data\":\"AQ==\"}",
                baos.toString("UTF-8"));
    }

    @Test
    public void testAddKeyEntityTwoUsers() throws IOException {
        Set<User> users = new HashSet<>();
        users.add(new User("user", "pass"));
        users.add(new User("user2", "pass"));
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] { 1 });
        KeyEntity data = new AddKeyEntity(bis, new CommitData("master", "key", "msg", "usr", "mail"),
                new MetaData(users, "application/json"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        data.writeTo(baos);
        assertEquals(
                "{\"key\":\"key\",\"branch\":\"refs/heads/master\",\"message\":\"msg\",\"userInfo\":\"usr\",\"userMail\":\"mail\",\"metaData\":{\"users\":[{\"user\":\"user2\",\"password\":\"pass\"},{\"user\":\"user\",\"password\":\"pass\"}],\"contentType\":\"application/json\"},\"data\":\"AQ==\"}",
                baos.toString("UTF-8"));
    }
}
