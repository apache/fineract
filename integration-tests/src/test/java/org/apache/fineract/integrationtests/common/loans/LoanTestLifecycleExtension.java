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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PutLoansApprovedAmountRequest;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class LoanTestLifecycleExtension implements AfterEachCallback {

    private LoanTransactionHelper loanTransactionHelper;
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    private final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern(DATE_FORMAT).toFormatter();

    @Override
    public void afterEach(ExtensionContext context) {
        BusinessDateHelper.runAt(DateTimeFormatter.ofPattern(DATE_FORMAT).format(Utils.getLocalDateOfTenant()), () -> {
            this.loanTransactionHelper = new LoanTransactionHelper(null, null);

            // Fully repay ACTIVE loans, so it will not be picked up by any jobs
            List<Long> loanIds = LoanTransactionHelper.getLoanIdsByStatusId(300);
            loanIds.forEach(loanId -> {
                GetLoansLoanIdResponse loanResponse = Calls
                        .ok(FineractClientHelper.getFineractClient().loans.retrieveLoan((long) loanId, null, "all", null, null));
                if (MathUtil.isLessThan(loanResponse.getApprovedPrincipal(), loanResponse.getProposedPrincipal())) {
                    // reset approved principal in case it's less than proposed principal so all expected disbursements
                    // can be properly disbursed
                    PutLoansApprovedAmountRequest request = new PutLoansApprovedAmountRequest().amount(loanResponse.getProposedPrincipal())
                            .locale("en");
                    Calls.ok(FineractClientHelper.getFineractClient().loans.modifyLoanApprovedAmount(loanId, request));
                }
                loanResponse.getDisbursementDetails().forEach(disbursementDetail -> {
                    if (disbursementDetail.getActualDisbursementDate() == null) {
                        loanTransactionHelper.disburseLoan((long) loanId,
                                new PostLoansLoanIdRequest()
                                        .actualDisbursementDate(dateFormatter.format(disbursementDetail.getExpectedDisbursementDate()))
                                        .dateFormat(DATE_FORMAT).locale("en")
                                        .transactionAmount(BigDecimal.valueOf(disbursementDetail.getPrincipal())));
                    }
                });
                loanResponse = Calls
                        .ok(FineractClientHelper.getFineractClient().loans.retrieveLoan((long) loanId, null, "all", null, null));
                GetLoansLoanIdTransactionsTemplateResponse prepayDetail = this.loanTransactionHelper.getPrepaymentAmount(loanId,
                        dateFormatter.format(Utils.getLocalDateOfTenant()), DATE_FORMAT);
                LocalDate transactionDate = prepayDetail.getDate();
                Double amount = prepayDetail.getAmount();
                Double netDisbursalAmount = prepayDetail.getNetDisbursalAmount();
                Double repayAmount = Double.compare(amount, 0.0) > 0 ? amount : netDisbursalAmount;
                loanTransactionHelper.makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATE_FORMAT)
                        .transactionDate(dateFormatter.format(transactionDate)).locale("en").transactionAmount(repayAmount));
            });
            // Undo APPROVED loans, so the next step can REJECT them, so it will not be picked up by any jobs
            loanIds = LoanTransactionHelper.getLoanIdsByStatusId(200);
            loanIds.forEach(loanId -> {
                loanTransactionHelper.undoApprovalForLoan(loanId, new PostLoansLoanIdRequest());
            });
            // Mark SUBMITTED loans, as REJECTED, so it will not be picked up by any jobs
            loanIds = LoanTransactionHelper.getLoanIdsByStatusId(100);
            loanIds.forEach(loanId -> {
                GetLoansLoanIdResponse details = loanTransactionHelper.getLoanDetails((long) loanId);
                loanTransactionHelper.rejectLoan(loanId,
                        new PostLoansLoanIdRequest().rejectedOnDate(dateFormatter.format(details.getTimeline().getSubmittedOnDate()))
                                .locale("en").dateFormat(DATE_FORMAT));
            });
            loanIds = LoanTransactionHelper.getLoanIdsByStatusId(300);
            assertEquals(0, loanIds.size());
        });
    }
}
