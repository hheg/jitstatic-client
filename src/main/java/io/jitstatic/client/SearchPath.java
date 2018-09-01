package io.jitstatic.client;

import java.util.Objects;

public class SearchPath {

    private final String path;
    private final boolean recursivly;

    public SearchPath(final String path, final boolean recursively) {
        this.path = Objects.requireNonNull(path);
        if (path.isEmpty()) {
            throw new IllegalArgumentException("path cannot be empty");
        }
        this.recursivly = recursively;
    }

    public String getPath() {
        return path;
    }

    public boolean isRecursivly() {
        return recursivly;
    }
}
