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

import org.junit.Test;

public class ModifyDataEntityTest {

    @Test
    public void testModifyDataEntityTest() throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[] {1});
        KeyEntity data = new ModifyKeyEntity(bis, "msg", "usr", "mail");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        data.writeTo(baos);
        assertEquals("{\"message\":\"msg\",\"userInfo\":\"usr\",\"userMail\":\"mail\",\"data\":\"AQ==\"}",baos.toString("UTF-8"));
    }
    
}
