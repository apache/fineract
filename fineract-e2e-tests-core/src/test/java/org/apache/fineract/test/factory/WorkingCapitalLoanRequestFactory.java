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
package org.apache.fineract.test.factory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdDiscountRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.test.data.workingcapitalproduct.DefaultWorkingCapitalLoanProduct;
import org.apache.fineract.test.data.workingcapitalproduct.WorkingCapitalLoanProductResolver;
import org.apache.fineract.test.helper.Utils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanRequestFactory {

    private final WorkingCapitalLoanProductResolver workingCapitalLoanProductResolver;

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DEFAULT_LOCALE = "en";
    public static final DefaultWorkingCapitalLoanProduct DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT = DefaultWorkingCapitalLoanProduct.WCLP;
    public static final BigDecimal DEFAULT_PRINCIPAL = new BigDecimal(100);
    public static final BigDecimal DEFAULT_DISCOUNT = new BigDecimal(100);
    public static final BigDecimal DEFAULT_TOTAL_PAYMENT = new BigDecimal(100);
    public static final BigDecimal DEFAULT_PERIOD_PAYMENT_RATE = new BigDecimal(1);
    public static final BigDecimal DEFAULT_DISCOUNT_ZERO = BigDecimal.ZERO;

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static final String DATE_SUBMIT_STRING = FORMATTER.format(Utils.now().minusMonths(1L));

    public PostWorkingCapitalLoansRequest defaultWorkingCapitalLoansRequest(Long clientId) {
        return new PostWorkingCapitalLoansRequest()//
                .clientId(clientId)//
                .productId(workingCapitalLoanProductResolver.resolve(DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT))//
                .submittedOnDate(DATE_SUBMIT_STRING)//
                .expectedDisbursementDate(DATE_SUBMIT_STRING)//
                .principalAmount(DEFAULT_PRINCIPAL)//
                .totalPayment(DEFAULT_TOTAL_PAYMENT)//
                .periodPaymentRate(DEFAULT_PERIOD_PAYMENT_RATE)//
                .discount(DEFAULT_DISCOUNT_ZERO)//
                .locale(DEFAULT_LOCALE)//
                .dateFormat(DATE_FORMAT);//
    }

    public PutWorkingCapitalLoansLoanIdRequest defaultModifyWorkingCapitalLoansRequest() {
        return new PutWorkingCapitalLoansLoanIdRequest()//
                .locale(DEFAULT_LOCALE)//
                .dateFormat(DATE_FORMAT);//
    }

    public PostWorkingCapitalLoansLoanIdRequest defaultWorkingCapitalLoanApproveRequest() {
        return new PostWorkingCapitalLoansLoanIdRequest()//
                .approvedOnDate(DATE_SUBMIT_STRING)//
                .expectedDisbursementDate(DATE_SUBMIT_STRING)//
                .approvedLoanAmount(DEFAULT_PRINCIPAL)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//
    }

    public PostWorkingCapitalLoansLoanIdRequest defaultWorkingCapitalLoanRejectRequest() {
        return new PostWorkingCapitalLoansLoanIdRequest()//
                .rejectedOnDate(DATE_SUBMIT_STRING)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//
    }

    public PostWorkingCapitalLoansLoanIdRequest defaultWorkingCapitalLoanUndoApprovalRequest() {
        return new PostWorkingCapitalLoansLoanIdRequest()//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//
    }

    public PostWorkingCapitalLoansLoanIdRequest defaultWorkingCapitalLoanDisburseRequest() {
        return new PostWorkingCapitalLoansLoanIdRequest()//
                .actualDisbursementDate(DATE_SUBMIT_STRING)//
                .transactionAmount(DEFAULT_PRINCIPAL)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//
    }

    public PostWorkingCapitalLoansLoanIdRequest defaultWorkingCapitalLoanUndoDisburseRequest() {
        return new PostWorkingCapitalLoansLoanIdRequest()//
                .note("")//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//
    }

    public PostWorkingCapitalLoansDelinquencyActionRequest defaultWorkingCapitalLoansDelinquencyActionRequest(String action) {
        return new PostWorkingCapitalLoansDelinquencyActionRequest()//
                .action(action)//
                .startDate(DATE_SUBMIT_STRING)//
                .endDate(DATE_SUBMIT_STRING)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//
    }

    public PutWorkingCapitalLoansLoanIdDiscountRequest defaultWorkingCapitalLoanUpdateDiscountRequest() {
        return new PutWorkingCapitalLoansLoanIdDiscountRequest()//
                .discountAmount(DEFAULT_DISCOUNT).note("")//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//
    }
}
