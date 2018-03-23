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

class Utils {

    private static final String REFS_HEADS = "refs/heads/";
    private static final String REFS_TAGS = "refs/tags/";

    public static String checkBranch(final String branch) {
        if (branch == null) {
            return branch;
        }
        if (branch.startsWith(REFS_HEADS)) {
            return branch;
        }
        return REFS_HEADS + branch;
    }

    public static String checkRef(final String ref) {
        if (ref == null) {
            return ref;
        }
        if (ref.startsWith(REFS_HEADS) || ref.startsWith(REFS_TAGS)) {
            return ref;
        }
        return REFS_HEADS + ref;
    }
}
