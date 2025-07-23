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
package org.apache.fineract.portfolio.loanaccount.api.pointintime;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.DateParam;
import org.apache.fineract.infrastructure.core.data.DateFormat;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.api.pointintime.data.RetrieveLoansPointInTimeExternalIdsRequest;
import org.apache.fineract.portfolio.loanaccount.api.pointintime.data.RetrieveLoansPointInTimeRequest;
import org.apache.fineract.portfolio.loanaccount.data.LoanPointInTimeData;
import org.apache.fineract.portfolio.loanaccount.service.LoanPointInTimeService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoansPointInTimeApiDelegate implements LoansPointInTimeApi {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOAN";

    private final LoanPointInTimeService loanPointInTimeService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final PlatformSecurityContext context;

    @Override
    public LoanPointInTimeData retrieveLoanPointInTime(Long loanId, DateParam dateParam, String dateFormat, String locale) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return getLoanPointInTime(loanId, dateParam, dateFormat, locale);
    }

    @Override
    public LoanPointInTimeData retrieveLoanPointInTimeByExternalId(String loanExternalIdStr, DateParam dateParam, String dateFormat,
            String locale) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = loanReadPlatformService.getResolvedLoanId(loanExternalId);

        return getLoanPointInTime(resolvedLoanId, dateParam, dateFormat, locale);
    }

    @Override
    public List<LoanPointInTimeData> retrieveLoansPointInTime(RetrieveLoansPointInTimeRequest request) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        List<Long> loanIds = request.getLoanIds();
        DateParam dateParam = request.getDate();
        String dateFormat = request.getDateFormat();
        String locale = request.getLocale();

        return getLoansPointInTime(loanIds, dateParam, dateFormat, locale);
    }

    @Override
    public List<LoanPointInTimeData> retrieveLoansPointInTimeByExternalIds(RetrieveLoansPointInTimeExternalIdsRequest request) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        List<String> loanExternalIds = request.getExternalIds();
        DateParam dateParam = request.getDate();
        String dateFormat = request.getDateFormat();
        String locale = request.getLocale();

        List<ExternalId> externalIds = ExternalIdFactory.produce(loanExternalIds);
        List<Long> loanIds = resolveExternalIds(externalIds);

        return getLoansPointInTime(loanIds, dateParam, dateFormat, locale);
    }

    private List<LoanPointInTimeData> getLoansPointInTime(List<Long> loanIds, DateParam dateParam, String dateFormat, String locale) {
        DateFormat df = StringUtils.isBlank(dateFormat) ? null : new DateFormat(dateFormat);
        LocalDate date = dateParam.getDate("date", df, locale);
        return loanPointInTimeService.retrieveAt(loanIds, date);
    }

    private LoanPointInTimeData getLoanPointInTime(Long loanId, DateParam dateParam, String dateFormat, String locale) {
        DateFormat df = StringUtils.isBlank(dateFormat) ? null : new DateFormat(dateFormat);
        LocalDate date = dateParam.getDate("date", df, locale);
        return loanPointInTimeService.retrieveAt(loanId, date);
    }

    private List<Long> resolveExternalIds(List<ExternalId> loanExternalIds) {
        loanExternalIds.forEach(ExternalId::throwExceptionIfEmpty);
        return loanReadPlatformService.retrieveLoanIdsByExternalIds(loanExternalIds);
    }

}
