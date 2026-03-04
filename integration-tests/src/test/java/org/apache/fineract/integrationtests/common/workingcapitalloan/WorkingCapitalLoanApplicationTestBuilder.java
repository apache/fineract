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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Builds JSON request bodies for Working Capital Loan application API (submit, modify).
 */
public class WorkingCapitalLoanApplicationTestBuilder {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_LOCALE = "en";

    private Long clientId;
    private Long productId;
    private Long fundId;
    private String accountNo;
    private String externalId;
    private BigDecimal principal;
    private BigDecimal periodPaymentRate;
    private BigDecimal totalPayment;
    private BigDecimal discount;
    private LocalDate submittedOnDate;
    private LocalDate expectedDisbursementDate;
    private String submittedOnNote;
    private Integer repaymentEvery;
    private String repaymentFrequencyType;
    private Long delinquencyBucketId;
    private List<String> paymentAllocationTypes;

    public WorkingCapitalLoanApplicationTestBuilder withClientId(final Long clientId) {
        this.clientId = clientId;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withProductId(final Long productId) {
        this.productId = productId;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withFundId(final Long fundId) {
        this.fundId = fundId;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withAccountNo(final String accountNo) {
        this.accountNo = accountNo;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withExternalId(final String externalId) {
        this.externalId = externalId;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withPrincipal(final BigDecimal principal) {
        this.principal = principal;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withPeriodPaymentRate(final BigDecimal periodPaymentRate) {
        this.periodPaymentRate = periodPaymentRate;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withTotalPayment(final BigDecimal totalPayment) {
        this.totalPayment = totalPayment;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withDiscount(final BigDecimal discount) {
        this.discount = discount;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withSubmittedOnDate(final LocalDate submittedOnDate) {
        this.submittedOnDate = submittedOnDate;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withExpectedDisbursementDate(final LocalDate expectedDisbursementDate) {
        this.expectedDisbursementDate = expectedDisbursementDate;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withSubmittedOnNote(final String submittedOnNote) {
        this.submittedOnNote = submittedOnNote;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withRepaymentEvery(final Integer repaymentEvery) {
        this.repaymentEvery = repaymentEvery;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withRepaymentFrequencyType(final String repaymentFrequencyType) {
        this.repaymentFrequencyType = repaymentFrequencyType;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withDelinquencyBucketId(final Long delinquencyBucketId) {
        this.delinquencyBucketId = delinquencyBucketId;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withPaymentAllocationTypes(final List<String> paymentAllocationTypes) {
        this.paymentAllocationTypes = paymentAllocationTypes;
        return this;
    }

    /**
     * Builds JSON for submit (create) request.
     */
    public String buildSubmitJson() {
        final JsonObject json = buildBaseJson();
        return json.toString();
    }

    /**
     * Builds JSON for modify (update) request. Only includes fields that are set.
     */
    public String buildModifyJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", DEFAULT_LOCALE);
        json.addProperty("dateFormat", DEFAULT_DATE_FORMAT);
        if (clientId != null) {
            json.addProperty("clientId", clientId);
        }
        if (productId != null) {
            json.addProperty("productId", productId);
        }
        if (fundId != null) {
            json.addProperty("fundId", fundId);
        }
        if (accountNo != null) {
            json.addProperty("accountNo", accountNo);
        }
        if (externalId != null) {
            json.addProperty("externalId", externalId);
        }
        if (principal != null) {
            json.addProperty("principalAmount", principal);
        }
        if (periodPaymentRate != null) {
            json.addProperty("periodPaymentRate", periodPaymentRate);
        }
        if (totalPayment != null) {
            json.addProperty("totalPayment", totalPayment);
        }
        if (discount != null) {
            json.addProperty("discount", discount);
        }
        if (submittedOnDate != null) {
            json.addProperty("submittedOnDate", submittedOnDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (expectedDisbursementDate != null) {
            json.addProperty("expectedDisbursementDate", expectedDisbursementDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (submittedOnNote != null) {
            json.addProperty("submittedOnNote", submittedOnNote);
        }
        if (repaymentEvery != null) {
            json.addProperty("repaymentEvery", repaymentEvery);
        }
        if (repaymentFrequencyType != null) {
            json.addProperty("repaymentFrequencyType", repaymentFrequencyType);
        }
        if (delinquencyBucketId != null) {
            json.addProperty("delinquencyBucketId", delinquencyBucketId);
        }
        if (paymentAllocationTypes != null && !paymentAllocationTypes.isEmpty()) {
            json.add("paymentAllocation", buildPaymentAllocationJson());
        }
        return json.toString();
    }

    private JsonObject buildBaseJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", DEFAULT_LOCALE);
        json.addProperty("dateFormat", DEFAULT_DATE_FORMAT);
        json.addProperty("clientId", clientId);
        json.addProperty("productId", productId);
        if (fundId != null) {
            json.addProperty("fundId", fundId);
        }
        if (accountNo != null) {
            json.addProperty("accountNo", accountNo);
        }
        if (externalId != null) {
            json.addProperty("externalId", externalId);
        }
        json.addProperty("principalAmount", principal);
        json.addProperty("periodPaymentRate", periodPaymentRate);
        json.addProperty("totalPayment", totalPayment != null ? totalPayment : principal);
        if (discount != null) {
            json.addProperty("discount", discount);
        }
        if (submittedOnDate != null) {
            json.addProperty("submittedOnDate", submittedOnDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        json.addProperty("expectedDisbursementDate",
                expectedDisbursementDate != null ? expectedDisbursementDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        : LocalDate.now(ZoneId.systemDefault()).plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (submittedOnNote != null) {
            json.addProperty("submittedOnNote", submittedOnNote);
        }
        if (repaymentEvery != null) {
            json.addProperty("repaymentEvery", repaymentEvery);
        }
        if (repaymentFrequencyType != null) {
            json.addProperty("repaymentFrequencyType", repaymentFrequencyType);
        }
        if (delinquencyBucketId != null) {
            json.addProperty("delinquencyBucketId", delinquencyBucketId);
        }
        if (paymentAllocationTypes != null && !paymentAllocationTypes.isEmpty()) {
            json.add("paymentAllocation", buildPaymentAllocationJson());
        }
        return json;
    }

    private JsonArray buildPaymentAllocationJson() {
        final JsonArray paymentAllocation = new JsonArray();
        final JsonObject rule = new JsonObject();
        rule.addProperty("transactionType", "DEFAULT");
        final JsonArray order = new JsonArray();
        int ord = 1;
        for (final String type : paymentAllocationTypes) {
            final JsonObject item = new JsonObject();
            item.addProperty("paymentAllocationRule", type);
            item.addProperty("order", ord++);
            order.add(item);
        }
        rule.add("paymentAllocationOrder", order);
        paymentAllocation.add(rule);
        return paymentAllocation;
    }
}
