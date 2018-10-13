package io.jitstatic.client;

import java.io.IOException;

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

import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;

class APIHelper {

    static void addRefParameter(final String ref, final URIBuilder uriBuilder) {
        if (ref != null) {
            uriBuilder.addParameter(JitStaticUpdaterClientImpl.REF, ref);
        }
    }

    static void checkPUTStatusCode(final URI uri, final HttpPut putRequest, final StatusLine statusLine, final HttpEntity httpEntity)
            throws ParseException, IOException {
        switch (statusLine.getStatusCode()) {
        case HttpStatus.SC_OK:
        case HttpStatus.SC_ACCEPTED:
            break;
        default:
            throw new APIException(statusLine, uri.toString(), putRequest.getMethod(), httpEntity);
        }
    }

    static String escapeVersion(String currentVersion) {
        if (currentVersion.isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be empty");
        }
        if (!currentVersion.startsWith("\"")) {
            currentVersion = "\"" + currentVersion;
        }
        if (!currentVersion.endsWith("\"")) {
            currentVersion += "\"";
        }
        return currentVersion;
    }

    static void checkGETresponse(final URI url, final HttpGet getRequest, final StatusLine statusLine, final HttpEntity httpEntity)
            throws ParseException, IOException {
        switch (statusLine.getStatusCode()) {
        case HttpStatus.SC_OK:
        case HttpStatus.SC_ACCEPTED:
        case HttpStatus.SC_NOT_MODIFIED:
            break;
        default:
            throw new APIException(statusLine, url.toString(), getRequest.getMethod(), httpEntity);
        }
    }

    static void checkDELETEresponse(final URI url, final HttpDelete request, final StatusLine statusLine, final HttpEntity httpEntity)
            throws ParseException, IOException {
        switch (statusLine.getStatusCode()) {
        case HttpStatus.SC_OK:
        case HttpStatus.SC_ACCEPTED:
            break;
        default:
            throw new APIException(statusLine, url.toString(), request.getMethod(), httpEntity);
        }
    }

    static String getSingleHeader(final CloseableHttpResponse httpResponse, final String headerTag) {
        final Header[] headers = httpResponse.getHeaders(headerTag);
        if (headers.length != 1) {
            return null;
        }
        return headers[0].getValue();
    }

    static String checkVersion(String version) {
        if (!version.startsWith("\"")) {
            version = "\"" + version;
        }
        if (!version.endsWith("\"")) {
            version += "\"";
        }
        return version;
    }

    public static void checkPOSTresponse(URI url, HttpPost postRequest, StatusLine statusLine, final HttpEntity httpEntity) throws ParseException, IOException {
        switch (statusLine.getStatusCode()) {
        case HttpStatus.SC_OK:
        case HttpStatus.SC_ACCEPTED:
            break;
        default:
            throw new APIException(statusLine, url.toString(), postRequest.getMethod(), httpEntity);
        }

    }

}
