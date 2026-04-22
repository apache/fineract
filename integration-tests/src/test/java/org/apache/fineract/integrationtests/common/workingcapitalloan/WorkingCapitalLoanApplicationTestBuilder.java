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
    private Long breachId;
    private Long delinquencyBucketId;
    private List<String> paymentAllocationTypes;
    private Integer delinquencyGraceDays;
    private String delinquencyStartType;

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

    public WorkingCapitalLoanApplicationTestBuilder withBreachId(final Long breachId) {
        this.breachId = breachId;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withDelinquencyBucketId(final Long delinquencyBucketId) {
        this.delinquencyBucketId = delinquencyBucketId;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withDelinquencyGraceDays(final Integer delinquencyGraceDays) {
        this.delinquencyGraceDays = delinquencyGraceDays;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withDelinquencyStartType(final String delinquencyStartType) {
        this.delinquencyStartType = delinquencyStartType;
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
        json.addProperty("delinquencyGraceDays", delinquencyGraceDays);
        json.addProperty("delinquencyStartType", delinquencyStartType);
        if (breachId != null) {
            json.addProperty("breachId", breachId);
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
        if (delinquencyGraceDays != null) {
            json.addProperty("delinquencyGraceDays", delinquencyGraceDays);
        }
        if (delinquencyStartType != null) {
            json.addProperty("delinquencyStartType", delinquencyStartType);
        }
        if (breachId != null) {
            json.addProperty("breachId", breachId);
        }
        if (paymentAllocationTypes != null && !paymentAllocationTypes.isEmpty()) {
            json.add("paymentAllocation", buildPaymentAllocationJson());
        }
        return json;
    }

    public static String buildApproveJson(final LocalDate approvedOnDate, final BigDecimal approvedLoanAmount,
            final BigDecimal discountAmount) {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", DEFAULT_LOCALE);
        json.addProperty("dateFormat", DEFAULT_DATE_FORMAT);
        if (approvedOnDate != null) {
            json.addProperty("approvedOnDate", approvedOnDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        // expectedDisbursementDate is mandatory — default to 7 days after approval
        final LocalDate disbursementDate = approvedOnDate != null ? approvedOnDate.plusDays(7)
                : LocalDate.now(ZoneId.systemDefault()).plusDays(7);
        json.addProperty("expectedDisbursementDate", disbursementDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (approvedLoanAmount != null) {
            json.addProperty("approvedLoanAmount", approvedLoanAmount);
        }
        if (discountAmount != null) {
            json.addProperty("discountAmount", discountAmount);
        }
        return json.toString();
    }

    public static String buildApproveJson(final LocalDate approvedOnDate) {
        return buildApproveJson(approvedOnDate, null, null);
    }

    public static String buildRejectJson(final LocalDate rejectedOnDate) {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", DEFAULT_LOCALE);
        json.addProperty("dateFormat", DEFAULT_DATE_FORMAT);
        if (rejectedOnDate != null) {
            json.addProperty("rejectedOnDate", rejectedOnDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        return json.toString();
    }

    public static String buildUndoApproveJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", DEFAULT_LOCALE);
        json.addProperty("dateFormat", DEFAULT_DATE_FORMAT);
        return json.toString();
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
