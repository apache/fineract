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
package org.apache.fineract.integrationtests.common.loans;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTransactionHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.ParallelExecutionHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class LoanTestLifecycleExtension implements AfterEachCallback, BeforeEachCallback {

    private final FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
    private final FeignLoanHelper loanHelper = new FeignLoanHelper(fineractClient);
    private final FeignTransactionHelper transactionHelper = new FeignTransactionHelper(fineractClient);
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    private final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern(DATE_FORMAT).toFormatter();

    @Override
    public void afterEach(ExtensionContext context) {
        closeOpenLoans();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        closeOpenLoans();
    }

    private void closeOpenLoans() {
        LocalDate cleanupDate = determineCleanupDate();
        BusinessDateHelper.runAt(DateTimeFormatter.ofPattern(DATE_FORMAT).format(cleanupDate), () -> {
            List<Long> loanIds = getLoanIdsByStatusId(300);
            ParallelExecutionHelper.runInParallel(loanIds, loanId -> closeActiveLoan(loanId, cleanupDate));
            loanIds = getLoanIdsByStatusId(200);
            ParallelExecutionHelper.runInParallel(loanIds, loanHelper::undoApproval);
            loanIds = getLoanIdsByStatusId(100);
            ParallelExecutionHelper.runInParallel(loanIds, this::rejectSubmittedLoan);
            loanIds = getLoanIdsByStatusId(300);
            assertEquals(0, loanIds.size());
        });
    }

    private void closeActiveLoan(Long loanId, LocalDate cleanupDate) {
        GetLoansLoanIdResponse loanResponse = loanHelper.getLoanDetailsWithAssociations(loanId, "all");
        if (MathUtil.isLessThan(loanResponse.getApprovedPrincipal(), loanResponse.getProposedPrincipal())) {
            loanHelper.modifyApprovedAmount(loanId, loanResponse.getProposedPrincipal());
        }
        loanResponse.getDisbursementDetails().forEach(disbursementDetail -> {
            if (disbursementDetail.getActualDisbursementDate() == null) {
                loanHelper.disburseLoan(loanId,
                        new PostLoansLoanIdRequest()
                                .actualDisbursementDate(dateFormatter.format(disbursementDetail.getExpectedDisbursementDate()))
                                .dateFormat(DATE_FORMAT).locale("en").transactionAmount(disbursementDetail.getPrincipal()));
            }
        });
        GetLoansLoanIdTransactionsTemplateResponse prepayDetail = transactionHelper.getPrepaymentAmount(loanId,
                dateFormatter.format(cleanupDate), DATE_FORMAT);
        LocalDate transactionDate = prepayDetail.getDate();
        Double amount = prepayDetail.getAmount() != null ? prepayDetail.getAmount().doubleValue() : 0.0;
        Double netDisbursalAmount = prepayDetail.getNetDisbursalAmount() != null ? prepayDetail.getNetDisbursalAmount().doubleValue() : 0.0;
        Double repayAmount = Double.compare(amount, 0.0) > 0 ? amount : netDisbursalAmount;
        transactionHelper.makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATE_FORMAT)
                .transactionDate(dateFormatter.format(transactionDate)).locale("en").transactionAmount(repayAmount));
    }

    private void rejectSubmittedLoan(Long loanId) {
        GetLoansLoanIdResponse details = loanHelper.getLoanDetails(loanId);
        loanHelper.rejectLoan(loanId, new PostLoansLoanIdRequest()
                .rejectedOnDate(dateFormatter.format(details.getTimeline().getSubmittedOnDate())).locale("en").dateFormat(DATE_FORMAT));
    }

    private List<Long> getLoanIdsByStatusId(Integer statusId) {
        return ok(() -> fineractClient.defaultApi().getLoansByStatus(statusId));
    }

    private LocalDate determineCleanupDate() {
        LocalDate tenantDate = Utils.getLocalDateOfTenant();
        try {
            LocalDate maxTxnDate = ok(() -> fineractClient.defaultApi().getMaxTransactionDateOfActiveLoans());
            return maxTxnDate.isAfter(tenantDate) ? maxTxnDate : tenantDate;
        } catch (Exception e) {
            return tenantDate;
        }
    }
}
