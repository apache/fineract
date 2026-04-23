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
        /** Full list of disbursement details (for multi-disbursement support). */
        public List<GetDisbursementDetail> disbursementDetails;
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
        public GetWorkingCapitalLoansClient client;
        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long fundId;
        @Schema(example = "Fund 1")
        public String fundName;
        public WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanProductsResponse product;
        public GetWorkingCapitalLoansLoanIdStatus status;
        public GetWorkingCapitalLoansLoanIdTimeline timeline;
        @Schema(example = "[2024, 1, 15]")
        public LocalDate submittedOnDate;
        public LocalDate approvedOnDate;
        public LocalDate rejectedOnDate;
        public BigDecimal proposedPrincipal;
        public BigDecimal approvedPrincipal;

        public CurrencyData currency;
        @Schema(example = "1.0")
        public BigDecimal periodPaymentRate;
        @Schema(example = "30")
        public Integer repaymentEvery;
        public StringEnumOptionData repaymentFrequencyType;
        @Schema(example = "0.0")
        public BigDecimal discount;
        @Schema(description = "Working capital breach)")
        public WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanProductsResponse.GetWorkingCapitalLoanBreach breach;
        public WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanProductsResponse.GetDelinquencyBucket delinquencyBucket;
        @Schema(example = "3", description = "Number of grace days before delinquency tracking starts")
        public Integer delinquencyGraceDays;
        @Schema(description = "Delinquency start type: LOAN_CREATION or DISBURSEMENT")
        public StringEnumOptionData delinquencyStartType;
        @Schema(example = "[2024, 1, 14]", description = "Last closed business date (COB)")
        public LocalDate lastClosedBusinessDate;
        public List<GetPaymentAllocation> paymentAllocation;
        /** Full list of disbursement details (timeline uses the first). */
        public List<GetDisbursementDetail> disbursementDetails;
        /** Running balances (principal outstanding, total payment, etc.). */
        public GetBalance balance;
        @Schema(description = "Transaction history (e.g. disbursement).")
        public List<WorkingCapitalLoanTransactionsApiResourceSwagger.GetWorkingCapitalLoanTransactionIdResponse> transactions;
        @Schema(description = "Working Capital Delinquency Collection Data")
        public WorkingCapitalCollection collectionData;
    }

    @Schema(description = "Working capital loan running balances")
    public static final class GetBalance {

        private GetBalance() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "10000.00")
        public java.math.BigDecimal principalOutstanding;
        @Schema(example = "0.00")
        public java.math.BigDecimal totalPaidPrincipal;
        @Schema(example = "10500.00")
        public java.math.BigDecimal totalPayment;
        @Schema(example = "0.00")
        public java.math.BigDecimal realizedIncome;
        @Schema(example = "0.00")
        public java.math.BigDecimal unrealizedIncome;
        @Schema(example = "0.00")
        public java.math.BigDecimal overpaymentAmount;
    }

    @Schema(description = "Single disbursement detail (expected and actual)")
    public static final class GetDisbursementDetail {

        private GetDisbursementDetail() {}

        public Long id;
        public LocalDate expectedDisbursementDate;
        public BigDecimal expectedAmount;
        public LocalDate expectedMaturityDate;
        public LocalDate actualDisbursementDate;
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

    @Schema(description = "Loan transaction type enum data (same as basic loan)")
    public static final class LoanTransactionEnumData {

        private LoanTransactionEnumData() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "loanTransactionType.disbursement")
        public String code;
        @Schema(example = "Disbursement")
        public String value;
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
        public BigDecimal totalPayment;
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
        public Long delinquencyBucketId;
        @Schema(example = "3")
        public Integer delinquencyGraceDays;
        @Schema(example = "LOAN_CREATION", description = "Delinquency start type: LOAN_CREATION or DISBURSEMENT")
        public String delinquencyStartType;
        public List<PostPaymentAllocationRule> paymentAllocationRules;

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
        public BigDecimal totalPayment;
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
        public Long delinquencyBucketId;
        @Schema(example = "3")
        public Integer delinquencyGraceDays;
        @Schema(example = "LOAN_CREATION", description = "Delinquency start type: LOAN_CREATION or DISBURSEMENT")
        public String delinquencyStartType;
        public List<PostWorkingCapitalLoansRequest.PostPaymentAllocationRule> paymentAllocationRules;

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
        @Schema(example = "3")
        public Long resourceId;
        @Schema(example = "95174ff9-1a75-4d72-a413-6f9b1cb988b7")
        public String resourceExternalId;
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

        @Schema(description = "Working capital loan delinquency collection summary", example = "true")
        public Long delinquentDays;
        @Schema(description = "Date when the loan became delinquent", example = "[2024, 1, 15]")
        public LocalDate delinquentDate;
        @Schema(description = "Total delinquent amount", example = "1234.56")
        public BigDecimal delinquentAmount;
        @Schema(description = "Pause periods during which delinquency is not counted")
        public Collection<WorkingCapitalCollectionDelinquencyPausePeriod> delinquencyPausePeriods;
        @Schema(description = "Delinquency amounts grouped by age range")
        public Collection<WorkingCapitalCollectionRangeScheduleDelinquency> rangeLevelDelinquency;
        @Schema(description = "Delinquent principal amount", example = "1000.00")
        public BigDecimal delinquentPrincipal;
        @Schema(description = "Delinquent fee amount", example = "150.00")
        public BigDecimal delinquentFee;
        @Schema(description = "Delinquent penalty amount", example = "84.56")
        public BigDecimal delinquentPenalty;

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

}
