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
package org.apache.fineract.portfolio.workingcapitalloan.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.api.WorkingCapitalLoanProductApiResourceSwagger;

/**
 * Swagger documentation classes for Working Capital Loans API.
 */
public final class WorkingCapitalLoanApiResourceSwagger {

    private WorkingCapitalLoanApiResourceSwagger() {}

    @Schema(description = "Template: loan details (prefilled when productId/clientId provided) plus dropdown options.")
    public static final class GetWorkingCapitalLoansTemplateResponse {

        private GetWorkingCapitalLoansTemplateResponse() {}

        @Schema(description = "Loan details (product, fundId, currency, client, etc.).")
        public GetWorkingCapitalLoansLoanIdResponse loanData;
        public List<WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanProductsResponse> productOptions;
        public Collection<FundData> fundOptions;
        public Collection<WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanProductsResponse.GetDelinquencyBucket> delinquencyBucketOptions;
        public List<StringEnumOptionData> periodFrequencyTypeOptions;
        public List<StringEnumOptionData> delinquencyStartTypeOptions;
        public List<StringEnumOptionData> breachStartTypeOptions;
        public List<StringEnumOptionData> delinquencyMinimumPaymentTypeOptions;
        public List<WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanProductsResponse.GetWorkingCapitalLoanBreach> breachOptions;
    }

    @Schema(description = "GetWorkingCapitalLoansClient")
    public static final class GetWorkingCapitalLoansClient {

        private GetWorkingCapitalLoansClient() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Client One")
        public String displayName;
    }

    @Schema(description = "GetWorkingCapitalLoansPagedResponse (content, totalElements, totalPages, number, size, first, last)")
    public static final class GetWorkingCapitalLoansPagedResponse {

        private GetWorkingCapitalLoansPagedResponse() {}

        public List<GetWorkingCapitalLoansLoanIdResponse> content;
        @Schema(example = "100")
        public Long totalElements;
        @Schema(example = "2")
        public Integer totalPages;
        @Schema(example = "0")
        public Integer number;
        @Schema(example = "50")
        public Integer size;
        public Boolean first;
        public Boolean last;
    }

    @Schema(description = "GetWorkingCapitalLoansLoanIdStatus")
    static final class GetWorkingCapitalLoansLoanIdStatus {

        private GetWorkingCapitalLoansLoanIdStatus() {}

        @Schema(example = "100")
        public Long id;
        @Schema(example = "loanStatusType.submitted.and.pending.approval")
        public String code;
        @Schema(example = "Submitted and pending approval")
        public String value;
        @Schema(example = "true")
        public Boolean pendingApproval;
        @Schema(example = "false")
        public Boolean waitingForDisbursal;
        @Schema(example = "false")
        public Boolean active;
        @Schema(example = "false")
        public Boolean closedObligationsMet;
        @Schema(example = "false")
        public Boolean closedWrittenOff;
        @Schema(example = "false")
        public Boolean closedRescheduled;
        @Schema(example = "false")
        public Boolean closed;
        @Schema(example = "false")
        public Boolean overpaid;
    }

    @Schema(description = "GetWorkingCapitalLoansLoanIdTimeline")
    static final class GetWorkingCapitalLoansLoanIdTimeline {

        private GetWorkingCapitalLoansLoanIdTimeline() {}

