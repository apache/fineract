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

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Builds JSON request bodies for Working Capital Loan Disbursement API.
 */
public final class WorkingCapitalLoanDisbursementTestBuilder {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_LOCALE = "en";

    private WorkingCapitalLoanDisbursementTestBuilder() {}

    public static String buildDisburseJson(final LocalDate actualDisbursementDate, final BigDecimal transactionAmount,
            final BigDecimal discountAmount, final String note, final Integer paymentTypeId, final String accountNumber,
            final String checkNumber, final String routingCode, final String receiptNumber, final String bankNumber,
            final String externalId) {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", DEFAULT_LOCALE);
        json.addProperty("dateFormat", DEFAULT_DATE_FORMAT);
        if (actualDisbursementDate != null) {
            json.addProperty("actualDisbursementDate", actualDisbursementDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (transactionAmount != null) {
            json.addProperty("transactionAmount", transactionAmount);
        }
        if (discountAmount != null) {
            json.addProperty("discountAmount", discountAmount);
        }
        if (note != null) {
            json.addProperty("note", note);
        }
        if (paymentTypeId != null || accountNumber != null || checkNumber != null || routingCode != null || receiptNumber != null
                || bankNumber != null) {
            final JsonObject paymentDetails = new JsonObject();
            if (paymentTypeId != null) {
                paymentDetails.addProperty("paymentTypeId", paymentTypeId);
            }
            if (accountNumber != null) {
                paymentDetails.addProperty("accountNumber", accountNumber);
            }
            if (checkNumber != null) {
                paymentDetails.addProperty("checkNumber", checkNumber);
            }
            if (routingCode != null) {
                paymentDetails.addProperty("routingCode", routingCode);
            }
            if (receiptNumber != null) {
                paymentDetails.addProperty("receiptNumber", receiptNumber);
            }
            if (bankNumber != null) {
                paymentDetails.addProperty("bankNumber", bankNumber);
            }
            json.add("paymentDetails", paymentDetails);
        }
        if (externalId != null) {
            json.addProperty("externalId", externalId);
        }
        return json.toString();
    }

    public static String buildDisburseJson(final LocalDate actualDisbursementDate, final BigDecimal transactionAmount,
            final BigDecimal discountAmount, final String note, final Integer paymentTypeId, final String accountNumber,
            final String checkNumber, final String routingCode, final String receiptNumber, final String bankNumber) {
        return buildDisburseJson(actualDisbursementDate, transactionAmount, discountAmount, note, paymentTypeId, accountNumber, checkNumber,
                routingCode, receiptNumber, bankNumber, null);
    }

    public static String buildDisburseJson(final LocalDate actualDisbursementDate, final BigDecimal transactionAmount) {
        return buildDisburseJson(actualDisbursementDate, transactionAmount, null, null, null, null, null, null, null, null);
    }

    public static String buildDisburseJson(final LocalDate actualDisbursementDate, final BigDecimal transactionAmount,
            final Long classificationId) {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", DEFAULT_LOCALE);
        json.addProperty("dateFormat", DEFAULT_DATE_FORMAT);
        if (actualDisbursementDate != null) {
            json.addProperty("actualDisbursementDate", actualDisbursementDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (transactionAmount != null) {
            json.addProperty("transactionAmount", transactionAmount);
        }
        if (classificationId != null) {
            json.addProperty("classificationId", classificationId);
        }
        return json.toString();
    }

    public static String buildUndoDisburseJson() {
        return buildUndoDisburseJson(null);
    }

    public static String buildUndoDisburseJson(final String note) {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", DEFAULT_LOCALE);
        json.addProperty("dateFormat", DEFAULT_DATE_FORMAT);
        if (note != null) {
            json.addProperty("note", note);
        }
        return json.toString();
    }

    public static String buildUpdateDiscountJson(final BigDecimal discountAmount, final String note) {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", DEFAULT_LOCALE);
        json.addProperty("dateFormat", DEFAULT_DATE_FORMAT);
        if (discountAmount != null) {
            json.addProperty("discountAmount", discountAmount);
        }
        if (note != null) {
            json.addProperty("note", note);
        }
        return json.toString();
    }

    public static String buildRepaymentJson(final LocalDate transactionDate, final BigDecimal transactionAmount,
            final Long classificationId, final String note, final Integer paymentTypeId, final String accountNumber) {
        return buildTransactionJson(transactionDate, transactionAmount, classificationId, note, paymentTypeId, accountNumber, null);
    }

    public static String buildTransactionJson(final LocalDate transactionDate, final BigDecimal transactionAmount,
            final Long classificationId, final String note, final Integer paymentTypeId, final String accountNumber,
            final String externalId) {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", DEFAULT_LOCALE);
        json.addProperty("dateFormat", DEFAULT_DATE_FORMAT);
        if (transactionDate != null) {
            json.addProperty("transactionDate", transactionDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (transactionAmount != null) {
            json.addProperty("transactionAmount", transactionAmount);
        }
        if (classificationId != null) {
            json.addProperty("classificationId", classificationId);
        }
        if (note != null) {
            json.addProperty("note", note);
        }
        if (paymentTypeId != null || accountNumber != null) {
            final JsonObject paymentDetails = new JsonObject();
            if (paymentTypeId != null) {
                paymentDetails.addProperty("paymentTypeId", paymentTypeId);
            }
            if (accountNumber != null) {
                paymentDetails.addProperty("accountNumber", accountNumber);
            }
            json.add("paymentDetails", paymentDetails);
        }
        if (externalId != null) {
            json.addProperty("externalId", externalId);
        }
        return json.toString();
    }

    public static String buildCreditBalanceRefundJson(final LocalDate transactionDate, final BigDecimal transactionAmount,
            final Long classificationId, final String note, final Integer paymentTypeId, final String accountNumber) {
        return buildTransactionJson(transactionDate, transactionAmount, classificationId, note, paymentTypeId, accountNumber, null);
    }
}
