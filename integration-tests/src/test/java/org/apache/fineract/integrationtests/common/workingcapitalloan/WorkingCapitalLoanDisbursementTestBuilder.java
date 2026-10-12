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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsPaymentDetailRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdDisbursementPaymentDetails;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;

/**
 * Builds request bodies for Working Capital Loan Disbursement API.
 */
public final class WorkingCapitalLoanDisbursementTestBuilder {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_LOCALE = "en";

    private WorkingCapitalLoanDisbursementTestBuilder() {}

    public static PostWorkingCapitalLoansLoanIdRequest buildDisburseRequest(final LocalDate actualDisbursementDate,
            final BigDecimal transactionAmount, final BigDecimal discountAmount, final String note, final Integer paymentTypeId,
            final String accountNumber, final String checkNumber, final String routingCode, final String receiptNumber,
            final String bankNumber, final String externalId) {
        final PostWorkingCapitalLoansLoanIdRequest request = baseLoanIdRequest();
        if (actualDisbursementDate != null) {
            request.actualDisbursementDate(format(actualDisbursementDate));
        }
        if (transactionAmount != null) {
            request.transactionAmount(transactionAmount);
        }
        if (discountAmount != null) {
            request.discountAmount(discountAmount);
        }
        if (note != null) {
            request.note(note);
        }
        if (paymentTypeId != null || accountNumber != null || checkNumber != null || routingCode != null || receiptNumber != null
                || bankNumber != null) {
            request.paymentDetails(
                    new PostWorkingCapitalLoansLoanIdDisbursementPaymentDetails().paymentTypeId(paymentTypeId).accountNumber(accountNumber)
                            .checkNumber(checkNumber).routingCode(routingCode).receiptNumber(receiptNumber).bankNumber(bankNumber));
        }
        if (externalId != null) {
            request.externalId(externalId);
        }
        return request;
    }

    public static PostWorkingCapitalLoansLoanIdRequest buildDisburseRequest(final LocalDate actualDisbursementDate,
            final BigDecimal transactionAmount, final BigDecimal discountAmount, final String note, final Integer paymentTypeId,
            final String accountNumber, final String checkNumber, final String routingCode, final String receiptNumber,
            final String bankNumber) {
        return buildDisburseRequest(actualDisbursementDate, transactionAmount, discountAmount, note, paymentTypeId, accountNumber,
                checkNumber, routingCode, receiptNumber, bankNumber, null);
    }

    public static PostWorkingCapitalLoansLoanIdRequest buildDisburseRequest(final LocalDate actualDisbursementDate,
            final BigDecimal transactionAmount) {
        return buildDisburseRequest(actualDisbursementDate, transactionAmount, null, null, null, null, null, null, null, null);
    }

    public static PostWorkingCapitalLoansLoanIdRequest buildDisburseRequest(final LocalDate actualDisbursementDate,
            final BigDecimal transactionAmount, final Long classificationId) {
        final PostWorkingCapitalLoansLoanIdRequest request = baseLoanIdRequest();
        if (actualDisbursementDate != null) {
            request.actualDisbursementDate(format(actualDisbursementDate));
        }
        if (transactionAmount != null) {
            request.transactionAmount(transactionAmount);
        }
        if (classificationId != null) {
            request.classificationId(classificationId);
        }
        return request;
    }

    public static PostWorkingCapitalLoansLoanIdRequest buildUndoDisburseRequest() {
        return buildUndoDisburseRequest(null);
    }

    public static PostWorkingCapitalLoansLoanIdRequest buildUndoDisburseRequest(final String note) {
        final PostWorkingCapitalLoansLoanIdRequest request = baseLoanIdRequest();
        if (note != null) {
            request.note(note);
        }
        return request;
    }

    public static PostWorkingCapitalLoansLoanIdRequest buildUpdateDiscountRequest(final BigDecimal discountAmount, final String note) {
        final PostWorkingCapitalLoansLoanIdRequest request = baseLoanIdRequest();
        if (discountAmount != null) {
            request.discountAmount(discountAmount);
        }
        if (note != null) {
            request.note(note);
        }
        return request;
    }