        @Schema(example = "[2024, 1, 15]")
        public LocalDate submittedOnDate;
        @Schema(example = "admin")
        public String submittedByUsername;
        @Schema(example = "App")
        public String submittedByFirstname;
        @Schema(example = "Administrator")
        public String submittedByLastname;
        @Schema(example = "[2024, 1, 15]")
        public LocalDate approvedOnDate;
        @Schema(example = "admin")
        public String approvedByUsername;
        @Schema(example = "App")
        public String approvedByFirstname;
        @Schema(example = "Administrator")
        public String approvedByLastname;
        @Schema(example = "[2024, 1, 15]")
        public LocalDate rejectedOnDate;
        @Schema(example = "admin")
        public String rejectedByUsername;
        @Schema(example = "App")
        public String rejectedByFirstname;
        @Schema(example = "Administrator")
        public String rejectedByLastname;
        @Schema(example = "[2024, 2, 1]")
        public LocalDate expectedDisbursementDate;
        @Schema(example = "[2024, 2, 1]")
        public LocalDate actualDisbursementDate;
        @Schema(example = "admin")
        public String disbursedByUsername;
        @Schema(example = "App")
        public String disbursedByFirstname;
        @Schema(example = "Administrator")
        public String disbursedByLastname;
        @Schema(example = "[2024, 2, 1]")
        public LocalDate closedOnDate;
        @Schema(example = "admin")
        public String closedByUsername;
        @Schema(example = "App")
        public String closedByFirstname;
        @Schema(example = "Administrator")
        public String closedByLastname;
        @Schema(example = "[2024, 2, 1]", description = "Expected maturity date")
        public LocalDate expectedMaturityDate;
        @Schema(example = "[2024, 12, 31]", description = "Actual maturity date (when loan is fully paid)")
        public LocalDate actualMaturityDate;
    }

    @Schema(description = "GetWorkingCapitalLoansLoanIdResponse")
    public static final class GetWorkingCapitalLoansLoanIdResponse {

