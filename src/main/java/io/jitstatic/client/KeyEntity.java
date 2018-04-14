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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Base64.Encoder;

abstract class KeyEntity extends MetaDataEntity {

    public KeyEntity(final MetaData data) {
        super(data);
    }

    private static final byte[] DATA = getBytes("data");

    protected static final Encoder ENCODER = Base64.getEncoder();
    
    protected void writeDataField(final OutputStream o) throws IOException {
        o.write(DOUBLEQUOTE);
        o.write(DATA);
        o.write(DOUBLEQUOTE);
        o.write(COLON);
        o.write(DOUBLEQUOTE);
        writeData(o);
        o.write(DOUBLEQUOTE);
    }

    protected abstract void writeData(final OutputStream o) throws IOException;

}
