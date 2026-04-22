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
package org.apache.fineract.infrastructure.jobs.filter;

import static org.apache.fineract.batch.command.CommandStrategyUtils.isRelativeUrlVersioned;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.infrastructure.core.http.BodyCachingHttpServletRequestWrapper;

public abstract class COBFilterApiMatcher implements COBFilterHelper {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean isOnApiList(BodyCachingHttpServletRequestWrapper request) throws IOException {
        String pathInfo = request.getPathInfo();
        String method = request.getMethod();
        if (StringUtils.isBlank(pathInfo)) {
            return false;
        }
        if (isBatchApi(pathInfo)) {
            return isBatchApiMatching(request);
        } else {
            return isApiMatching(method, pathInfo);
        }
    }

    protected boolean isBatchApiMatching(BodyCachingHttpServletRequestWrapper request) throws IOException {
        for (BatchRequest batchRequest : getBatchRequests(request)) {
            String method = batchRequest.getMethod();
            String pathInfo = batchRequest.getRelativeUrl();
            if (isApiMatching(method, pathInfo)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isBatchApi(String pathInfo) {
        return pathInfo.startsWith("/v1/batches");
    }

    protected List<BatchRequest> getBatchRequests(BodyCachingHttpServletRequestWrapper request) throws IOException {
        List<BatchRequest> batchRequests = objectMapper.readValue(request.getInputStream(), new TypeReference<>() {});
        // since we read body, we have to reset so the upcoming readings are successful
        request.resetStream();
        for (BatchRequest batchRequest : batchRequests) {
            String pathInfo = "/" + batchRequest.getRelativeUrl();
            if (!isRelativeUrlVersioned(batchRequest.getRelativeUrl())) {
                pathInfo = "/v1/" + batchRequest.getRelativeUrl();
            }
            batchRequest.setRelativeUrl(pathInfo);
        }
        return batchRequests;
    }

    protected abstract boolean isApiMatching(String method, String pathInfo);
}
