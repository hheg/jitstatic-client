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

public class HeaderPair {

    private final String header;
    private final String value;

    public HeaderPair(final String header, final String value) {
        this.header = header;
        this.value = value;
    }

    public String getHeader() {
        return header;
    }

    public String getValue() {
        return value;
    }

    static HeaderPair of(final String header, final String value) {
        return new HeaderPair(header, value);
    }
}
