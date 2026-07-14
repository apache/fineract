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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.models.PostPaymentAllocationOrder;
import org.apache.fineract.client.models.PostPaymentAllocationRule;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansOriginatorData;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRequest;

/**
 * Builds request bodies for Working Capital Loan application API (submit, modify).
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
    private BigDecimal totalPaymentVolume;
    private BigDecimal discount;
    private LocalDate submittedOnDate;
    private LocalDate expectedDisbursementDate;
    private String submittedOnNote;
    private Integer repaymentEvery;
    private String repaymentFrequencyType;
    private Long breachId;
    private Long nearBreachId;
    private Long delinquencyBucketId;
    private List<String> paymentAllocationTypes;
    private Integer delinquencyGraceDays;
    private String delinquencyStartType;
    private Integer breachGraceDays;
    private String breachStartType;
    private List<PostWorkingCapitalLoansOriginatorData> originators;

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

    public WorkingCapitalLoanApplicationTestBuilder withTotalPaymentVolume(final BigDecimal totalPaymentVolume) {
        this.totalPaymentVolume = totalPaymentVolume;
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

    public WorkingCapitalLoanApplicationTestBuilder withNearBreachId(final Long nearBreachId) {
        this.nearBreachId = nearBreachId;
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

    public WorkingCapitalLoanApplicationTestBuilder withBreachGraceDays(final Integer breachGraceDays) {
        this.breachGraceDays = breachGraceDays;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withBreachStartType(final String breachStartType) {
        this.breachStartType = breachStartType;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withPaymentAllocationTypes(final List<String> paymentAllocationTypes) {
        this.paymentAllocationTypes = paymentAllocationTypes;
        return this;
    }

    public WorkingCapitalLoanApplicationTestBuilder withOriginators(final List<PostWorkingCapitalLoansOriginatorData> originators) {
        this.originators = originators;
        return this;
    }

    public PostWorkingCapitalLoansRequest buildSubmitRequest() {
        return populateSubmitRequest(new PostWorkingCapitalLoansRequest())
                .totalPaymentVolume(totalPaymentVolume != null ? totalPaymentVolume : principal)
                .expectedDisbursementDate(expectedDisbursementDate != null ? format(expectedDisbursementDate)
                        : format(LocalDate.now(ZoneId.systemDefault()).plusDays(7)));
    }

    public PutWorkingCapitalLoansLoanIdRequest buildModifyRequest() {
        return populateModifyRequest(new PutWorkingCapitalLoansLoanIdRequest());
    }

    private PostWorkingCapitalLoansRequest populateSubmitRequest(final PostWorkingCapitalLoansRequest request) {
        request.locale(DEFAULT_LOCALE).dateFormat(DEFAULT_DATE_FORMAT).clientId(clientId).productId(productId).principalAmount(principal)
                .periodPaymentRate(periodPaymentRate);
        if (fundId != null) {
            request.fundId(fundId);
        }
        if (accountNo != null) {
            request.accountNo(accountNo);
        }
        if (externalId != null) {
            request.externalId(externalId);
        }
        if (discount != null) {
            request.discount(discount);
        }
        if (submittedOnDate != null) {
            request.submittedOnDate(format(submittedOnDate));
        }
        if (submittedOnNote != null) {
            request.submittedOnNote(submittedOnNote);
        }
        if (repaymentEvery != null) {
            request.repaymentEvery(repaymentEvery);
        }
        if (repaymentFrequencyType != null) {
            request.repaymentFrequencyType(PostWorkingCapitalLoansRequest.RepaymentFrequencyTypeEnum.fromValue(repaymentFrequencyType));
        }
        if (delinquencyBucketId != null) {
            request.delinquencyBucketId(delinquencyBucketId);
        }
        if (delinquencyGraceDays != null) {
            request.delinquencyGraceDays(delinquencyGraceDays);
        }
        if (delinquencyStartType != null) {
            request.delinquencyStartType(delinquencyStartType);
        }
        if (breachGraceDays != null) {
            request.breachGraceDays(breachGraceDays);
        }
        if (breachStartType != null) {
            request.breachStartType(breachStartType);
        }
        if (breachId != null) {
            request.breachId(breachId);
        }
        if (nearBreachId != null) {
            request.nearBreachId(nearBreachId);
        }
        if (paymentAllocationTypes != null && !paymentAllocationTypes.isEmpty()) {
            request.paymentAllocation(buildPaymentAllocationRules());
        }
        if (originators != null && !originators.isEmpty()) {
            request.originators(originators);
        }
        return request;
    }

    private PutWorkingCapitalLoansLoanIdRequest populateModifyRequest(final PutWorkingCapitalLoansLoanIdRequest request) {
        request.locale(DEFAULT_LOCALE).dateFormat(DEFAULT_DATE_FORMAT);
        if (clientId != null) {
            request.clientId(clientId);
        }
        if (productId != null) {
            request.productId(productId);
        }
        if (fundId != null) {
            request.fundId(fundId);
        }
        if (accountNo != null) {
            request.accountNo(accountNo);
        }
        if (externalId != null) {
            request.externalId(externalId);
        }
        if (principal != null) {
            request.principalAmount(principal);
        }
        if (periodPaymentRate != null) {
            request.periodPaymentRate(periodPaymentRate);
        }
        if (totalPaymentVolume != null) {
            request.totalPaymentVolume(totalPaymentVolume);
        }
        if (discount != null) {
            request.discount(discount);
        }
        if (submittedOnDate != null) {
            request.submittedOnDate(format(submittedOnDate));
        }
        if (expectedDisbursementDate != null) {
            request.expectedDisbursementDate(format(expectedDisbursementDate));
        }
        if (submittedOnNote != null) {
            request.submittedOnNote(submittedOnNote);
        }
        if (repaymentEvery != null) {
            request.repaymentEvery(repaymentEvery);
        }
        if (repaymentFrequencyType != null) {
            request.repaymentFrequencyType(
                    PutWorkingCapitalLoansLoanIdRequest.RepaymentFrequencyTypeEnum.fromValue(repaymentFrequencyType));
        }
        if (delinquencyBucketId != null) {
            request.delinquencyBucketId(delinquencyBucketId);
        }
        request.delinquencyGraceDays(delinquencyGraceDays);
        request.delinquencyStartType(delinquencyStartType);
        request.breachGraceDays(breachGraceDays);
        if (breachId != null) {
            request.breachId(breachId);
        }
        if (nearBreachId != null) {
            request.nearBreachId(nearBreachId);
        }
        if (paymentAllocationTypes != null && !paymentAllocationTypes.isEmpty()) {
            request.paymentAllocation(buildPaymentAllocationRules());
        }
        return request;
    }

    public static PostWorkingCapitalLoansLoanIdRequest buildApproveRequest(final LocalDate approvedOnDate,
            final BigDecimal approvedLoanAmount, final BigDecimal discountAmount) {
        final PostWorkingCapitalLoansLoanIdRequest request = new PostWorkingCapitalLoansLoanIdRequest().locale(DEFAULT_LOCALE)
                .dateFormat(DEFAULT_DATE_FORMAT);
        if (approvedOnDate != null) {
            request.approvedOnDate(format(approvedOnDate));
        }
        final LocalDate disbursementDate = approvedOnDate != null ? approvedOnDate.plusDays(7)
                : LocalDate.now(ZoneId.systemDefault()).plusDays(7);
        request.expectedDisbursementDate(format(disbursementDate));
        if (approvedLoanAmount != null) {
            request.approvedLoanAmount(approvedLoanAmount);
        }
        if (discountAmount != null) {
            request.discountAmount(discountAmount);
        }
        return request;
    }

    public static PostWorkingCapitalLoansLoanIdRequest buildApproveRequest(final LocalDate approvedOnDate) {
        return buildApproveRequest(approvedOnDate, null, null);
    }

    public static PostWorkingCapitalLoansLoanIdRequest buildRejectRequest(final LocalDate rejectedOnDate) {
        final PostWorkingCapitalLoansLoanIdRequest request = new PostWorkingCapitalLoansLoanIdRequest().locale(DEFAULT_LOCALE)
                .dateFormat(DEFAULT_DATE_FORMAT);
        if (rejectedOnDate != null) {
            request.rejectedOnDate(format(rejectedOnDate));
        }
        return request;
    }

    public static PostWorkingCapitalLoansLoanIdRequest buildUndoApproveRequest() {
        return new PostWorkingCapitalLoansLoanIdRequest().locale(DEFAULT_LOCALE).dateFormat(DEFAULT_DATE_FORMAT);
    }

    private List<PostPaymentAllocationRule> buildPaymentAllocationRules() {
        final List<PostPaymentAllocationOrder> order = new ArrayList<>();
        int ord = 1;
        for (final String type : paymentAllocationTypes) {
            order.add(new PostPaymentAllocationOrder().paymentAllocationRule(type).order(ord++));
        }
        return List.of(new PostPaymentAllocationRule().transactionType("DEFAULT").paymentAllocationOrder(order));
    }

    private static String format(final LocalDate date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
