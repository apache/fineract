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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.http.BodyCachingHttpServletRequestWrapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Works out which Working Capital loans a request is about, so their stored amortization models can be brought up to
 * the current version before the request reads or writes them.
 *
 * <p>
 * The counterpart of {@link ProgressiveLoanModelCheckerHelper} for working capital loans, and deliberately the same
 * shape: the URL patterns and loan-id extraction match {@link WorkingCapitalLoanCOBFilterHelperImpl}, but this is a
 * separate bean because that one exists only when close-of-business is enabled, and a model has to be brought up to
 * date whether or not it is.
 */
@RequiredArgsConstructor
@Component
public class WorkingCapitalLoanModelCheckerHelper extends COBFilterApiMatcher implements InitializingBean {

    private final WorkingCapitalLoanRepository loanRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<HttpMethod> HTTP_METHODS = List.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);

    public static final Pattern IGNORE_LOAN_PATH_PATTERN = Pattern.compile("/v[1-9][0-9]*/working-capital-loans/catch-up");
    public static final Pattern LOAN_PATH_PATTERN = Pattern.compile("/v[1-9][0-9]*/working-capital-loans/([^/?]+).*");
    public static final Pattern EXTERNAL_ID_LOAN_PATH_PATTERN = Pattern
            .compile("/v[1-9][0-9]*/working-capital-loans/external-id/([^/?]+).*");

    private static final Predicate<String> URL_FUNCTION = s -> LOAN_PATH_PATTERN.matcher(s).find();

    @Override
    protected boolean isApiMatching(final String method, final String pathInfo) {
        return HTTP_METHODS.contains(HttpMethod.valueOf(method)) && !IGNORE_LOAN_PATH_PATTERN.matcher(pathInfo).find()
                && URL_FUNCTION.test(pathInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> calculateRelevantLoanIds(final BodyCachingHttpServletRequestWrapper request) throws IOException {
        final String pathInfo = request.getPathInfo();
        if (isBatchApi(pathInfo)) {
            return getLoanIdsFromBatchApi(request);
        }
        return getLoanIdsFromApi(pathInfo);
    }

    private List<Long> getLoanIdsFromBatchApi(final BodyCachingHttpServletRequestWrapper request) throws IOException {
        final List<Long> loanIds = new ArrayList<>();
        for (final BatchRequest batchRequest : getBatchRequests(request)) {
            final String relativeUrl = batchRequest.getRelativeUrl();
            if (!relativeUrl.contains("$.resourceId")) {
                // A request referring to an earlier one's resourceId has no id to read until that one has run, so it
                // is skipped rather than guessed at.
                loanIds.addAll(getLoanIdsFromApi(relativeUrl));
            }
            final Long loanId = getTopLevelLoanIdFromBatchRequest(batchRequest);
            if (loanId != null) {
                loanIds.add(loanId);
            }
        }
        return loanIds.stream().distinct().toList();
    }

    private Long getTopLevelLoanIdFromBatchRequest(final BatchRequest batchRequest) throws JsonProcessingException {
        final String body = batchRequest.getBody();
        if (StringUtils.isNotBlank(body)) {
            final JsonNode jsonNode = objectMapper.readTree(body);
            if (jsonNode.has("loanId")) {
                return jsonNode.get("loanId").asLong();
            }
        }
        return null;
    }

    private List<Long> getLoanIdsFromApi(final String pathInfo) {
        final Long loanId = getLoanId(pathInfo);
        return loanId == null ? Collections.emptyList() : Collections.singletonList(loanId);
    }

    private Long getLoanId(final String pathInfo) {
        if (isExternal(pathInfo)) {
            final String externalId = EXTERNAL_ID_LOAN_PATH_PATTERN.matcher(pathInfo).replaceAll("$1");
            return loanRepository.findIdByExternalId(new ExternalId(externalId));
        }
        final String id = LOAN_PATH_PATTERN.matcher(pathInfo).replaceAll("$1");
        return StringUtils.isNumeric(id) ? Long.valueOf(id) : null;
    }

    private boolean isExternal(final String pathInfo) {
        return EXTERNAL_ID_LOAN_PATH_PATTERN.matcher(pathInfo).matches();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
    }

    @Override
    public boolean isBypassUser() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isLoanBehind(final List<Long> loanIds) {
        throw new NotImplementedException();
    }

    @Override
    public void executeInlineCob(final List<Long> loanIds) {
        throw new NotImplementedException();
    }
}
