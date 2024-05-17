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
package org.apache.fineract.portfolio.loanaccount.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.delinquency.api.DelinquencyApiResourceSwagger.GetDelinquencyRangesResponse;

/**
 * Created by Chirag Gupta on 12/09/17.
 */
final class LoansApiResourceSwagger {

    private LoansApiResourceSwagger() {}

    @Schema(description = "GetLoansApprovalTemplateResponse")
    public static final class GetLoansApprovalTemplateResponse {

        private GetLoansApprovalTemplateResponse() {}

        @Schema(example = "[2012, 4, 3]")
        public LocalDate approvalDate;
        @Schema(example = "200.000000")
        public Double approvalAmount;
        @Schema(example = "200.000000")
        public Double netDisbursalAmount;
    }

    @Schema(description = "GetLoansTemplateResponse")
    public static final class GetLoansTemplateResponse {

        private GetLoansTemplateResponse() {}

        static final class GetLoansTemplateTimeline {

            private GetLoansTemplateTimeline() {}

            @Schema(example = "[2013, 3, 8]")
            public LocalDate expectedDisbursementDate;
        }

        static final class GetLoansTemplateProductOptions {

            private GetLoansTemplateProductOptions() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "Kampala Product (with cash accounting)")
            public String name;
        }

        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "Kampala first Client")
        public String clientName;
        @Schema(example = "2")
        public Long clientOfficeId;
        public GetLoansTemplateTimeline timeline;
        public Set<GetLoansTemplateProductOptions> productOptions;
    }

    @Schema(description = "GetLoansLoanIdResponse")
    public static final class GetLoansLoanIdResponse {

        private GetLoansLoanIdResponse() {}

        static final class GetLoansLoanIdStatus {

            private GetLoansLoanIdStatus() {}

            @Schema(example = "300")
            public Long id;
            @Schema(example = "loanStatusType.active")
            public String code;
            @Schema(example = "Active")
            public String description;
            @Schema(example = "false")
            public Boolean pendingApproval;
            @Schema(example = "false")
            public Boolean waitingForDisbursal;
            @Schema(example = "true")
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

        static final class GetLoansLoanIdLoanType {

            private GetLoansLoanIdLoanType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "loanType.individual")
            public String code;
            @Schema(example = "Individual")
            public String description;
        }

        static final class GetLoansLoanIdCurrency {

            private GetLoansLoanIdCurrency() {}

            @Schema(example = "UGX")
            public String code;
            @Schema(example = "Uganda Shilling")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "USh")
            public String displaySymbol;
            @Schema(example = "currency.UGX")
            public String nameCode;
            @Schema(example = "Uganda Shilling (USh)")
            public String displayLabel;
        }

        static final class GetLoansLoanIdTermPeriodFrequencyType {

            private GetLoansLoanIdTermPeriodFrequencyType() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "termFrequency.periodFrequencyType.months")
            public String code;
            @Schema(example = "Months")
            public String description;
        }

        static final class GetLoansLoanIdRepaymentFrequencyType {

            private GetLoansLoanIdRepaymentFrequencyType() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "repaymentFrequency.periodFrequencyType.months")
            public String code;
            @Schema(example = "Months")
            public String description;
        }

        static final class GetLoansLoanIdInterestRateFrequencyType {

            private GetLoansLoanIdInterestRateFrequencyType() {}

            @Schema(example = "3")
            public Long id;
            @Schema(example = "interestRateFrequency.periodFrequencyType.years")
            public String code;
            @Schema(example = "Per year")
            public String description;
        }

        static final class GetLoansLoanIdAmortizationType {

            private GetLoansLoanIdAmortizationType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "amortizationType.equal.installments")
            public String code;
            @Schema(example = "Equal installments")
            public String description;
        }

        static final class GetLoansLoanIdInterestType {

            private GetLoansLoanIdInterestType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "interestType.flat")
            public String code;
            @Schema(example = "Flat")
            public String description;
        }

        static final class GetLoansLoanIdInterestCalculationPeriodType {

            private GetLoansLoanIdInterestCalculationPeriodType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "interestCalculationPeriodType.same.as.repayment.period")
            public String code;
            @Schema(example = "Same as repayment period")
            public String description;
        }

        static final class GetLoansLoanIdTimeline {

            private GetLoansLoanIdTimeline() {}

            @Schema(example = "[2012, 4, 3]")
            public LocalDate submittedOnDate;
            @Schema(example = "admin")
            public String submittedByUsername;
            @Schema(example = "App")
            public String submittedByFirstname;
            @Schema(example = "Administrator")
            public String submittedByLastname;
            @Schema(example = "[2012, 4, 3]")
            public LocalDate approvedOnDate;
            @Schema(example = "admin")
            public String approvedByUsername;
            @Schema(example = "App")
            public String approvedByFirstname;
            @Schema(example = "Administrator")
            public String approvedByLastname;
            @Schema(example = "[2012, 4, 10]")
            public LocalDate expectedDisbursementDate;
            @Schema(example = "[2012, 4, 10]")
            public LocalDate actualDisbursementDate;
            @Schema(example = "admin")
            public String disbursedByUsername;
            @Schema(example = "App")
            public String disbursedByFirstname;
            @Schema(example = "Administrator")
            public String disbursedByLastname;
            @Schema(example = "[2012, 4, 10]")
            public LocalDate expectedMaturityDate;
            @Schema(example = "[2012, 4, 10]")
            public LocalDate actualMaturityDate;
            @Schema(example = "[2012, 4, 3]")
            public LocalDate closedOnDate;
            @Schema(example = "[2012, 4, 10]")
            public LocalDate chargedOffOnDate;
            @Schema(example = "admin")
            public String chargedOffByUsername;
            @Schema(example = "App")
            public String chargedOffByFirstname;
            @Schema(example = "Administrator")
            public String chargedOffByLastname;
        }

        static final class GetLoansLoanIdRepaymentSchedule {

            private GetLoansLoanIdRepaymentSchedule() {}

            public GetLoansLoanIdCurrency currency;
            @Schema(example = "30")
            public Long loanTermInDays;
            @Schema(example = "200.000000")
            public Double totalPrincipalDisbursed;
            @Schema(example = "200.00")
            public Double totalPrincipalExpected;
            @Schema(example = "200.00")
            public Double totalPrincipalPaid;
            @Schema(example = "0.00")
            public Double totalInterestCharged;
            @Schema(example = "0.00")
            public Double totalFeeChargesCharged;
            @Schema(example = "0.00")
            public Double totalPenaltyChargesCharged;
            @Schema(example = "0.00")
            public Double totalWaived;
            @Schema(example = "0.00")
            public Double totalWrittenOff;
            @Schema(example = "200.00")
            public Double totalRepaymentExpected;
            @Schema(example = "200.00")
            public Double totalPaidInAdvance;
            @Schema(example = "0.00")
            public Double totalPaidLate;
            @Schema(example = "0.00")
            public Double totalOutstanding;
            public List<GetLoansLoanIdRepaymentPeriod> periods;
        }

        static final class GetLoansLoanIdRepaymentPeriod {

            private GetLoansLoanIdRepaymentPeriod() {}

            @Schema(example = "1")
            public Integer period;
            @Schema(example = "[2012, 4, 3]")
            public LocalDate fromDate;
            @Schema(example = "[2012, 4, 3]")
            public LocalDate dueDate;
            @Schema(example = "[2012, 4, 3]")
            public LocalDate obligationsMetOnDate;
            @Schema(example = "true")
            public Boolean complete;
            @Schema(example = "30")
            public Long daysInPeriod;
            @Schema(example = "200.000000")
            public Double principalOriginalDue;
            @Schema(example = "200.000000")
            public Double principalDue;
            @Schema(example = "200.000000")
            public Double principalPaid;
            @Schema(example = "0.000000")
            public Double principalWrittenOff;
            @Schema(example = "20.000000")
            public Double principalOutstanding;
            @Schema(example = "20.000000")
            public Double principalLoanBalanceOutstanding;
            @Schema(example = "0.000000")
            public Double interestOriginalDue;
            @Schema(example = "0.000000")
            public Double interestDue;
            @Schema(example = "0.000000")
            public Double interestPaid;
            @Schema(example = "0.000000")
            public Double interestWaived;
            @Schema(example = "0.000000")
            public Double interestWrittenOff;
            @Schema(example = "0.000000")
            public Double interestOutstanding;
            @Schema(example = "0.000000")
            public Double feeChargesDue;
            @Schema(example = "20.000000")
            public Double feeChargesPaid;
            @Schema(example = "20.000000")
            public Double feeChargesWaived;
            @Schema(example = "20.000000")
            public Double feeChargesWrittenOff;
            @Schema(example = "20.000000")
            public Double feeChargesOutstanding;
            @Schema(example = "20.000000")
            public Double penaltyChargesDue;
            @Schema(example = "20.000000")
            public Double penaltyChargesPaid;
            @Schema(example = "20.000000")
            public Double penaltyChargesWaived;
            @Schema(example = "20.000000")
            public Double penaltyChargesWrittenOff;
            @Schema(example = "20.000000")
            public Double penaltyChargesOutstanding;
            @Schema(example = "20.000000")
            public Double totalOriginalDueForPeriod;
            @Schema(example = "20.000000")
            public Double totalDueForPeriod;
            @Schema(example = "20.000000")
            public Double totalPaidForPeriod;
            @Schema(example = "20.000000")
            public Double totalPaidInAdvanceForPeriod;
            @Schema(example = "20.000000")
            public Double totalPaidLateForPeriod;
            @Schema(example = "20.000000")
            public Double totalWaivedForPeriod;
            @Schema(example = "20.000000")
            public Double totalWrittenOffForPeriod;
            @Schema(example = "200.000000")
            public Double totalOutstandingForPeriod;
            @Schema(example = "20.000000")
            public Double totalActualCostOfLoanForPeriod;
            @Schema(example = "200.000000")
            public Double totalInstallmentAmountForPeriod;
            @Schema(example = "2.000000")
            public Double totalCredits;
            @Schema(example = "true")
            public Boolean downPaymentPeriod;
        }

        static final class GetLoansLoanIdDisbursementDetails {

            private GetLoansLoanIdDisbursementDetails() {}

            @Schema(example = "71")
            public Long id;
            @Schema(example = "[2022, 07, 01]")
            public LocalDate expectedDisbursementDate;
            @Schema(example = "[2022, 07, 01]")
            public LocalDate actualDisbursementDate;
            @Schema(example = "22000.000000")
            public Double principal;
            @Schema(example = "22000.000000")
            public Double netDisbursalAmount;
            @Schema(example = "1")
            public String loanChargeId;
            @Schema(example = "22000.000000")
            public Double chargeAmount;
            @Schema(example = "22000.000000")
            public Double waivedChargeAmount;
            @Schema(example = "dd MMMM yyyy")
            public String dateFormat;
            @Schema(example = "de_DE")
            public String locale;
            @Schema(example = "some note")
            public String note;
        }

        static final class GetLoansLoanIdSummary {

            private GetLoansLoanIdSummary() {}

            static final class GetLoansLoanIdEmiVariations {

                private GetLoansLoanIdEmiVariations() {}
            }

            static final class GetLoansLoanIdLinkedAccount {

                private GetLoansLoanIdLinkedAccount() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "000000001")
                public String accountNo;
            }

            static final class GetLoansLoanIdOverdueCharges {

                private GetLoansLoanIdOverdueCharges() {}

                static final class GetLoansLoanIdChargeTimeType {

                    private GetLoansLoanIdChargeTimeType() {}

                    @Schema(example = "9")
                    public Long id;
                    @Schema(example = "chargeTimeType.overdueInstallment")
                    public String code;
                    @Schema(example = "overdue fees")
                    public String description;
                }

                static final class GetLoansLoanIdChargeCalculationType {

                    private GetLoansLoanIdChargeCalculationType() {}

                    @Schema(example = "2")
                    public Long id;
                    @Schema(example = "chargeCalculationType.percent.of.amount")
                    public String code;
                    @Schema(example = "% Amount")
                    public String description;
                }

                static final class GetLoansLoanIdChargePaymentMode {

                    private GetLoansLoanIdChargePaymentMode() {}

                    @Schema(example = "0")
                    public Long id;
                    @Schema(example = "chargepaymentmode.regular")
                    public String code;
                    @Schema(example = "Regular")
                    public String description;
                }

                static final class GetLoansLoanIdFeeFrequency {

                    private GetLoansLoanIdFeeFrequency() {}

                    @Schema(example = "1")
                    public Long id;
                    @Schema(example = "feeFrequencyperiodFrequencyType.weeks")
                    public String code;
                    @Schema(example = "Weeks")
                    public String description;
                }

                @Schema(example = "20")
                public Long id;
                @Schema(example = "overdraft penality")
                public String name;
                @Schema(example = "true")
                public Boolean active;
                @Schema(example = "true")
                public Boolean penalty;
                public LoanChargesApiResourceSwagger.GetLoansLoanIdChargesChargeIdResponse.GetLoanChargeCurrency currency;
                @Schema(example = "3.000000")
                public Float amount;
                public GetLoansLoanIdChargeTimeType chargeTimeType;
                public LoanChargesApiResourceSwagger.GetLoansLoanIdChargesTemplateResponse.GetLoanChargeTemplateChargeOptions.GetLoanChargeTemplateChargeAppliesTo chargeAppliesTo;
                public GetLoansLoanIdChargeCalculationType chargeCalculationType;
                public GetLoansLoanIdChargePaymentMode chargePaymentMode;
                @Schema(example = "2")
                public Integer feeInterval;
                public GetLoansLoanIdFeeFrequency feeFrequency;
            }

            public GetLoansLoanIdCurrency currency;
            @Schema(example = "1000000.000000")
            public Double principalDisbursed;
            @Schema(example = "0.000000")
            public Double principalPaid;
            @Schema(example = "0.00")
            public Double principalAdjustments;
            @Schema(example = "0.000000")
            public Double principalWrittenOff;
            @Schema(example = "1000000.000000")
            public Double principalOutstanding;
            @Schema(example = "833333.300000")
            public Double principalOverdue;
            @Schema(example = "240000.000000")
            public Double interestCharged;
            @Schema(example = "0.000000")
            public Double interestPaid;
            @Schema(example = "0.000000")
            public Double interestWaived;
            @Schema(example = "0.000000")
            public Double interestWrittenOff;
            @Schema(example = "240000.000000")
            public Double interestOutstanding;
            @Schema(example = "200000.000000")
            public Double interestOverdue;
            @Schema(example = "0.00")
            public Double feeAdjustments;
            @Schema(example = "18000.000000")
            public Double feeChargesCharged;
            @Schema(example = "0.000000")
            public Double feeChargesDueAtDisbursementCharged;
            @Schema(example = "0.000000")
            public Double feeChargesPaid;
            @Schema(example = "0.000000")
            public Double feeChargesWaived;
            @Schema(example = "0.000000")
            public Double feeChargesWrittenOff;
            @Schema(example = "18000.000000")
            public Double feeChargesOutstanding;
            @Schema(example = "15000.000000")
            public Double feeChargesOverdue;
            @Schema(example = "0.00")
            public Double penaltyAdjustments;
            @Schema(example = "0.000000")
            public Double penaltyChargesCharged;
            @Schema(example = "0.000000")
            public Double penaltyChargesPaid;
            @Schema(example = "0.000000")
            public Double penaltyChargesWaived;
            @Schema(example = "0.000000")
            public Double penaltyChargesWrittenOff;
            @Schema(example = "0.000000")
            public Double penaltyChargesOutstanding;
            @Schema(example = "0.000000")
            public Double penaltyChargesOverdue;
            @Schema(example = "1258000.000000")
            public Double totalExpectedRepayment;
            @Schema(example = "0.000000")
            public Double totalRepayment;
            @Schema(example = "258000.000000")
            public Double totalExpectedCostOfLoan;
            @Schema(example = "0.000000")
            public Double totalCostOfLoan;
            @Schema(example = "0.000000")
            public Double totalWaived;
            @Schema(example = "0.000000")
            public Double totalWrittenOff;
            @Schema(example = "1258000.000000")
            public Double totalOutstanding;
            @Schema(example = "1048333.30000")
            public Double totalOverdue;
            @Schema(example = "2456.30000")
            public Double totalRecovered;
            @Schema(example = "[2012, 5, 10]")
            public LocalDate overdueSinceDate;
            @Schema(example = "1")
            public Long writeoffReasonId;
            @Schema(example = "reason")
            public String writeoffReason;
            public GetLoansLoanIdLinkedAccount linkedAccount;
            public Set<GetLoansLoanIdDisbursementDetails> disbursementDetails;
            @Schema(example = "1100.000000")
            public Double fixedEmiAmount;
            @Schema(example = "35000.000000")
            public Double maxOutstandingLoanBalance;
            @Schema(example = "false")
            public Boolean canDisburse;
            public Set<GetLoansLoanIdEmiVariations> emiAmountVariations;
            @Schema(example = "true")
            public Boolean inArrears;
            @Schema(example = "false")
            public Boolean isNPA;
            @Schema(example = "0.000000")
            public Double totalMerchantRefund;
            @Schema(example = "0.000000")
            public Double totalMerchantRefundReversed;
            @Schema(example = "0.000000")
            public Double totalPayoutRefund;
            @Schema(example = "0.000000")
            public Double totalPayoutRefundReversed;
            @Schema(example = "0.000000")
            public Double totalGoodwillCredit;
            @Schema(example = "0.000000")
            public Double totalGoodwillCreditReversed;
            @Schema(example = "0.000000")
            public Double totalChargeAdjustment;
            @Schema(example = "0.000000")
            public Double totalChargeAdjustmentReversed;
            @Schema(example = "0.000000")
            public Double totalChargeback;
            @Schema(example = "0.000000")
            public Double totalCreditBalanceRefund;
            @Schema(example = "0.000000")
            public Double totalCreditBalanceRefundReversed;
            @Schema(example = "0.000000")
            public Double totalRepaymentTransaction;
            @Schema(example = "0.000000")
            public Double totalRepaymentTransactionReversed;
            public Set<GetLoansLoanIdOverdueCharges> overdueCharges;
            @Schema(example = "1")
            public Long chargeOffReasonId;
            @Schema(example = "reason")
            public String chargeOffReason;
            @Schema(example = "800.000000")
            public Double balloonRepaymentAmount;
        }

        static final class GetLoansLoanIdPaymentType {

            private GetLoansLoanIdPaymentType() {}

            @Schema(example = "11")
            public Long id;
            @Schema(example = "Cash")
            public String name;
            @Schema(example = "Cash Payment")
            public String description;
            @Schema(example = "true")
            public Boolean isCashPayment;
            @Schema(example = "0")
            public Long position;
        }

        static final class GetLoansLoanIdTransactions {

            private GetLoansLoanIdTransactions() {}

            static final class GetLoansLoanIdLoanTransactionEnumData {

                private GetLoansLoanIdLoanTransactionEnumData() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "loanTransactionType.repayment")
                public String code;
                @Schema(example = "2")
                public String value;

                @Schema(example = "false")
                public boolean disbursement;
                @Schema(example = "false")
                public boolean repaymentAtDisbursement;
                @Schema(example = "true")
                public boolean repayment;
                @Schema(example = "false")
                public boolean merchantIssuedRefund;
                @Schema(example = "false")
                public boolean payoutRefund;
                @Schema(example = "false")
                public boolean goodwillCredit;
                @Schema(example = "false")
                public boolean contra;
                @Schema(example = "false")
                public boolean waiveInterest;
                @Schema(example = "false")
                public boolean waiveCharges;
                @Schema(example = "false")
                public boolean accrual;
                @Schema(example = "false")
                public boolean writeOff;
                @Schema(example = "false")
                public boolean recoveryRepayment;
                @Schema(example = "false")
                public boolean initiateTransfer;
                @Schema(example = "false")
                public boolean approveTransfer;
                @Schema(example = "false")
                public boolean withdrawTransfer;
                @Schema(example = "false")
                public boolean rejectTransfer;
                @Schema(example = "false")
                public boolean chargePayment;
                @Schema(example = "false")
                public boolean refund;
                @Schema(example = "false")
                public boolean refundForActiveLoans;
                @Schema(example = "false")
                public boolean creditBalanceRefund;
                @Schema(example = "false")
                public boolean chargeAdjustment;
                @Schema(example = "false")
                public boolean chargeoff;
            }

            static final class GetLoansLoanIdPaymentDetailData {

                private GetLoansLoanIdPaymentDetailData() {}

                @Schema(example = "62")
                public Long id;
                public GetLoansLoanIdPaymentType paymentType;
                @Schema(example = "acc123")
                public String accountNumber;
                @Schema(example = "che123")
                public String checkNumber;
                @Schema(example = "rou123")
                public String routingCode;
                @Schema(example = "rec123")
                public String receiptNumber;
                @Schema(example = "ban123")
                public String bankNumber;
            }

            static final class GetLoansLoanIdLoanChargePaidByData {

                private GetLoansLoanIdLoanChargePaidByData() {}

                @Schema(example = "11")
                public Long id;
                @Schema(example = "100.000000")
                public Double amount;
                @Schema(example = "9679")
                public Integer installmentNumber;
                @Schema(example = "1")
                public Long chargeId;
                @Schema(example = "636")
                public Long transactionId;
                @Schema(example = "name")
                public String name;
            }

            static final class GetLoansLoanIdCodeValueData {

                private GetLoansLoanIdCodeValueData() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "code name")
                public String name;
                @Schema(example = "0")
                public Integer position;
                @Schema(example = "code description")
                public String description;
                @Schema(example = "true")
                public Boolean active;
                @Schema(example = "false")
                public Boolean mandatory;
            }

            static final class GetLoansLoanIdLoanRepaymentScheduleInstallmentData {

                private GetLoansLoanIdLoanRepaymentScheduleInstallmentData() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "11")
                public Integer installmentId;
                @Schema(example = "[2022, 07, 01]")
                public LocalDate date;
                @Schema(example = "100.000000")
                public BigDecimal amount;
            }

            static final class GetLoansLoanIdLoanTransactionRelation {

                private GetLoansLoanIdLoanTransactionRelation() {}

                @Schema(example = "1")
                public Long fromLoanTransaction;
                @Schema(example = "10")
                public Long toLoanTransaction;
                @Schema(example = "10")
                public Long toLoanCharge;
                @Schema(example = "CHARGEBACK")
                public String relationType;
                @Schema(example = "100.00")
                public Double amount;
                @Schema(example = "Repayment Adjustment Chargeback")
                public String paymentType;

            }

            @Schema(example = "1")
            public Long id;
            @Schema(example = "1")
            public Long officeId;
            @Schema(example = "Head Office")
            public String officeName;
            @Schema(description = "Transaction type")
            public GetLoansLoanIdLoanTransactionEnumData type;
            @Schema(example = "[2022, 07, 01]")
            public LocalDate date;
            @Schema(description = "Currency")
            public GetLoansLoanIdCurrency currency;
            @Schema(description = "Payment detail")
            public GetLoansLoanIdPaymentDetailData paymentDetailData;
            @Schema(example = "100.000000")
            public Double amount;
            @Schema(example = "100.000000")
            public Double netDisbursalAmount;
            @Schema(example = "100.000000")
            public Double principalPortion;
            @Schema(example = "100.000000")
            public Double interestPortion;
            @Schema(example = "100.000000")
            public Double feeChargesPortion;
            @Schema(example = "100.000000")
            public Double penaltyChargesPortion;
            @Schema(example = "100.000000")
            public Double overpaymentPortion;
            @Schema(example = "100.000000")
            public Double unrecognizedIncomePortion;
            @Schema(example = "3")
            public String externalId;
            @Schema(example = "100.000000")
            public Double fixedEmiAmount;
            @Schema(example = "100.000000")
            public Double outstandingLoanBalance;
            @Schema(example = "[2022, 07, 01]")
            public LocalDate submittedOnDate;
            public boolean manuallyReversed;
            @Schema(example = "[2022, 07, 01]")
            public LocalDate possibleNextRepaymentDate;
            @Schema(description = "List of GetLoansLoanIdLoanChargePaidByData")
            public List<GetLoansLoanIdLoanChargePaidByData> loanChargePaidByList;
            @Schema(description = "List of GetLoansLoanIdPaymentType")
            public List<GetLoansLoanIdPaymentType> paymentTypeOptions;
            @Schema(description = "List of GetLoansLoanIdCodeValueData")
            public List<GetLoansLoanIdCodeValueData> writeOffReasonOptions;
            @Schema(example = "4")
            public Integer numberOfRepayments;
            @Schema(example = "dd MMMM yyyy")
            public String dateFormat;
            @Schema(example = "de_DE")
            public String locale;
            @Schema(example = "100.000000")
            public Double transactionAmount;
            @Schema(example = "[2022, 07, 01]")
            public LocalDate transactionDate;
            @Schema(example = "101")
            public Integer paymentTypeId;
            @Schema(example = "acct123")
            public String accountNumber;
            @Schema(example = "10001")
            public Integer checkNumber;
            @Schema(example = "6337")
            public Integer routingCode;
            @Schema(example = "67863")
            public Integer receiptNumber;
            @Schema(example = "34645568")
            public Integer bankNumber;
            @Schema(example = "7327")
            public Long accountId;
            @Schema(example = "repayment")
            public String transactionType;
            @Schema(description = "List of GetLoansLoanIdLoanRepaymentScheduleInstallmentData")
            public List<GetLoansLoanIdLoanRepaymentScheduleInstallmentData> loanRepaymentScheduleInstallments;
            @Schema(example = "95174ff9-1a75-4d72-a413-6f9b1cb988b7")
            public String reversalExternalId;
            @Schema(example = "[2022, 9, 19]")
            public LocalDate reversedOnDate;
            @Schema(description = "List of GetLoansLoanIdLoanTransactionRelationData")
            public Set<GetLoansLoanIdLoanTransactionRelation> transactionRelations;
        }

        static final class GetLoansLoanIdLoanChargeData {

            private GetLoansLoanIdLoanChargeData() {}

            static final class GetLoansLoanIdEnumOptionData {

                private GetLoansLoanIdEnumOptionData() {}

                @Schema(example = "2")
                public Long id;
                @Schema(example = "chargeTimeType.specifiedDueDate")
                public String code;
                @Schema(example = "Specified due date")
                public String value;
            }

            static final class GetLoansLoanIdLoanInstallmentChargeData {

                private GetLoansLoanIdLoanInstallmentChargeData() {}

                @Schema(example = "2")
                public Integer installmentNumber;
                @Schema(example = "[2022, 07, 01]")
                public LocalDate dueDate;
                @Schema(example = "13.560000")
                public Double amount;
                @Schema(example = "13.560000")
                public Double amountOutstanding;
                @Schema(example = "13.560000")
                public Double amountWaived;
                @Schema(example = "false")
                public boolean paid;
                @Schema(example = "false")
                public boolean waived;
                @Schema(example = "13.560000")
                public Double amountAccrued;
                @Schema(example = "13.560000")
                public Double amountUnrecognized;
            }

            @Schema(example = "3")
            public Long id;
            @Schema(example = "5")
            public Long chargeId;
            @Schema(example = "snooze fee")
            public String name;
            @Schema(description = "Enum option data")
            public GetLoansLoanIdEnumOptionData chargeTimeType;
            @Schema(example = "[2022, 07, 01]")
            public LocalDate dueDate;
            @Schema(description = "Enum option data")
            public GetLoansLoanIdEnumOptionData chargeCalculationType;
            @Schema(example = "3.400000")
            public Double percentage;
            @Schema(example = "13.560000")
            public Double amountPercentageAppliedTo;
            @Schema(description = "currency")
            public GetLoansLoanIdCurrency currency;
            @Schema(example = "102.000000")
            public Double amount;
            @Schema(example = "12.000000")
            public Double amountPaid;
            @Schema(example = "14.000000")
            public Double amountWaived;
            @Schema(example = "102.000000")
            public Double amountWrittenOff;
            @Schema(example = "102.000000")
            public Double amountOutstanding;
            @Schema(example = "102.000000")
            public Double amountOrPercentage;
            @Schema(example = "false")
            public boolean penalty;
            @Schema(description = "Enum option data")
            public GetLoansLoanIdEnumOptionData chargePaymentMode;
            @Schema(example = "false")
            public boolean paid;
            @Schema(example = "false")
            public boolean waived;
            @Schema(example = "false")
            public boolean chargePayable;
            @Schema(example = "3")
            public Long loanId;
            @Schema(example = "30.000000")
            public Double minCap;
            @Schema(example = "30.000000")
            public Double maxCap;
            @Schema(description = "List of GetLoansLoanIdLoanInstallmentChargeData")
            public List<GetLoansLoanIdLoanInstallmentChargeData> installmentChargeData;
            @Schema(example = "30.000000")
            private Double amountAccrued;
            @Schema(example = "30.000000")
            private Double amountUnrecognized;
            @Schema(example = "3ert3453")
            private String externalId;
        }

        static final class GetLoansLoanIdDelinquencySummary {

            private GetLoansLoanIdDelinquencySummary() {}

            @Schema(example = "100.000000")
            public Double availableDisbursementAmount;
            @Schema(example = "12")
            public Integer pastDueDays;
            @Schema(example = "[2022, 07, 01]")
            public LocalDate nextPaymentDueDate;
            @Schema(example = "4")
            public Integer delinquentDays;
            @Schema(example = "[2022, 07, 01]")
            public LocalDate delinquentDate;
            @Schema(example = "100.000000")
            public Double delinquentAmount;
            @Schema(example = "[2022, 07, 01]")
            public LocalDate lastPaymentDate;
            @Schema(example = "100.000000")
            public Double lastPaymentAmount;

            @Schema(example = "[2022, 07, 01]")
            public LocalDate lastRepaymentDate;
            @Schema(example = "100.000000")
            public Double lastRepaymentAmount;

            @Schema(description = "List of GetLoansLoanIdDelinquencyPausePeriod")
            public List<GetLoansLoanIdDelinquencyPausePeriod> delinquencyPausePeriods;

            @Schema(description = "List of GetLoansLoanIdLoanInstallmentLevelDelinquency")
            public List<GetLoansLoanIdLoanInstallmentLevelDelinquency> installmentLevelDelinquency;

        }

        static final class GetLoansLoanIdDelinquencyPausePeriod {

            @Schema(example = "true")
            public Boolean active;

            @Schema(example = "[2022, 07, 05]")
            public LocalDate pausePeriodStart;

            @Schema(example = "[2022, 07, 10]")
            public LocalDate pausePeriodEnd;

        }

        static final class GetLoansLoanIdLoanInstallmentLevelDelinquency {

            private GetLoansLoanIdLoanInstallmentLevelDelinquency() {}

            @Schema(example = "112")
            public Long rangeId;

            @Schema(example = "Delinquency Range 3 to 5 days")
            public String classification;

            @Schema(example = "3")
            public Integer minimumAgeDays;

            @Schema(example = "5")
            public Integer maximumAgeDays;

            @Schema(example = "250.0")
            public BigDecimal delinquentAmount;

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "95174ff9-1a75-4d72-a413-6f9b1cb988b7")
        public String externalId;
        @Schema(example = "000000001")
        public String accountNo;
        public GetLoansLoanIdStatus status;
        @Schema(example = "false")
        public boolean disallowExpectedDisbursements;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "5e77989e-aa11-11bc-b109-0242ac120004")
        public String clientExternalId;
        @Schema(example = "Kampala first Client")
        public String clientName;
        @Schema(example = "2")
        public Long clientOfficeId;
        @Schema(example = "1")
        public Long loanProductId;
        @Schema(example = "Kampala Product (with cash accounting)")
        public String loanProductName;
        @Schema(example = "Typical Kampala loan product with cash accounting enabled for testing.")
        public String loanProductDescription;
        @Schema(example = "22")
        public Long loanPurposeId;
        @Schema(example = "option.HousingImprovement")
        public String loanPurposeName;
        @Schema(example = "2")
        public Long loanOfficerId;
        @Schema(example = "LoanOfficer, Kampala")
        public String loanOfficerName;
        public GetLoansLoanIdLoanType loanType;
        public GetLoansLoanIdCurrency currency;
        @Schema(example = "1000000.00")
        public BigDecimal principal;
        @Schema(example = "1000.000000")
        public Double approvedPrincipal;
        @Schema(example = "1001.000000")
        public Double proposedPrincipal;
        @Schema(example = "200.000000")
        public Double netDisbursalAmount;
        @Schema(example = "12")
        public Integer termFrequency;
        public GetLoansLoanIdTermPeriodFrequencyType termPeriodFrequencyType;
        @Schema(example = "12")
        public Integer numberOfRepayments;
        @Schema(example = "1")
        public Integer repaymentEvery;
        public GetLoansLoanIdRepaymentFrequencyType repaymentFrequencyType;
        @Schema(example = "1")
        public Integer fixedLength;
        @Schema(example = "24")
        public BigDecimal interestRatePerPeriod;
        public GetLoansLoanIdInterestRateFrequencyType interestRateFrequencyType;
        @Schema(example = "24")
        public Integer annualInterestRate;
        @Schema(example = "false")
        public Boolean isFloatingInterestRate;
        public GetLoansLoanIdAmortizationType amortizationType;
        @Schema(example = "5.5")
        public BigDecimal fixedPrincipalPercentagePerInstallment;
        public GetLoansLoanIdInterestType interestType;
        public GetLoansLoanIdInterestCalculationPeriodType interestCalculationPeriodType;
        @Schema(example = "mifos-standard-strategy")
        public String transactionProcessingStrategyCode;
        public GetLoansLoanIdTimeline timeline;
        public GetLoansLoanIdSummary summary;
        public GetLoansLoanIdRepaymentSchedule repaymentSchedule;
        @Schema(description = "Set of GetLoansLoanIdTransactions")
        public List<GetLoansLoanIdTransactions> transactions;
        @Schema(description = "Set of GetLoansLoanIdDisbursementDetails")
        public Set<GetLoansLoanIdDisbursementDetails> disbursementDetails;
        @Schema(description = "Delinquent data")
        public GetLoansLoanIdDelinquencySummary delinquent;
        @Schema(description = "Set of charges")
        public List<GetLoansLoanIdLoanChargeData> charges;
        public GetDelinquencyRangesResponse delinquencyRange;
        @Schema(example = "false")
        public Boolean fraud;
        @Schema(example = "false")
        public Boolean enableInstallmentLevelDelinquency;
        @Schema(example = "250.000000")
        public Double totalOverpaid;
        public LocalDate lastClosedBusinessDate;
        @Schema(example = "[2013, 11, 1]")
        public LocalDate overpaidOnDate;
        @Schema(example = "false")
        public Boolean chargedOff;
        @Schema(example = "3")
        public Integer inArrearsTolerance;
        @Schema(example = "false")
        public Boolean enableDownPayment;
        @Schema(example = "0.000000")
        public BigDecimal disbursedAmountPercentageForDownPayment;
        @Schema(example = "false")
        public Boolean enableAutoRepaymentForDownPayment;
        @Schema(example = "CUMULATIVE")
        public EnumOptionData loanScheduleType;
        @Schema(example = "HORIZONTAL")
        public EnumOptionData loanScheduleProcessingType;
    }

    @Schema(description = "GetLoansResponse")
    public static final class GetLoansResponse {

        private GetLoansResponse() {}

        @Schema(example = "1")
        public Integer totalFilteredRecords;
        public Set<GetLoansLoanIdResponse> pageItems;
    }

    @Schema(description = "PostLoansRequest")
    public static final class PostLoansRequest {

        static final class PostLoansDisbursementData {

            private PostLoansDisbursementData() {}

            @Schema(example = "[2013, 11, 1]")
            public LocalDate expectedDisbursementDate;
            @Schema(example = "1000.00")
            public Double principal;
        }

        private PostLoansRequest() {}

        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en_GB")
        public String locale;
        @Schema(example = "1")
        public Long productId;
        @Schema(example = "1000.00")
        public BigDecimal principal;
        @Schema(example = "12")
        public Integer loanTermFrequency;
        @Schema(example = "2")
        public Integer loanTermFrequencyType;
        @Schema(example = "12")
        public Integer numberOfRepayments;
        @Schema(example = "1")
        public Integer repaymentEvery;
        @Schema(example = "2")
        public Integer repaymentFrequencyType;
        @Schema(example = "1")
        public Integer fixedLength;
        @Schema(example = "2")
        public BigDecimal interestRatePerPeriod;
        @Schema(example = "1")
        public Integer amortizationType;
        @Schema(example = "5.5")
        public BigDecimal fixedPrincipalPercentagePerInstallment;
        @Schema(example = "0")
        public Integer interestType;
        @Schema(example = "1")
        public Integer interestCalculationPeriodType;
        @Schema(example = "20 September 2011")
        public String expectedDisbursementDate;
        @Schema(example = "mifos-standard-strategy")
        public String transactionProcessingStrategyCode;
        @Schema(example = "360", allowableValues = "1, 360, 364, 36")
        public Integer daysInYearType;
        @Schema(example = "individual")
        public String loanType;
        @Schema(example = "20 September 2011")
        public String submittedOnDate;
        @Schema(example = "786444UUUYYH7")
        public String externalId;
        @Schema(description = "List of PostLoansDisbursementData")
        public List<PostLoansDisbursementData> disbursementData;
        @Schema(description = "Maximum allowed outstanding balance")
        public BigDecimal maxOutstandingLoanBalance;
        @Schema(example = "[2011, 10, 20]")
        public LocalDate repaymentsStartingFromDate;
        @Schema(example = "1")
        public Integer graceOnInterestCharged;
        @Schema(example = "1")
        public Integer graceOnPrincipalPayment;
        @Schema(example = "1")
        public Integer graceOnInterestPayment;
        @Schema(example = "1")
        public Integer graceOnArrearsAgeing;
        @Schema(example = "HORIZONTAL")
        public String loanScheduleProcessingType;
        @Schema(example = "false")
        public Boolean enableInstallmentLevelDelinquency;
        @Schema(example = "800.000000")
        public Double balloonRepaymentAmount;
    }

    @Schema(description = "PostLoansResponse")
    public static final class PostLoansResponse {

        private PostLoansResponse() {}

        static final class PostLoansRepaymentSchedulePeriods {

            private PostLoansRepaymentSchedulePeriods() {}

            @Schema(example = "0")
            public Integer period;
            @Schema(example = "[2011, 9, 20]")
            public LocalDate dueDate;
            @Schema(example = "100000")
            public Long principalDisbursed;
            @Schema(example = "100000")
            public Long principalLoanBalanceOutstanding;
            @Schema(example = "0")
            public Long feeChargesDue;
            @Schema(example = "0")
            public Long feeChargesOutstanding;
            @Schema(example = "0")
            public Long totalOriginalDueForPeriod;
            @Schema(example = "0")
            public Long totalDueForPeriod;
            @Schema(example = "0")
            public Long totalOutstandingForPeriod;
            @Schema(example = "0")
            public Long totalOverdue;
            @Schema(example = "0")
            public Long totalActualCostOfLoanForPeriod;
        }

        public GetLoansLoanIdResponse.GetLoansLoanIdCurrency currency;
        @Schema(example = "366")
        public Integer loanTermInDays;
        @Schema(example = "100000")
        public Long totalPrincipalDisbursed;
        @Schema(example = "100000")
        public Long totalPrincipalExpected;
        @Schema(example = "0")
        public Long totalPrincipalPaid;
        @Schema(example = "13471.52")
        public Double totalInterestCharged;
        @Schema(example = "0")
        public Long totalFeeChargesCharged;
        @Schema(example = "0")
        public Long totalPenaltyChargesCharged;
        @Schema(example = "0")
        public Long totalWaived;
        @Schema(example = "0")
        public Long totalWrittenOff;
        @Schema(example = "113471.52")
        public Double totalRepaymentExpected;
        @Schema(example = "0")
        public Long totalRepayment;
        @Schema(example = "0")
        public Long totalOutstanding;
        public Set<PostLoansRepaymentSchedulePeriods> periods;
        @Schema(example = "2")
        public Long officeId;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "1")
        public Long loanId;
        @Schema(example = "1")
        public Long resourceId;
        @Schema(example = "95174ff9-1a75-4d72-a413-6f9b1cb988b7")
        public String resourceExternalId;
    }

    @Schema(description = "PutLoansLoanIdRequest")
    public static final class PutLoansLoanIdRequest {

        private PutLoansLoanIdRequest() {}

        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema
        public String submittedOnDate;
        @Schema(example = "1")
        public Long productId;
        @Schema(example = "5000")
        public Long principal;
        @Schema(example = "10")
        public Integer loanTermFrequency;
        @Schema(example = "0")
        public Integer loanTermFrequencyType;
        @Schema(example = "10")
        public Integer numberOfRepayments;
        @Schema(example = "1")
        public Integer repaymentEvery;
        @Schema(example = "0")
        public Integer repaymentFrequencyType;
        @Schema(example = "1")
        public Integer fixedLength;
        @Schema(example = "2")
        public BigDecimal interestRatePerPeriod;
        @Schema(example = "0")
        public Integer interestType;
        @Schema(example = "0")
        public Integer interestCalculationPeriodType;
        @Schema(example = "1")
        public Integer amortizationType;
        @Schema(example = "5.5")
        public BigDecimal fixedPrincipalPercentagePerInstallment;
        @Schema(example = "04 March 2014")
        public String expectedDisbursementDate;
        @Schema(example = "mifos-standard-strategy")
        public String transactionProcessingStrategyCode;
        @Schema(example = "1")
        public Long linkAccountId;
        @Schema(example = "true")
        public Boolean createStandingInstructionAtDisbursement;
        @Schema(example = "1")
        public Integer repaymentFrequencyNthDayType;
        @Schema(example = "1")
        public Integer repaymentFrequencyDayOfWeekType;
        @Schema
        public String repaymentsStartingFromDate;
        @Schema
        public String interestChargedFromDate;
        @Schema(example = "true")
        public Boolean isEqualAmortization;
        @Schema(example = "true")
        public Boolean fraud;
        @Schema(example = "1")
        public Integer graceOnArrearsAgeing;
        @Schema(example = "1")
        public Long loanIdToClose;
        @Schema(example = "true")
        public Boolean isTopup;
        @Schema(example = "1")
        public Long maxOutstandingLoanBalance;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "individual")
        public String loanType;
        public List<PutLoansLoanIdChanges> charges;
        public List<PutLoansLoanIdCollateral> collateral;
        public List<PutLoansLoanIdDisbursementData> disbursementData;
        @Schema(example = "HORIZONTAL")
        public String loanScheduleProcessingType;
        @Schema(example = "false")
        public Boolean enableInstallmentLevelDelinquency;

        static final class PutLoansLoanIdChanges {

            private PutLoansLoanIdChanges() {}

            @Schema(example = "dd MMMM yyyy")
            public String dateFormat;
            @Schema(example = "en")
            public String locale;
            @Schema(example = "1")
            public Long id;
            @Schema(example = "1")
            public Long chargeId;
            @Schema(example = "1")
            public BigDecimal amount;
            @Schema
            public String dueDate;
            @Schema(example = "1")
            public Integer chargeTimeType;
            @Schema(example = "1")
            public Integer chargeCalculationType;
            @Schema(example = "1")
            public Integer chargePaymentMode;
        }

        static final class PutLoansLoanIdCollateral {

            private PutLoansLoanIdCollateral() {}

            @Schema(example = "1")
            public Long clientCollateralId;
            @Schema(example = "1")
            public BigDecimal quantity;
        }

        static final class PutLoansLoanIdDisbursementData {

            private PutLoansLoanIdDisbursementData() {}

            @Schema(example = "dd MMMM yyyy")
            public String dateFormat;
            @Schema(example = "en")
            public String locale;
            @Schema
            public String expectedDisbursementDate;
            @Schema(example = "true")
            public Boolean isEqualAmortization;
            @Schema(example = "1")
            public BigDecimal principal;
            @Schema(example = "1")
            public BigDecimal netDisbursalAmount;
            @Schema(example = "1")
            public Integer interestType;
        }
    }

    @Schema(description = "PutLoansLoanIdResponse")
    public static final class PutLoansLoanIdResponse {

        private PutLoansLoanIdResponse() {}

        static final class PutLoansLoanIdChanges {

            private PutLoansLoanIdChanges() {}

            @Schema(example = "5000")
            public Long principal;
            @Schema(example = "en")
            public String locale;
            @Schema(example = "false")
            public Boolean fraud;
        }

        @Schema(example = "2")
        public Long officeId;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "1")
        public Long loanId;
        @Schema(example = "1")
        public Long resourceId;
        @Schema(example = "95174ff9-1a75-4d72-a413-6f9b1cb988b7")
        public String resourceExternalId;
        public PutLoansLoanIdChanges changes;
    }

    @Schema(description = "DeleteLoansLoanIdResponse")
    public static final class DeleteLoansLoanIdResponse {

        private DeleteLoansLoanIdResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "1")
        public Long loanId;
        @Schema(example = "1")
        public Long resourceId;
        @Schema(example = "95174ff9-1a75-4d72-a413-6f9b1cb988b7")
        public String resourceExternalId;
    }

    @Schema(description = "PostLoansLoanIdRequest")
    public static final class PostLoansLoanIdRequest {

        private PostLoansLoanIdRequest() {}

        static final class PostLoansLoanIdDisbursementData {

            private PostLoansLoanIdDisbursementData() {}

            @Schema(example = "[2012, 4, 3]")
            public LocalDate expectedDisbursementDate;
            @Schema(example = "22000")
            public Double principal;
        }

        @Schema(example = "2")
        public Integer toLoanOfficerId;
        @Schema(example = "02 September 2014")
        public String assignmentDate;

        @Schema(example = "02 September 2014")
        public String unassignedDate;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "")
        public Integer fromLoanOfficerId;
        @Schema(example = "3e7791ce-aa10-11ec-b909-0242ac120002")
        public String externalId;
        @Schema(example = "5000.33")
        public BigDecimal transactionAmount;
        @Schema(example = "Description of disbursement details.")
        public String note;
        @Schema(example = "28 June 2022")
        public String actualDisbursementDate;
        @Schema(example = "3")
        public Integer paymentTypeId;
        @Schema(example = "28 June 2022")
        public String approvedOnDate;
        @Schema(example = "1000")
        public BigDecimal approvedLoanAmount;
        @Schema(example = "28 June 2022")
        public String expectedDisbursementDate;
        @Schema(example = "28 June 2022")
        public String rejectedOnDate;
        @Schema(example = "28 June 2022")
        public String withdrawnOnDate;
        @Schema(description = "List of PostLoansLoanIdDisbursementData")
        public List<PostLoansLoanIdDisbursementData> disbursementData;
    }

    @Schema(description = "PostLoansLoanIdResponse")
    public static final class PostLoansLoanIdResponse {

        private PostLoansLoanIdResponse() {}

        static final class PostLoansLoanIdStatus {

            private PostLoansLoanIdStatus() {}

            @Schema(example = "300")
            public Long id;
            @Schema(example = "loanStatusType.approved")
            public String code;
            @Schema(example = "Approved")
            public String value;
            @Schema(example = "false")
            public Boolean pendingApproval;
            @Schema(example = "false")
            public Boolean waitingForDisbursal;
            @Schema(example = "true")
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

        static final class PostLoansLoanIdChanges {

            private PostLoansLoanIdChanges() {}

            @Schema(example = "en")
            public String locale;
            @Schema(example = "dd MMMM yyyy")
            public String dateFormat;
            @Schema(example = "28 June 2022")
            public String approvedOnDate;
            @Schema(example = "Loan approval note")
            public String note;
            @Schema(description = "PostLoansLoanIdStatus")
            public PostLoansLoanIdStatus status;
        }

        @Schema(example = "2")
        public Integer officeId;
        @Schema(example = "6")
        public Long clientId;
        @Schema(example = "3")
        public Long loanId;
        @Schema(example = "3")
        public Long resourceId;
        @Schema(example = "95174ff9-1a75-4d72-a413-6f9b1cb988b7")
        public String resourceExternalId;
        @Schema(example = "22")
        public Long subResourceId;
        @Schema(example = "b4f8fefd-a14d-4487-8d80-6f2fb0e07836")
        public String subResourceExternalId;
        @Schema(description = "PostLoansLoanIdChanges")
        public PostLoansLoanIdChanges changes;
    }
}
