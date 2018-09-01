package io.jitstatic.client;

import java.util.List;
import java.util.Objects;

public class BulkSearch {

    private final String ref;
    private final List<SearchPath> paths;

    public BulkSearch(final String ref, final List<SearchPath> paths) {
        this.ref = Objects.requireNonNull(ref);
        if(ref.isEmpty()) {
            throw new IllegalArgumentException("parameter ref cannot be empty");
        }
        this.paths = Objects.requireNonNull(paths);
        if(paths.isEmpty()) {
            throw new IllegalArgumentException("parameter paths cannot be empty");
        }
    }

    public String getRef() {
        return ref;
    }

    public List<SearchPath> getPaths() {
        return paths;
    }

}