    public static PostWorkingCapitalLoanTransactionsRequest buildRepaymentRequest(final LocalDate transactionDate,
            final BigDecimal transactionAmount, final Long classificationId, final String note, final Integer paymentTypeId,
            final String accountNumber) {
        return buildTransactionRequest(transactionDate, transactionAmount, classificationId, note, paymentTypeId, accountNumber, null);
    }

    public static PostWorkingCapitalLoanTransactionsRequest buildTransactionRequest(final LocalDate transactionDate,
            final BigDecimal transactionAmount, final Long classificationId, final String note, final Integer paymentTypeId,
            final String accountNumber, final String externalId) {
        final PostWorkingCapitalLoanTransactionsRequest request = new PostWorkingCapitalLoanTransactionsRequest().locale(DEFAULT_LOCALE)
                .dateFormat(DEFAULT_DATE_FORMAT);
        if (transactionDate != null) {
            request.transactionDate(format(transactionDate));
        }
        if (transactionAmount != null) {
            request.transactionAmount(transactionAmount);
        }
        if (classificationId != null) {
            request.classificationId(classificationId);
        }
        if (note != null) {
            request.note(note);
        }
        if (paymentTypeId != null || accountNumber != null) {
            request.paymentDetails(new PostWorkingCapitalLoanTransactionsPaymentDetailRequest()
                    .paymentTypeId(paymentTypeId != null ? paymentTypeId.longValue() : null).accountNumber(accountNumber));
        }
        if (externalId != null) {
            request.externalId(externalId);
        }
        return request;
    }

    public static PostWorkingCapitalLoanTransactionsRequest buildCreditBalanceRefundRequest(final LocalDate transactionDate,
            final BigDecimal transactionAmount, final Long classificationId, final String note, final Integer paymentTypeId,
            final String accountNumber) {
        return buildTransactionRequest(transactionDate, transactionAmount, classificationId, note, paymentTypeId, accountNumber, null);
    }

    public static PostWorkingCapitalLoanTransactionsRequest buildChargeOffRequest(final LocalDate transactionDate, final String note) {
        final PostWorkingCapitalLoanTransactionsRequest request = new PostWorkingCapitalLoanTransactionsRequest().locale(DEFAULT_LOCALE)
                .dateFormat(DEFAULT_DATE_FORMAT);
        if (transactionDate != null) {
            request.transactionDate(format(transactionDate));
        }
        if (note != null) {
            request.note(note);
        }
        return request;
    }

    public static PostWorkingCapitalLoanTransactionsRequest buildUndoChargeOffRequest(final String note) {
        final PostWorkingCapitalLoanTransactionsRequest request = new PostWorkingCapitalLoanTransactionsRequest().locale(DEFAULT_LOCALE);
        if (note != null) {
            request.note(note);
        }
        return request;
    }

    public static PostWorkingCapitalLoanTransactionsRequest buildWriteOffRequest(final LocalDate transactionDate) {
        return buildWriteOffRequest(transactionDate, null);
    }

    public static PostWorkingCapitalLoanTransactionsRequest buildWriteOffRequest(final LocalDate transactionDate,
            final Long writeoffReasonId) {
        final PostWorkingCapitalLoanTransactionsRequest request = new PostWorkingCapitalLoanTransactionsRequest().locale(DEFAULT_LOCALE)
                .dateFormat(DEFAULT_DATE_FORMAT);
        if (transactionDate != null) {
            request.transactionDate(format(transactionDate));
        }
        if (writeoffReasonId != null) {
            request.writeoffReasonId(writeoffReasonId);
        }
        return request;
    }

    public static PostWorkingCapitalLoanTransactionsRequest buildUndoWriteOffRequest() {
        return new PostWorkingCapitalLoanTransactionsRequest().locale(DEFAULT_LOCALE);
    }

    private static PostWorkingCapitalLoansLoanIdRequest baseLoanIdRequest() {
        return new PostWorkingCapitalLoansLoanIdRequest().locale(DEFAULT_LOCALE).dateFormat(DEFAULT_DATE_FORMAT);
    }

    private static String format(final LocalDate date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
