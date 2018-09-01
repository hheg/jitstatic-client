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

class BulkSearchEntity extends JsonEntity {

    private static final byte[] REF = getBytes("ref");
    private static final byte[] PATHS = getBytes("paths");
    
    private final List<BulkSearch> search;

    public BulkSearchEntity(final List<BulkSearch> search) {
        this.search = search;
    }

    @Override
    public void writeTo(final OutputStream o) throws IOException {
        o.write(LEFTSQBRACKET);       
        byte[] b = new byte[0];
        for (BulkSearch bs : search) {
            o.write(b);
            o.write(LEFTBRACKET);
            writeField(REF, bs.getRef(), o);
            o.write(COMMA);
            o.write(DOUBLEQUOTE);
            o.write(PATHS);
            o.write(DOUBLEQUOTE);
            o.write(COLON);
            writeSearchPath(bs.getPaths(), o);
            o.write(RIGHTBRACKET);
            b = COMMA;
        }        
        o.write(RIGHTSQBRACKET);
    }

    private void writeSearchPath(final List<SearchPath> paths, final OutputStream o) throws IOException {
        o.write(LEFTSQBRACKET);
        byte[] b = new byte[0];
        for (SearchPath sp : paths) {
            o.write(b);
            o.write(LEFTBRACKET);
            new SearchPathEntity(sp).writeTo(o);
            o.write(RIGHTBRACKET);
            b = COMMA;
        }
        o.write(RIGHTSQBRACKET);
    }

    private static class SearchPathEntity extends JsonEntity {

        private static final byte[] PATH = getBytes("path");
        private static final byte[] RECURSIVELY = getBytes("recursively");
        private final SearchPath searchPath;

        public SearchPathEntity(final SearchPath searchPath) {
            this.searchPath = searchPath;
        }

        @Override
        public void writeTo(final OutputStream o) throws IOException {
            writeField(PATH, searchPath.getPath(), o);
            o.write(COMMA);
            writeBool(RECURSIVELY, searchPath.isRecursivly(), o);
        }

    }
}
