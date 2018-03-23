package io.jitstatic.client;

/*-
 * #%L
 * jitstatic
 * %%
 * Copyright (C) 2017 H.Hegardt
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
import java.io.UncheckedIOException;
import java.util.Properties;

class ProjectVersion {

    public final static ProjectVersion INSTANCE = new ProjectVersion();

    private final String commitId;
    private final String commitIdAbbrev;
    private final String buildVersion;

    private ProjectVersion() {
        try {
            final Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("git.properties"));
            this.commitId = String.valueOf(properties.get("git.commit.id"));
            this.commitIdAbbrev = String.valueOf(properties.get("git.commit.id.abbrev"));
            this.buildVersion = String.valueOf(properties.get("git.build.version"));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getCommitIdAbbrev() {
        return commitIdAbbrev;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public String getCommitId() {
        return commitId;
    }
}
