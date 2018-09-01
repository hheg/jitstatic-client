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
