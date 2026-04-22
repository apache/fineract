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
package org.apache.fineract.integrationtests.common.workingcapitalloan;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyActionData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;

@Slf4j
public final class WorkingCapitalLoanDelinquencyActionHelper {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private WorkingCapitalLoanDelinquencyActionHelper() {}

    public static PostWorkingCapitalLoansDelinquencyActionResponse createDelinquencyAction(final Long loanId, final String action,
            final LocalDate startDate, final LocalDate endDate) {
        final PostWorkingCapitalLoansDelinquencyActionRequest request = buildActionRequest(action, startDate, endDate);
        log.info("Creating delinquency action for loan {} request={}", loanId, request);
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanDelinquencyActions()
                .createDelinquencyAction(loanId, request));
    }

    public static PostWorkingCapitalLoansDelinquencyActionResponse createDelinquencyAction(final Long loanId,
            final PostWorkingCapitalLoansDelinquencyActionRequest request) {
        log.info("Creating delinquency action for loan {} request={}", loanId, request);
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanDelinquencyActions()
                .createDelinquencyAction(loanId, request));
    }

    public static List<WorkingCapitalLoanDelinquencyActionData> retrieveDelinquencyActions(final Long loanId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanDelinquencyActions()
                .retrieveDelinquencyActions(loanId));
    }

    public static PostWorkingCapitalLoansDelinquencyActionResponse createDelinquencyActionByExternalId(final String loanExternalId,
            final String action, final LocalDate startDate, final LocalDate endDate) {
        final PostWorkingCapitalLoansDelinquencyActionRequest request = buildActionRequest(action, startDate, endDate);
        log.info("Creating delinquency action for loan externalId={} request={}", loanExternalId, request);
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanDelinquencyActions()
                .createDelinquencyActionByExternalId(loanExternalId, request));
    }

    public static List<WorkingCapitalLoanDelinquencyActionData> retrieveDelinquencyActionsByExternalId(final String loanExternalId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanDelinquencyActions()
                .retrieveDelinquencyActionsByExternalId(loanExternalId));
    }

    public static void activateLoan(final Long loanId, final LocalDate disbursementDate) {
        final String dateStr = disbursementDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        log.info("Activating WC loan {} with disbursement date {}", loanId, dateStr);
        ok(() -> {
            FineractFeignClientHelper.getFineractFeignClient().internalWorkingCapitalLoans().activateLoan(loanId, dateStr);
            return null;
        });
    }

    public static void generateNextDelinquencyPeriod(final Long loanId, final LocalDate businessDate) {
        final String dateStr = businessDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        log.info("Generating next delinquency period for WC loan {} with business date {}", loanId, dateStr);
        ok(() -> {
            FineractFeignClientHelper.getFineractFeignClient().internalWorkingCapitalLoans().generateNextDelinquencyPeriod(loanId, dateStr);
            return null;
        });
    }

    public static PostWorkingCapitalLoansDelinquencyActionRequest buildActionRequest(final String action, final LocalDate startDate,
            final LocalDate endDate) {
        final PostWorkingCapitalLoansDelinquencyActionRequest request = new PostWorkingCapitalLoansDelinquencyActionRequest();
        request.setAction(action);
        request.setStartDate(startDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        if (endDate != null) {
            request.setEndDate(endDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        }
        request.setDateFormat(DATE_FORMAT);
        request.setLocale("en");
        return request;
    }
}
