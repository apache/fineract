/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.security.data;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.time.StopWatch;

/**
 * Immutable data object representing platform API request used for logging/debugging.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class PlatformRequestLog {

    @SuppressWarnings("unused")
    private long startTime;
    @SuppressWarnings("unused")
    private long totalTime;
    @SuppressWarnings("unused")
    private String method;
    @SuppressWarnings("unused")
    private String url;
    @SuppressWarnings("unused")
    private Map<String, String[]> parameters;

    public static PlatformRequestLog from(final StopWatch task, final HttpServletRequest request) throws IOException {
        final String requestUrl = request.getRequestURL().toString();

        final Map<String, String[]> parameters = new HashMap<>(request.getParameterMap());
        parameters.remove("password");
        parameters.remove("_");

        return new PlatformRequestLog().setStartTime(task.getStartTime()).setTotalTime(task.getTime()).setMethod(request.getMethod())
                .setUrl(requestUrl).setParameters(parameters);
    }
}
