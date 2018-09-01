package io.jitstatic.client;

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
