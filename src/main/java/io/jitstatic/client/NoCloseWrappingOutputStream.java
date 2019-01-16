package io.jitstatic.client;

/*-
 * #%L
 * jitstatic client
 * %%
 * Copyright (C) 2017 - 2019 H.Hegardt
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

class NoCloseWrappingOutputStream  extends OutputStream {

    private final OutputStream wrapped;
    
    public NoCloseWrappingOutputStream(OutputStream os) {
        this.wrapped = os;
    }

    @Override
    public void write(int b) throws IOException {
        wrapped.write(b);
    }
    @Override
    public void close() throws IOException {
        // Do nothing
    }
    @Override
    public void flush() throws IOException {
        wrapped.flush();
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        wrapped.write(b);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        wrapped.write(b, off, len);
    }
    
}

