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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class LoanTestLifecycleExtension implements AfterEachCallback {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    @Override
    public void afterEach(ExtensionContext context) {
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        // Fully repay ACTIVE loans, so it will not be picked up by any jobs
        List<Integer> loanIds = LoanTransactionHelper.getLoanIdsByStatusId(requestSpec, responseSpec, 300);
        loanIds.forEach(loanId -> {
            HashMap prepayDetail = this.loanTransactionHelper.getPrepayAmount(this.requestSpec, this.responseSpec, loanId);
            LocalDate transactionDate = LocalDate.of((Integer) ((List) prepayDetail.get("date")).get(0),
                    (Integer) ((List) prepayDetail.get("date")).get(1), (Integer) ((List) prepayDetail.get("date")).get(2));
            Double amount = Double.parseDouble(String.valueOf(prepayDetail.get("amount")));
            Double netDisbursalAmount = Double.parseDouble(String.valueOf(prepayDetail.get("netDisbursalAmount")));
            Double repayAmount = Double.compare(amount, 0.0) > 0 ? amount : netDisbursalAmount;
            loanTransactionHelper.makeLoanRepayment((long) loanId, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                    .transactionDate(dateFormatter.format(transactionDate)).locale("en").transactionAmount(repayAmount));
        });
        // Undo APPROVED loans, so the next step can REJECT them, so it will not be picked up by any jobs
        loanIds = LoanTransactionHelper.getLoanIdsByStatusId(requestSpec, responseSpec, 200);
        loanIds.forEach(loanId -> {
            loanTransactionHelper.undoApproval(loanId);
        });
        // Mark SUBMITTED loans, as REJECTED, so it will not be picked up by any jobs
        loanIds = LoanTransactionHelper.getLoanIdsByStatusId(requestSpec, responseSpec, 100);
        loanIds.forEach(loanId -> {
            GetLoansLoanIdResponse details = loanTransactionHelper.getLoanDetails((long) loanId);
            loanTransactionHelper.rejectLoan((long) loanId,
                    new PostLoansLoanIdRequest().rejectedOnDate(dateFormatter.format(details.getTimeline().getSubmittedOnDate()))
                            .locale("en").dateFormat("dd MMMM yyyy"));
        });
    }
}
