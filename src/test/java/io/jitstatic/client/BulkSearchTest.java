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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class BulkSearchTest {

    @Test
    public void testBulkSearch() throws IOException {
        List<BulkSearch> bulkSearch = new ArrayList<>();
        List<SearchPath> searchPath = new ArrayList<>();
        searchPath.add(new SearchPath("path1", false));
        searchPath.add(new SearchPath("path2", true));
        BulkSearch bs1 = new BulkSearch("refs/heads/master", searchPath);
        BulkSearch bs2 = new BulkSearch("refs/heads/develop", searchPath);
        bulkSearch.add(bs1);
        bulkSearch.add(bs2);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new BulkSearchEntity(bulkSearch).writeTo(baos);
        assertEquals(
                "[{\"ref\":\"refs/heads/master\",\"paths\":[{\"path\":\"path1\",\"recursively\":false},{\"path\":\"path2\",\"recursively\":true}]},{\"ref\":\"refs/heads/develop\",\"paths\":[{\"path\":\"path1\",\"recursively\":false},{\"path\":\"path2\",\"recursively\":true}]}]",
                new String(baos.toByteArray(), StandardCharsets.UTF_8));

    }
}
