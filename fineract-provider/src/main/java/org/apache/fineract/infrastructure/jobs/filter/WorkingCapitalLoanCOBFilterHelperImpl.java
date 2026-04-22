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
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.cob.conditions.LoanCOBEnabledCondition;
import org.apache.fineract.cob.data.COBIdAndLastClosedBusinessDate;
import org.apache.fineract.cob.domain.WorkingCapitalLoanAccountLock;
import org.apache.fineract.cob.service.AbstractAccountLockService;
import org.apache.fineract.cob.service.InlineCommonLockableCOBExecutorService;
import org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant;
import org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanRetrieveIdService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.http.BodyCachingHttpServletRequestWrapper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.LoanIdsHardLockedException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Conditional(LoanCOBEnabledCondition.class)
public class WorkingCapitalLoanCOBFilterHelperImpl extends COBFilterApiMatcher
        implements WorkingCapitalLoanCOBFilterHelper, InitializingBean {

    private final AbstractAccountLockService<WorkingCapitalLoanAccountLock> loanAccountLockService;
    private final PlatformSecurityContext context;
    private final InlineCommonLockableCOBExecutorService<WorkingCapitalLoanAccountLock> inlineLoanCOBExecutorService;
    private final WorkingCapitalLoanRepository loanRepository;
    private final FineractProperties fineractProperties;
    private final WorkingCapitalLoanRetrieveIdService retrieveIdService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<HttpMethod> HTTP_METHODS = List.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);

    public static final Pattern IGNORE_LOAN_PATH_PATTERN = Pattern.compile("/v[1-9][0-9]*/working-capital-loans/catch-up");
    public static final Pattern LOAN_PATH_PATTERN = Pattern
            .compile("/v[1-9][0-9]*/(?:reschedule)?working-capital-loans/(?:external-id/)?([^/?]+).*");
    private static final Predicate<String> URL_FUNCTION = s -> LOAN_PATH_PATTERN.matcher(s).find();

    private Long getLoanId(String pathInfo) {
        String id = LOAN_PATH_PATTERN.matcher(pathInfo).replaceAll("$1");
        if (isExternal(pathInfo)) {
            String externalId = id;
            return loanRepository.findIdByExternalId(new ExternalId(externalId));
        } else if (StringUtils.isNumeric(id)) {
            return Long.valueOf(id);
        } else {
            return null;
        }

    }

    private boolean isExternal(String pathInfo) {
        return LOAN_PATH_PATTERN.matcher(pathInfo).matches() && pathInfo.contains("external-id");
    }

    @Override
    protected boolean isApiMatching(String method, String pathInfo) {
        return HTTP_METHODS.contains(HttpMethod.valueOf(method)) && !IGNORE_LOAN_PATH_PATTERN.matcher(pathInfo).find()
                && URL_FUNCTION.test(pathInfo);
    }

    @Override
    public boolean isBypassUser() {
        return context.authenticatedUser().isBypassUser();
    }

    private boolean isLoanHardLocked(Long... loanIds) {
        return isLoanHardLocked(Arrays.asList(loanIds));
    }

    private boolean isLoanHardLocked(List<Long> loanIds) {
        return loanIds.stream().anyMatch(loanAccountLockService::isLoanHardLocked);
    }

    private boolean isLockOverrulable(Long... loanIds) {
        return isLockOverrulable(Arrays.asList(loanIds));
    }

    private boolean isLockOverrulable(List<Long> loanIds) {
        return loanIds.stream().anyMatch(loanAccountLockService::isLockOverrulable);
    }

    @Override
    public boolean isLoanBehind(List<Long> loanIds) {
        List<COBIdAndLastClosedBusinessDate> loanIdAndLastClosedBusinessDates = new ArrayList<>();
        List<List<Long>> partitions = Lists.partition(loanIds, fineractProperties.getQuery().getInClauseParameterSizeLimit());
        partitions.forEach(partition -> {
            loanIdAndLastClosedBusinessDates.addAll(retrieveIdService
                    .retrieveLoanIdsBehindDate(ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE), partition));
            loanIdAndLastClosedBusinessDates.addAll(retrieveIdService.retrieveLoanBehindOnDisbursementDate(
                    ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE), partition));
        });
        return CollectionUtils.isNotEmpty(loanIdAndLastClosedBusinessDates);
    }

    @Override
    public List<Long> calculateRelevantLoanIds(BodyCachingHttpServletRequestWrapper request) throws IOException {
        String pathInfo = request.getPathInfo();
        if (isBatchApi(pathInfo)) {
            return getLoanIdsFromBatchApi(request);
        } else {
            return getLoanIdsFromApi(pathInfo);
        }
    }

    private List<Long> getLoanIdsFromBatchApi(BodyCachingHttpServletRequestWrapper request) throws IOException {
        List<Long> loanIds = new ArrayList<>();
        for (BatchRequest batchRequest : getBatchRequests(request)) {
            // check the URL for Loan related ID
            String relativeUrl = batchRequest.getRelativeUrl();
            if (!relativeUrl.contains("$.resourceId")) {
                // if resourceId reference is used, we simply don't know the resourceId without executing the requests
                // first, so skipping it
                loanIds.addAll(getLoanIdsFromApi(relativeUrl));
            }

            // check the body for Loan ID
            Long loanId = getTopLevelLoanIdFromBatchRequest(batchRequest);
            if (loanId != null) {
                if (isLoanHardLocked(loanId) && !isLockOverrulable(loanId)) {
                    throw new LoanIdsHardLockedException(loanId);
                } else {
                    loanIds.add(loanId);
                }
            }
        }
        return loanIds;
    }

    private Long getTopLevelLoanIdFromBatchRequest(BatchRequest batchRequest) throws JsonProcessingException {
        String body = batchRequest.getBody();
        if (StringUtils.isNotBlank(body)) {
            JsonNode jsonNode = objectMapper.readTree(body);
            if (jsonNode.has("loanId")) {
                return jsonNode.get("loanId").asLong();
            }
        }
        return null;
    }

    private List<Long> getLoanIdsFromApi(String pathInfo) {
        List<Long> loanIds = getLoanIdList(pathInfo);
        if (isLoanHardLocked(loanIds) && !isLockOverrulable(loanIds)) {
            throw new LoanIdsHardLockedException(loanIds.getFirst());
        } else {
            return loanIds;
        }
    }

    private List<Long> getLoanIdList(String pathInfo) {
        Long loanIdFromRequest = getLoanId(pathInfo);
        if (loanIdFromRequest == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(loanIdFromRequest);
    }

    @Override
    public void executeInlineCob(List<Long> loanIds) {
        inlineLoanCOBExecutorService.execute(loanIds, WorkingCapitalLoanCOBConstant.INLINE_WORKING_CAPITAL_LOAN_COB_JOB_NAME);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
    }

}