        private GetWorkingCapitalLoansLoanIdResponse() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "WCL-1")
        public String accountNo;
        @Schema(example = "ext-id-001")
        public String externalId;
        @Schema(description = "Client object. Populated only by the loan template endpoint; null in loan details "
                + "(loan details exposes clientId/clientAccountNo/clientName/clientOfficeId instead)")
        public GetWorkingCapitalLoansClient client;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "000000001")
        public String clientAccountNo;
        @Schema(example = "bharath gowda")
        public String clientName;
        @Schema(example = "786444UUUYYH7")
        public String clientExternalId;
        @Schema(example = "1")
        public Long clientOfficeId;
        @Schema(example = "1")
        public Long fundId;
        @Schema(example = "Fund 1")
        public String fundName;
        @Schema(description = "Product object. Populated only by the loan template endpoint; null in loan details "
                + "(loan details exposes loanProductId/loanProductName instead)")
        public WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanProductsResponse product;
        @Schema(example = "1")
        public Long loanProductId;
        @Schema(example = "BNPL")
        public String loanProductName;
        @Schema(example = "Buy now pay later loan product")
        public String loanProductDescription;
        public GetWorkingCapitalLoansLoanIdStatus status;
        public GetWorkingCapitalLoansLoanIdTimeline timeline;
        public BigDecimal proposedPrincipal;
        public BigDecimal approvedPrincipal;
        @Schema(example = "10000.00", description = "Active principal (loanProductRelatedDetails.principal)")
        public BigDecimal principal;
        @Schema(example = "10000.00", description = "Net disbursal amount from the amortization schedule; null if schedule not yet generated")
        public BigDecimal netDisbursalAmount;

        public CurrencyData currency;
        @Schema(example = "1.0")
        public BigDecimal paymentRate;
        @Schema(example = "30")
        public Integer repaymentEvery;
        public StringEnumOptionData repaymentFrequencyType;
        @Schema(description = "Amortization type: EIR or FLAT")
        public StringEnumOptionData amortizationType;
        @Schema(example = "360", description = "NPV day count used by the amortization schedule")
        public Integer npvDayCount;
        @Schema(example = "1", description = "Loan cycle (sequential WC loan counter per client+product)")
        public Integer loanProductCounter;
        @Schema(example = "10500.00")
        public BigDecimal totalPaymentVolume;
        @Schema(example = "0.0", description = "Discount fee set during loan disbursement")
        public BigDecimal discountFee;
        @Schema(example = "0.0", description = "Proposed discount fee at loan submission time")
        public BigDecimal proposedDiscountFee;
        @Schema(example = "0.0", description = "Approved discount fee set during loan approval")
        public BigDecimal approvedDiscountFee;
        @Schema(example = "90", description = "Number of repayments (effectiveTotalTerm from the amortization schedule; for WC this is the "
                + "loan term in days); null if schedule not yet generated")
        public Integer numberOfRepayments;
        @Schema(example = "116.67", description = "Daily expected payment amount from the amortization schedule; null if schedule not yet generated")
        public BigDecimal periodPaymentAmount;
        @Schema(example = "0.000435", description = "Periodic (daily) effective interest rate computed via RATE(); null if schedule not yet generated")
        public BigDecimal dailyEir;
        @Schema(example = "0.1691", description = "Annualized EIR: (1 + dailyEir)^365 − 1; null if schedule not yet generated")
        public BigDecimal calculatedAnnualEir;
        @Schema(description = "Working capital breach)")
        public WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanProductsResponse.GetWorkingCapitalLoanBreach breach;
        public WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanNearBreach nearBreach;
        public WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanProductsResponse.GetDelinquencyBucket delinquencyBucket;
        @Schema(example = "3", description = "Number of grace days before delinquency tracking starts")
        public Integer delinquencyGraceDays;
        @Schema(description = "Delinquency start type: LOAN_CREATION or DISBURSEMENT")
        public StringEnumOptionData delinquencyStartType;
        @Schema(example = "0", description = "Number of days to shift the start of the first breach schedule period after disbursement")
        public Integer breachGraceDays;
        @Schema(description = "Breach start type: LOAN_CREATION or DISBURSEMENT")
        public StringEnumOptionData breachStartType;
        @Schema(example = "[2024, 1, 14]", description = "Start date of the loan's breach, i.e. the fromDate of the earliest breached "
                + "breach schedule period (the breach grace days are already reflected in this date). Null when the loan is not in breach")
        public LocalDate breachStartDate;
        @Schema(example = "[2024, 1, 14]", description = "Start date of the loan's delinquency, i.e. the fromDate of the earliest "
                + "delinquent range schedule period shifted by delinquencyGraceDays. Null when the loan is not delinquent")
        public LocalDate delinquencyStartDate;
        @Schema(example = "[2024, 1, 14]", description = "Last closed business date (COB)")
        public LocalDate lastClosedBusinessDate;
        public List<GetPaymentAllocation> paymentAllocation;
        /**
         * Full list of disbursement details (for multi-disbursement support).
         */
        public List<GetDisbursementDetail> disbursementDetails;
        @Schema(description = "Charges associated with the loan")
        public List<GetWorkingCapitalLoanCharge> charges;
        /**
         * Running balances (principal outstanding, total payment, etc.).
         */
        public GetBalance balance;
        @Schema(description = "Loan summary: principal / fee / penalty totals, income recognition and aggregates")
        public GetWorkingCapitalLoanSummary summary;

        @Schema(description = "Working Capital Loan charge")
        public static final class GetWorkingCapitalLoanCharge {

            private GetWorkingCapitalLoanCharge() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "1")
            public Long chargeId;
            @Schema(example = "nsf fees")
            public String name;
            public EnumOptionData chargeTimeType;
            public LocalDate submittedOnDate;
            public LocalDate dueDate;
            public EnumOptionData chargeCalculationType;
            public CurrencyData currency;
            @Schema(example = "10")
            public BigDecimal amount;
            @Schema(example = "10")
            public BigDecimal amountPaid;
            @Schema(example = "0")
            public BigDecimal amountOutstanding;
            @Schema(example = "false")
            public boolean penalty;
            public EnumOptionData chargePaymentMode;
            @Schema(example = "true")
            public boolean paid;
            @Schema(example = "1")
            public Long loanId;
            @Schema(example = "c9b17bbe-4a4b-4a68-8c69-b9dfb9210f93")
            public String externalId;
            @Schema(example = "8f4a5f3e-7c2d-4d7a-9b1c-2e6d4a8b9c0d")
            public String externalLoanId;
        }

        @Schema(description = "Working Capital Loan summary")
        public static final class GetWorkingCapitalLoanSummary {

            private GetWorkingCapitalLoanSummary() {}

            public CurrencyData currency;
            @Schema(description = "Total principal due: original principal plus principalAdjustment. Already inclusive of "
                    + "principalAdjustment — do not add the two together.")
            public BigDecimal principal;
            public BigDecimal principalPaid;
            @Schema(description = "Principal re-injected by an over-refunding credit balance refund. Already included in principal.")
            public BigDecimal principalAdjustment;
            public BigDecimal principalOutstanding;
            public BigDecimal fee;
            public BigDecimal feePaid;
            public BigDecimal feeOutstanding;
            public BigDecimal penalty;
            public BigDecimal penaltyPaid;
            public BigDecimal penaltyOutstanding;
            public BigDecimal realizedIncomeFromDiscountFee;
            public BigDecimal unrealizedIncomeFromDiscountFee;
            public BigDecimal overpayment;
            public BigDecimal totalDisbursement;
            public BigDecimal totalDiscountFee;
            public BigDecimal totalDiscountFeeAdjustment;
            public BigDecimal totalExpectedRepayment;
            public BigDecimal totalRepayment;
            public BigDecimal totalOutstanding;
        }

        @Schema(description = "Working Capital Delinquency Collection Data")
        public WorkingCapitalCollection delinquent;
        @Schema(description = "Installment-level delinquency flag (Term-compatible name). True when the loan has a delinquency "
                + "bucket configured (Working Capital tracks delinquency at the period level); false otherwise", example = "true")
        public Boolean enableInstallmentLevelDelinquency;
        @Schema(description = "List of originators associated with this loan")
        public List<GetWorkingCapitalLoansLoanIdOriginatorData> originators;
        @Schema(description = "Fraud flag. True when the loan has been marked as fraudulent", example = "false")
        public Boolean fraud;
        @Schema(description = "Whether the loan is charged off (pure accounting tag; the loan stays active)")
        public Boolean chargedOff;
        @Schema(description = "Date the loan was charged off", example = "2026-07-16")
        public LocalDate chargedOffOnDate;
        @Schema(description = "Charge-off reason code value, when one was provided")
        public CodeValueData chargeOffReason;

        @Schema(description = "Originator data associated with the loan")
        public static final class GetWorkingCapitalLoansLoanIdOriginatorData {

            private GetWorkingCapitalLoansLoanIdOriginatorData() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "REV-SHARE-001")
            public String externalId;
            @Schema(example = "PP Merchant")
            public String name;
            @Schema(example = "ACTIVE")
            public String status;
            @Schema(description = "Originator type as a code value (id, name, ...)")
            public CodeValueData originatorType;
            @Schema(description = "Channel type as a code value (id, name, ...)")
            public CodeValueData channelType;
        }
    }

    @Schema(description = "Working capital loan running balances")
    public static final class GetBalance {

        private GetBalance() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "10000.00")
        public BigDecimal principal;
        @Schema(example = "10000.00")
        public BigDecimal principalPaid;
        @Schema(example = "0.00")
        public BigDecimal principalAdjustment;
        @Schema(example = "10000.00")
        public BigDecimal principalOutstanding;
        @Schema(example = "10000.00")
        public BigDecimal fee;
        @Schema(example = "10000.00")
        public BigDecimal feePaid;
        @Schema(example = "10000.00")
        public BigDecimal feeOutstanding;
        @Schema(example = "10000.00")
        public BigDecimal penalty;
        @Schema(example = "10000.00")
        public BigDecimal penaltyPaid;
        @Schema(example = "10000.00")
        public BigDecimal penaltyOutstanding;
        @Schema(example = "10000.00")
        public BigDecimal realizedIncomeFromDiscountFee;
        @Schema(example = "10000.00")
        public BigDecimal unrealizedIncomeFromDiscountFee;
        @Schema(example = "10000.00")
        public BigDecimal overpaymentAmount;
        @Schema(example = "10000.00")
        public BigDecimal totalExpectedRepayment;
        @Schema(example = "10000.00")
        public BigDecimal totalRepayment;
        @Schema(example = "10000.00")
        public BigDecimal totalOutstanding;
        @Schema(example = "10000.00")
        public BigDecimal totalDisbursement;
        @Schema(example = "10000.00")
        public BigDecimal totalDiscountFee;
        @Schema(example = "500.00")
        public BigDecimal totalDiscountFeeAdjustment;
        @Schema(example = "250.00", description = "Cumulative breach past due amount, summed from each breach schedule period's outstanding amount")
        public BigDecimal breachPastDueAmount;
    }

    @Schema(description = "Single disbursement detail (expected and actual)")
    public static final class GetDisbursementDetail {

        private GetDisbursementDetail() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "1")
        public Long loanId;
        public LocalDate expectedDisbursementDate;
        @Schema(example = "10000.00", description = "Expected (planned) disbursement principal")
        public BigDecimal principal;
        public LocalDate expectedMaturityDate;
        public LocalDate actualDisbursementDate;
        @Schema(example = "10000.00", description = "Actually disbursed amount; null until disbursed")
        public BigDecimal actualAmount;
        public String disbursedByUsername;
        public String disbursedByFirstname;
        public String disbursedByLastname;
    }

    @Schema(description = "GetPaymentAllocation")
    public static final class GetPaymentAllocation {

        private GetPaymentAllocation() {}

        @Schema(example = "DEFAULT")
        public String transactionType;
        public List<GetPaymentAllocationOrder> paymentAllocationOrder;
    }

    @Schema(description = "GetPaymentAllocationOrder")
    public static final class GetPaymentAllocationOrder {

        private GetPaymentAllocationOrder() {}

        @Schema(example = "PENALTY")
        public String paymentAllocationRule;
        @Schema(example = "1")
        public Integer order;
    }

    @Schema(description = "PostWorkingCapitalLoansRequest")
    public static final class PostWorkingCapitalLoansRequest {

        private PostWorkingCapitalLoansRequest() {}

        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        public Long clientId;
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        public Long productId;
        @Schema(example = "1")
        public Long fundId;
        @Schema(example = "WCL-1")
        public String accountNo;
        @Schema(example = "ext-id-001")
        public String externalId;
        @Schema(example = "10000.00", requiredMode = Schema.RequiredMode.REQUIRED, description = "Principal (disbursement) amount")
        public BigDecimal principalAmount;
        @Schema(example = "10500.00")
        public BigDecimal totalPaymentVolume;
        @Schema(example = "15 January 2024")
        public String submittedOnDate;
        @Schema(example = "1 February 2024")
        public String expectedDisbursementDate;
        @Schema(example = "Note when submitting")
        public String submittedOnNote;

        @Schema(example = "1.0")
        public BigDecimal periodPaymentRate;
        @Schema(example = "30")
        public Integer repaymentEvery;
        @Schema(example = "DAYS", allowableValues = { "DAYS", "MONTHS", "YEARS" })
        public String repaymentFrequencyType;
        @Schema(example = "0.0")
        public BigDecimal discount;
        @Schema(example = "1")
        public Long breachId;
        @Schema(example = "1")
        public Long nearBreachId;
        @Schema(example = "1")
        public Long delinquencyBucketId;
        @Schema(example = "3")
        public Integer delinquencyGraceDays;
        @Schema(example = "LOAN_CREATION", description = "Delinquency start type: LOAN_CREATION or DISBURSEMENT")
        public String delinquencyStartType;
        @Schema(example = "0", description = "Number of days to shift the start of the first breach schedule period after disbursement")
        public Integer breachGraceDays;
        @Schema(example = "DISBURSEMENT", description = "Breach start type: LOAN_CREATION or DISBURSEMENT")
        public String breachStartType;
        public List<PostPaymentAllocationRule> paymentAllocation;
        @Schema(description = """
                Optional array of originators to associate with this loan. \
                Each entry can reference an existing originator by 'id' or 'externalId'. \
                If the global config 'enable_originator_creation_during_loan_application' is enabled, \
                non-existing originators will be auto-created using the provided details (name, typeId, channelTypeId).""")
        public List<PostWorkingCapitalLoansOriginatorData> originators;

        @Schema(example = "en_GB")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;

        @Schema(description = "PostPaymentAllocationRule")
        public static final class PostPaymentAllocationRule {

            private PostPaymentAllocationRule() {}

            @Schema(example = "DEFAULT")
            public String transactionType;
            public List<PostPaymentAllocationOrder> paymentAllocationOrder;
        }

        @Schema(description = "PostPaymentAllocationOrder")
        public static final class PostPaymentAllocationOrder {

            private PostPaymentAllocationOrder() {}

            @Schema(example = "PENALTY")
            public String paymentAllocationRule;
            @Schema(example = "1")
            public Integer order;
        }

        @Schema(description = "Originator data for loan creation request")
        public static final class PostWorkingCapitalLoansOriginatorData {

            private PostWorkingCapitalLoansOriginatorData() {}

            @Schema(description = "Originator internal ID (use this OR externalId, not both)", example = "1")
            public Long id;

            @Schema(description = "Originator external ID (use this OR id, not both)", example = "REV-SHARE-001")
            public String externalId;

            @Schema(description = "Originator name (used when creating new originator if config enabled)", example = "PP Merchant")
            public String name;

            @Schema(description = "Code value ID for originator type (from LoanOriginatorType code)", example = "1")
            public Long typeId;

            @Schema(description = "Code value ID for channel type (from LoanOriginationChannelType code)", example = "2")
            public Long channelTypeId;
        }
    }

    @Schema(description = "PostWorkingCapitalLoansResponse")
    public static final class PostWorkingCapitalLoansResponse {

        private PostWorkingCapitalLoansResponse() {}

        @Schema(example = "1")
        public Long resourceId;
        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "1")
        public Long loanId;
    }

    @Schema(description = "Payment details for disbursement (Account No, Cheque No, Routing Code, Receipt No, Bank code)")
    public static final class PostWorkingCapitalLoansLoanIdDisbursementPaymentDetails {

        private PostWorkingCapitalLoansLoanIdDisbursementPaymentDetails() {}

        @Schema(example = "1", description = "Payment type id")
        public Integer paymentTypeId;
        @Schema(example = "acc123", description = "Account No")
        public String accountNumber;
        @Schema(example = "che123", description = "Cheque No")
        public String checkNumber;
        @Schema(example = "rou123", description = "Routing Code")
        public String routingCode;
        @Schema(example = "rec123", description = "Receipt No")
        public String receiptNumber;
        @Schema(example = "ban123", description = "Bank code")
        public String bankNumber;
    }

    @Schema(description = "PutWorkingCapitalLoansLoanIdRequest")
    public static final class PutWorkingCapitalLoansLoanIdRequest {

        private PutWorkingCapitalLoansLoanIdRequest() {}

        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "1")
        public Long productId;
        @Schema(example = "1")
        public Long fundId;
        @Schema(example = "WCL-1")
        public String accountNo;
        @Schema(example = "ext-id-001")
        public String externalId;
        @Schema(example = "10000.00", description = "Principal (disbursement) amount")
        public BigDecimal principalAmount;
        @Schema(example = "10500.00")
        public BigDecimal totalPaymentVolume;
        @Schema(example = "15 January 2024")
        public String submittedOnDate;
        @Schema(example = "1 February 2024")
        public String expectedDisbursementDate;
        @Schema(example = "Note when modifying", description = "Max length 500 characters")
        public String submittedOnNote;

        @Schema(example = "1.0")
        public BigDecimal periodPaymentRate;
        @Schema(example = "30")
        public Integer repaymentEvery;
        @Schema(example = "DAYS", allowableValues = { "DAYS", "MONTHS", "YEARS" })
        public String repaymentFrequencyType;
        @Schema(example = "0.0")
        public BigDecimal discount;
        @Schema(example = "1")
        public Long breachId;
        @Schema(example = "1")
        public Long nearBreachId;
        @Schema(example = "1")
        public Long delinquencyBucketId;
        @Schema(example = "3")
        public Integer delinquencyGraceDays;
        @Schema(example = "LOAN_CREATION", description = "Delinquency start type: LOAN_CREATION or DISBURSEMENT")
        public String delinquencyStartType;
        @Schema(example = "0", description = "Number of days to shift the start of the first breach schedule period after disbursement")
        public Integer breachGraceDays;
        @Schema(example = "DISBURSEMENT", description = "Breach start type: LOAN_CREATION or DISBURSEMENT")
        public String breachStartType;
        public List<PostWorkingCapitalLoansRequest.PostPaymentAllocationRule> paymentAllocation;

        @Schema(example = "en_GB")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
    }

    @Schema(description = "PutWorkingCapitalLoansLoanIdResponse")
    public static final class PutWorkingCapitalLoansLoanIdResponse {

        private PutWorkingCapitalLoansLoanIdResponse() {}

        @Schema(example = "1")
        public Long resourceId;
        public Object changes;
    }

    @Schema(description = "DeleteWorkingCapitalLoansLoanIdResponse")
    public static final class DeleteWorkingCapitalLoansLoanIdResponse {

        private DeleteWorkingCapitalLoansLoanIdResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "PostWorkingCapitalLoansLoanIdResponse")
    public static final class PostWorkingCapitalLoansLoanIdResponse {

        private PostWorkingCapitalLoansLoanIdResponse() {}

        @Schema(example = "2")
        public Long officeId;
        @Schema(example = "6")
        public Long clientId;
        @Schema(example = "3")
        public Long loanId;
        @Schema(example = "95174ff9-1a75-4d72-a413-6f9b1cb988b7")
        public String loanExternalId;
        @Schema(example = "3")
        public Long resourceId;
        @Schema(example = "95174ff9-1a75-4d72-a413-6f9b1cb988b7")
        public String resourceExternalId;
        @Schema(example = "3")
        public Long subResourceId;
        @Schema(example = "95174ff9-1a75-4d72-a413-6f9b1cb988b7")
        public String subResourceExternalId;
        public Object changes;
    }

    @Schema(description = "Request for state transition: approve, reject, undoapproval, disburse, undodisbursal")
    public static final class PostWorkingCapitalLoansLoanIdRequest {

        private PostWorkingCapitalLoansLoanIdRequest() {}

        @Schema(example = "15 January 2024", description = "Date of approval")
        public String approvedOnDate;
        @Schema(example = "10000.00", description = "Approved principal amount (optional, defaults to proposed principal)")
        public BigDecimal approvedLoanAmount;
        @Schema(example = "1 February 2024", description = "Expected disbursement date")
        public String expectedDisbursementDate;
        @Schema(example = "0.0", description = "Discount amount (cannot exceed creation-time discount)")
        public BigDecimal discountAmount;
        @Schema(example = "15 January 2024", description = "Date of rejection")
        public String rejectedOnDate;
        @Schema(example = "Approval/Rejection/Disbursal Note")
        public String note;
        @Schema(example = "en_GB")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "28 June 2024", description = "Required for disburse - Actual Disbursement date")
        public String actualDisbursementDate;
        @Schema(example = "1000", description = "Disbursement amount; required for disburse. Cannot exceed approved principal.")
        public BigDecimal transactionAmount;
        @Schema(example = "1", description = "Optional disbursement transaction classification: id of a code value under system code working_capital_loan_disbursement_classification")
        public Long classificationId;
        @Schema(example = "ext-disburse-001", description = "External ID; optional for disburse")
        public String externalId;
        @Schema(example = "ext-discount-001", description = "External ID for the discount fee transaction created during disburse; optional. Only accepted when discountAmount is greater than 0. When omitted and auto-generation is enabled, a UUID is generated.")
        public String discountExternalId;
        @Schema(description = "Payment details (Account No, Cheque No, Routing Code, Receipt No, Bank code)")
        public PostWorkingCapitalLoansLoanIdDisbursementPaymentDetails paymentDetails;
    }

    @Schema(description = "Request for updating discount on a disbursed Working Capital Loan")
    public static final class PutWorkingCapitalLoansLoanIdDiscountRequest {

        private PutWorkingCapitalLoansLoanIdDiscountRequest() {}

        @Schema(example = "0.0", description = "Discount amount")
        public BigDecimal discountAmount;

        @Schema(example = "Discount update Note")
        public String note;

        @Schema(example = "en_GB")
        public String locale;

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
    }

    @Schema(description = "Working Capital Delinquency Collection Data")
    public static final class WorkingCapitalCollection {

        private WorkingCapitalCollection() {}

        @Schema(description = "Days the oldest unmet minimum-payment period is past due (measured from its toDate); 0 when not past due", example = "0")
        public Long pastDueDays;
        @Schema(description = "Number of days the loan has been delinquent, from the oldest active delinquency tag", example = "0")
        public Long delinquentDays;
        @Schema(description = "Date when the loan became delinquent", example = "[2024, 1, 15]")
        public LocalDate delinquentDate;
        @Schema(description = "Total delinquent amount", example = "1234.56")
        public BigDecimal delinquentAmount;
        @Schema(description = "Pause periods during which delinquency is not counted")
        public Collection<WorkingCapitalCollectionDelinquencyPausePeriod> delinquencyPausePeriods;
        @Schema(description = "Delinquency amounts grouped by age range (installment-level delinquency; Term-compatible name)")
        public Collection<WorkingCapitalCollectionRangeScheduleDelinquency> installmentLevelDelinquency;
        @Schema(description = "Delinquent principal amount", example = "1000.00")
        public BigDecimal delinquentPrincipal;

        @Schema(description = "Delinquency amount for a specific age range")
        public static final class WorkingCapitalCollectionRangeScheduleDelinquency {

            private WorkingCapitalCollectionRangeScheduleDelinquency() {}

            @Schema(description = "Delinquency range id", example = "1")
            public Long rangeId;
            @Schema(description = "Classification for the delinquency range", example = "Current")
            public String classification;
            @Schema(description = "Minimum age in days for the range", example = "1")
            public Integer minimumAgeDays;
            @Schema(description = "Maximum age in days for the range", example = "30")
            public Integer maximumAgeDays;
            @Schema(description = "Delinquent amount for this range", example = "123.45")
            public BigDecimal delinquentAmount;
        }

        @Schema(description = "Pause period during which delinquency tracking is paused")
        public static final class WorkingCapitalCollectionDelinquencyPausePeriod {

            private WorkingCapitalCollectionDelinquencyPausePeriod() {}

            @Schema(description = "Whether the pause period is active", example = "true")
            public boolean active;
            @Schema(description = "Pause period start date", example = "[2024, 1, 1]")
            public LocalDate pausePeriodStart;
            @Schema(description = "Pause period end date", example = "[2024, 1, 31]")
            public LocalDate pausePeriodEnd;
        }
    }

    @Schema(description = "GetWorkingCapitalLoanDelinquencyTagHistoryResponse")
    public static final class GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse {

        private GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "10")
        public Long loanId;
        public DelinquencyRangeData delinquencyRange;
        @Schema(example = "2013,1,2")
        public LocalDate addedOnDate;
        @Schema(example = "2013,2,20")
        public LocalDate liftedOnDate;
        @Schema(example = "10")
        public Long delinquentDays;
        @Schema(example = "1")
        public Long rangeId;
        @Schema(example = "2")
        public Integer periodNumber;
        @Schema(example = "123.45")
        public BigDecimal delinquentAmount;
    }

    @Schema(description = "Request for updating period payment rate on an active Working Capital Loan")
    public static final class PutWorkingCapitalLoansLoanIdRateRequest {

        private PutWorkingCapitalLoansLoanIdRateRequest() {}

        @Schema(example = "0.17", description = "New period payment rate")
        public BigDecimal periodPaymentRate;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "01 July 2022", description = "Date the new rate takes effect. Mandatory. May be backdated or set in the future, but not before the disbursement date.")
        public String effectiveDate;

        @Schema(example = "Rate change note")
        public String note;

        @Schema(example = "en_GB")
        public String locale;

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
    }

}
