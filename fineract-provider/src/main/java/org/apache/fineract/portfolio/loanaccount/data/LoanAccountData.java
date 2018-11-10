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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.util.*;

import javax.persistence.Transient;

import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.collateral.data.CollateralData;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.floatingrates.data.InterestRatePeriodData;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductBorrowerCycleVariationData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductConfigurableAttributes;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductValueConditionType;
import org.apache.fineract.portfolio.note.data.NoteData;
import org.joda.time.LocalDate;
import org.springframework.util.CollectionUtils;

/**
 * Immutable data object representing loan account data.
 */
@SuppressWarnings("unused")
public class LoanAccountData {

    // basic loan details

    // identity
    private final Long id;
    private final String accountNo;
    private final String externalId;

    // status
    private final LoanStatusEnumData status;
    private final EnumOptionData subStatus;

    // related to
    private final Long clientId;
    private final String clientAccountNo;
    private final String clientName;
    private final Long clientOfficeId;
    private final GroupGeneralData group;
    private final Long loanProductId;
    private final String loanProductName;
    private final String loanProductDescription;
    private final boolean isLoanProductLinkedToFloatingRate;
    private final Long fundId;
    private final String fundName;
    private final Long loanPurposeId;
    private final String loanPurposeName;
    private final Long loanOfficerId;
    private final String loanOfficerName;
    private final EnumOptionData loanType;

    // terms
    private final CurrencyData currency;
    private final BigDecimal principal;
    private final BigDecimal approvedPrincipal;
    private final BigDecimal proposedPrincipal;

    private final Integer termFrequency;
    private final EnumOptionData termPeriodFrequencyType;
    private final Integer numberOfRepayments;
    private final Integer repaymentEvery;
    private final EnumOptionData repaymentFrequencyType;
    private final EnumOptionData repaymentFrequencyNthDayType;
    private final EnumOptionData repaymentFrequencyDayOfWeekType;
    private final BigDecimal interestRatePerPeriod;
    private final EnumOptionData interestRateFrequencyType;
    private final BigDecimal annualInterestRate;
    private final boolean isFloatingInterestRate;
    private final BigDecimal interestRateDifferential;

    // settings
    private final EnumOptionData amortizationType;
    private final EnumOptionData interestType;
    private final EnumOptionData interestCalculationPeriodType;
    private final Boolean allowPartialPeriodInterestCalcualtion;
    private final BigDecimal inArrearsTolerance;
    private final Long transactionProcessingStrategyId;
    private final String transactionProcessingStrategyName;
    private final Integer graceOnPrincipalPayment;
    private final Integer recurringMoratoriumOnPrincipalPeriods;
    private final Integer graceOnInterestPayment;
    private final Integer graceOnInterestCharged;
    private final Integer graceOnArrearsAgeing;
    private final LocalDate interestChargedFromDate;
    private final LocalDate expectedFirstRepaymentOnDate;
    private final Boolean syncDisbursementWithMeeting;

    // timeline
    private final LoanApplicationTimelineData timeline;

    // totals
    private final LoanSummaryData summary;

    // associations
    private final LoanScheduleData repaymentSchedule;
    private final Collection<LoanTransactionData> transactions;
    private final Collection<LoanChargeData> charges;
    private final Collection<CollateralData> collateral;
    private final Collection<GuarantorData> guarantors;
    private final CalendarData meeting;
    private final Collection<NoteData> notes;
    private final Collection<DisbursementData> disbursementDetails;
    private final LoanScheduleData originalSchedule;
    // template
    private final Collection<LoanProductData> productOptions;
    private final Collection<StaffData> loanOfficerOptions;
    private final Collection<CodeValueData> loanPurposeOptions;
    private final Collection<FundData> fundOptions;
    private final Collection<EnumOptionData> termFrequencyTypeOptions;
    private final Collection<EnumOptionData> repaymentFrequencyTypeOptions;
    private final Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions;
    private final Collection<EnumOptionData> repaymentFrequencyDaysOfWeekTypeOptions;

    private final Collection<EnumOptionData> interestRateFrequencyTypeOptions;
    private final Collection<EnumOptionData> amortizationTypeOptions;
    private final Collection<EnumOptionData> interestTypeOptions;
    private final Collection<EnumOptionData> interestCalculationPeriodTypeOptions;
    private final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions;
    private final Collection<ChargeData> chargeOptions;
    private final Collection<CodeValueData> loanCollateralOptions;
    private final Collection<CalendarData> calendarOptions;

    @Transient
    private final BigDecimal feeChargesAtDisbursementCharged;
    private final BigDecimal totalOverpaid;

    // loanCycle
    private final Integer loanCounter;
    private final Integer loanProductCounter;

    // linkable account details
    private final PortfolioAccountData linkedAccount;
    private final Collection<PortfolioAccountData> accountLinkingOptions;

    private final Boolean multiDisburseLoan;

    private final Boolean canDefineInstallmentAmount;

    private final BigDecimal fixedEmiAmount;

    private final BigDecimal maxOutstandingLoanBalance;

    private final Boolean canDisburse;

    private final Collection<LoanTermVariationsData> emiAmountVariations;
    private final Collection<LoanAccountSummaryData> clientActiveLoanOptions;
    private final Boolean canUseForTopup;
    private final boolean isTopup;
    private final Long closureLoanId;
    private final String closureLoanAccountNo;
    private final BigDecimal topupAmount;

    private LoanProductData product;

    private final Map<Long, LoanBorrowerCycleData> memberVariations;

    private final Boolean inArrears;

    private final Boolean isNPA;

    private final Collection<ChargeData> overdueCharges;

    private final EnumOptionData daysInMonthType;
    private final EnumOptionData daysInYearType;
    private final boolean isInterestRecalculationEnabled;

    private final LoanInterestRecalculationData interestRecalculationData;
    private final Boolean createStandingInstructionAtDisbursement;

    // Paid In Advance
    private final PaidInAdvanceData paidInAdvance;

    private final Collection<InterestRatePeriodData> interestRatesPeriods;

    // VariableInstallments
    private final Boolean isVariableInstallmentsAllowed;
    private final Integer minimumGap;
    private final Integer maximumGap;

    private List<DatatableData> datatables = null;
    private final Boolean isEqualAmortization;

    //import fields
    private String dateFormat;
    private String locale;
    private transient Integer rowIndex;
    private LocalDate submittedOnDate;
    private Long productId;
    private Integer loanTermFrequency;
    private EnumOptionData loanTermFrequencyType;
    private LocalDate repaymentsStartingFromDate;
    private String linkAccountId;
    private Long groupId;
    private LocalDate expectedDisbursementDate;

    public static LoanAccountData importInstanceIndividual(EnumOptionData loanTypeEnumOption,Long clientId,Long productId,
            Long loanOfficerId,LocalDate submittedOnDate,
            Long fundId,BigDecimal principal, Integer numberOfRepayments,Integer repaymentEvery,
            EnumOptionData repaidEveryFrequencyEnums, Integer loanTermFrequency,EnumOptionData loanTermFrequencyTypeEnum,
            BigDecimal nominalInterestRate,LocalDate expectedDisbursementDate ,EnumOptionData amortizationEnumOption,
            EnumOptionData interestMethodEnum, EnumOptionData interestCalculationPeriodTypeEnum,BigDecimal inArrearsTolerance,Long transactionProcessingStrategyId,
            Integer graceOnPrincipalPayment,Integer graceOnInterestPayment,Integer graceOnInterestCharged,
            LocalDate interestChargedFromDate,LocalDate repaymentsStartingFromDate,Integer rowIndex ,
            String externalId,Long groupId,Collection<LoanChargeData> charges,String linkAccountId,
            String locale,String dateFormat){

        return new LoanAccountData(loanTypeEnumOption, clientId, productId, loanOfficerId, submittedOnDate, fundId,
                principal, numberOfRepayments,
                repaymentEvery, repaidEveryFrequencyEnums, loanTermFrequency, loanTermFrequencyTypeEnum, nominalInterestRate, expectedDisbursementDate,
                amortizationEnumOption, interestMethodEnum, interestCalculationPeriodTypeEnum, inArrearsTolerance, transactionProcessingStrategyId,
                graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate, repaymentsStartingFromDate,
                rowIndex, externalId, null, charges, linkAccountId,locale,dateFormat);
    }


    public static LoanAccountData importInstanceGroup(EnumOptionData loanTypeEnumOption,Long groupIdforGroupLoan,Long productId,
            Long loanOfficerId,LocalDate submittedOnDate,
            Long fundId,BigDecimal principal, Integer numberOfRepayments,Integer repaidEvery,
            EnumOptionData repaidEveryFrequencyEnums, Integer loanTermFrequency,EnumOptionData loanTermFrequencyTypeEnum,
            BigDecimal nominalInterestRate, EnumOptionData amortizationEnumOption,EnumOptionData interestMethodEnum,
            EnumOptionData interestCalculationPeriodEnum,BigDecimal arrearsTolerance,
            Long transactionProcessingStrategyId,
            Integer graceOnPrincipalPayment,Integer graceOnInterestPayment,Integer graceOnInterestCharged,
            LocalDate interestChargedFromDate,LocalDate repaymentsStartingFromDate,
            Integer rowIndex ,String externalId,String linkAccountId,String locale,String dateFormat){

        return new LoanAccountData(loanTypeEnumOption, groupIdforGroupLoan, productId, loanOfficerId, submittedOnDate, fundId,
                principal, numberOfRepayments,
                repaidEvery, repaidEveryFrequencyEnums, loanTermFrequency, loanTermFrequencyTypeEnum, nominalInterestRate, null,
                amortizationEnumOption, interestMethodEnum, interestCalculationPeriodEnum, arrearsTolerance,
                transactionProcessingStrategyId, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged,
                interestChargedFromDate, repaymentsStartingFromDate, rowIndex, externalId, null, null, linkAccountId,locale,dateFormat);
    }
    
    private LoanAccountData(EnumOptionData loanType,Long clientId,Long productId,Long loanOfficerId,LocalDate submittedOnDate,
            Long fundId,BigDecimal principal, Integer numberOfRepayments,Integer repaymentEvery,
            EnumOptionData repaymentFrequencyType, Integer loanTermFrequency,EnumOptionData loanTermFrequencyType,
            BigDecimal interestRatePerPeriod,LocalDate expectedDisbursementDate ,EnumOptionData amortizationType,
            EnumOptionData interestType, EnumOptionData interestCalculationPeriodType,BigDecimal inArrearsTolerance,Long transactionProcessingStrategyId,
            Integer graceOnPrincipalPayment,Integer graceOnInterestPayment,Integer graceOnInterestCharged,
            LocalDate interestChargedFromDate,LocalDate repaymentsStartingFromDate,Integer rowIndex ,
            String externalId,Long groupId,Collection<LoanChargeData> charges,String linkAccountId,
            String locale,String dateFormat) {
        this.dateFormat=dateFormat;
        this.locale= locale;
        this.rowIndex=rowIndex;
        this.submittedOnDate=submittedOnDate;
        this.productId=productId;
        this.loanTermFrequency=loanTermFrequency;
        this.loanTermFrequencyType=loanTermFrequencyType;
        this.repaymentsStartingFromDate=repaymentsStartingFromDate;
        this.linkAccountId=linkAccountId;
        this.externalId = externalId;
        this.clientId = clientId;
        this.fundId = fundId;
        this.loanOfficerId = loanOfficerId;
        this.numberOfRepayments = numberOfRepayments;
        this.loanType = loanType;
        this.principal = principal;
        this.repaymentEvery = repaymentEvery;
        this.repaymentFrequencyType = repaymentFrequencyType;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.amortizationType = amortizationType;
        this.interestType = interestType;
        this.interestCalculationPeriodType = interestCalculationPeriodType;
        this.inArrearsTolerance = inArrearsTolerance;
        this.transactionProcessingStrategyId = transactionProcessingStrategyId;
        this.graceOnInterestPayment = graceOnInterestPayment;
        this.graceOnInterestCharged = graceOnInterestCharged;
        this.graceOnPrincipalPayment = graceOnPrincipalPayment;
        this.interestChargedFromDate = interestChargedFromDate;
        this.groupId=groupId;
        this.expectedDisbursementDate=expectedDisbursementDate;
        this.charges = charges;
        this.id = null;
        this.accountNo = null;

        this.status = null;
        this.subStatus = null;

        this.clientAccountNo = null;
        this.clientName = null;
        this.clientOfficeId = null;
        this.group = null;
        this.loanProductId = null;
        this.loanProductName = null;
        this.loanProductDescription = null;
        this.isLoanProductLinkedToFloatingRate = false;

        this.fundName = null;
        this.loanPurposeId = null;
        this.loanPurposeName = null;

        this.loanOfficerName = null;

        this.currency = null;

        this.approvedPrincipal = null;
        this.proposedPrincipal = null;
        this.termFrequency = null;
        this.termPeriodFrequencyType = null;


        this.repaymentFrequencyNthDayType = null;
        this.repaymentFrequencyDayOfWeekType = null;

        this.interestRateFrequencyType = null;
        this.annualInterestRate = null;
        this.isFloatingInterestRate = false;
        this.interestRateDifferential = null;

        this.allowPartialPeriodInterestCalcualtion = null;

        this.transactionProcessingStrategyName = null;

        this.recurringMoratoriumOnPrincipalPeriods = null;

        this.graceOnArrearsAgeing = null;

        this.expectedFirstRepaymentOnDate = null;
        this.syncDisbursementWithMeeting = null;
        this.timeline = null;
        this.summary = null;
        this.repaymentSchedule = null;
        this.transactions = null;

        this.collateral = null;
        this.guarantors = null;
        this.meeting = null;
        this.notes = null;
        this.disbursementDetails = null;
        this.originalSchedule = null;
        this.productOptions = null;
        this.loanOfficerOptions = null;
        this.loanPurposeOptions = null;
        this.fundOptions = null;
        this.termFrequencyTypeOptions = null;
        this.repaymentFrequencyTypeOptions = null;
        this.repaymentFrequencyNthDayTypeOptions = null;
        this.repaymentFrequencyDaysOfWeekTypeOptions = null;
        this.interestRateFrequencyTypeOptions = null;
        this.amortizationTypeOptions = null;
        this.interestTypeOptions = null;
        this.interestCalculationPeriodTypeOptions = null;
        this.transactionProcessingStrategyOptions = null;
        this.chargeOptions = null;
        this.loanCollateralOptions = null;
        this.calendarOptions = null;
        this.feeChargesAtDisbursementCharged = null;
        this.totalOverpaid = null;
        this.loanCounter = null;
        this.loanProductCounter = null;
        this.linkedAccount = null;
        this.accountLinkingOptions = null;
        this.multiDisburseLoan = null;
        this.canDefineInstallmentAmount = null;
        this.fixedEmiAmount = null;
        this.maxOutstandingLoanBalance = null;
        this.canDisburse = null;
        this.emiAmountVariations = null;
        this.clientActiveLoanOptions = null;
        this.canUseForTopup = null;
        this.isTopup = false;
        this.closureLoanId = null;
        this.closureLoanAccountNo = null;
        this.topupAmount = null;
        this.memberVariations = null;
        this.inArrears = null;
        this.isNPA = null;
        this.overdueCharges = null;
        this.daysInMonthType = null;
        this.daysInYearType = null;
        this.isInterestRecalculationEnabled = false;
        this.interestRecalculationData = null;
        this.createStandingInstructionAtDisbursement = null;
        this.paidInAdvance = null;
        this.interestRatesPeriods = null;
        this.isVariableInstallmentsAllowed = null;
        this.minimumGap = null;
        this.maximumGap = null;
        this.isEqualAmortization = null;
    }


    public Integer getRowIndex() {
        return rowIndex;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public LoanApplicationTimelineData getTimeline() {
        return timeline;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getLoanProductName() {
        return loanProductName;
    }

    public static final Comparator<LoanAccountData> ClientNameComparator = new Comparator<LoanAccountData>() {

        @Override
        public int compare(LoanAccountData loan1, LoanAccountData loan2) {
            String clientOfLoan1 = loan1.getClientName().toUpperCase(Locale.ENGLISH);
            String clientOfLoan2 = loan2.getClientName().toUpperCase(Locale.ENGLISH);
            return clientOfLoan1.compareTo(clientOfLoan2);
        }
    };

    public String getClientAccountNo() {
        return clientAccountNo;
    }
    /**
     * Used to produce a {@link LoanAccountData} with only collateral options
     * for now.
     */
    public static LoanAccountData collateralTemplate(final Collection<CodeValueData> loanCollateralOptions) {
        final Long id = null;
        final String accountNo = null;
        final LoanStatusEnumData status = null;
        final EnumOptionData subStatus = null;
        final String externalId = null;
        final Long clientId = null;
        final String clientName = null;
        final String clientAccountNo = null;
        final Long clientOfficeId = null;
        final GroupGeneralData group = null;
        final EnumOptionData loanType = null;
        final Long loanProductId = null;
        final String loanProductName = null;
        final String loanProductDescription = null;
        final boolean isLoanProductLinkedToFloatingRate = false;
        final Long fundId = null;
        final String fundName = null;
        final Long loanPurposeId = null;
        final String loanPurposeName = null;
        final Long loanOfficerId = null;
        final String loanOfficerName = null;
        final CurrencyData currencyData = null;
        final BigDecimal proposedPrincipal = null;
        final BigDecimal principal = null;
        final BigDecimal totalOverpaid = null;
        final BigDecimal inArrearsTolerance = null;
        final Integer termFrequency = null;
        final EnumOptionData termPeriodFrequencyType = null;
        final Integer numberOfRepayments = null;
        final Integer repaymentEvery = null;
        final EnumOptionData repaymentFrequencyType = null;
        final EnumOptionData repaymentFrequencyNthDayType = null;
        final EnumOptionData repaymentFrequencyDayOfWeekType = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;
        final EnumOptionData amortizationType = null;
        final BigDecimal interestRatePerPeriod = null;
        final EnumOptionData interestRateFrequencyType = null;
        final BigDecimal annualInterestRate = null;
        final EnumOptionData interestType = null;
        final boolean isFloatingInterestRate = false;
        final BigDecimal interestRateDifferential = null;
        final EnumOptionData interestCalculationPeriodType = null;
        final Boolean allowPartialPeriodInterestCalcualtion = null;
        final LocalDate expectedFirstRepaymentOnDate = null;
        final Integer graceOnPrincipalPayment = null;
        final Integer recurringMoratoriumOnPrincipalPeriods = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnInterestCharged = null;
        final Integer graceOnArrearsAgeing = null;
        final LocalDate interestChargedFromDate = null;
        final LoanApplicationTimelineData timeline = null;
        final LoanSummaryData summary = null;
        final BigDecimal feeChargesDueAtDisbursementCharged = null;

        final LoanScheduleData repaymentSchedule = null;
        final Collection<LoanTransactionData> transactions = null;
        final Collection<LoanChargeData> charges = null;
        final Collection<CollateralData> collateral = null;
        final Collection<GuarantorData> guarantors = null;
        final Collection<NoteData> notes = null;
        final CalendarData calendarData = null;
        final Collection<LoanProductData> productOptions = null;
        final Collection<EnumOptionData> termFrequencyTypeOptions = null;
        final Collection<EnumOptionData> repaymentFrequencyTypeOptions = null;
        final Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions = null;
        final Collection<EnumOptionData> repaymentFrequencyDayOfWeekTypeOptions = null;
        final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions = null;
        final Collection<EnumOptionData> interestRateFrequencyTypeOptions = null;
        final Collection<EnumOptionData> amortizationTypeOptions = null;
        final Collection<EnumOptionData> interestTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationPeriodTypeOptions = null;
        final Collection<FundData> fundOptions = null;
        final Collection<ChargeData> chargeOptions = null;
        final ChargeData chargeTemplate = null;
        final Collection<StaffData> loanOfficerOptions = null;
        final Collection<CodeValueData> loanPurposeOptions = null;
        final Collection<CalendarData> calendarOptions = null;
        final Boolean syncDisbursementWithMeeting = null;
        final Integer loancounter = null;
        final Integer loanProductCounter = null;
        final Collection<PortfolioAccountData> accountLinkingOptions = null;
        final PortfolioAccountData linkedAccount = null;
        final Collection<DisbursementData> disbursementData = null;
        final Boolean multiDisburseLoan = null;
        final Boolean canDefineInstallmentAmount = null;
        final BigDecimal fixedEmiAmount = null;
        final BigDecimal maxOutstandingLoanBalance = null;
        final Collection<LoanTermVariationsData> emiAmountVariations = null;
        final Map<Long, LoanBorrowerCycleData> memberVariations = null;
        final LoanProductData product = null;
        final Boolean inArrears = null;
        final Boolean isNPA = null;
        final Collection<ChargeData> overdueCharges = null;

        final EnumOptionData daysInMonthType = null;
        final EnumOptionData daysInYearType = null;
        final boolean isInterestRecalculationEnabled = false;
        final LoanInterestRecalculationData interestRecalculationData = null;
        final LoanScheduleData originalSchedule = null;
        final Boolean createStandingInstructionAtDisbursement = null;
        final PaidInAdvanceData paidInAdvance = null;
        final Collection<InterestRatePeriodData> interestRatesPeriods = null;
        final Boolean isVariableInstallmentsAllowed = Boolean.FALSE;
        final Integer minimumGap = null;
        final Integer maximumGap = null;
        final Boolean canUseForTopup = null;
        final Collection<LoanAccountSummaryData> clientActiveLoanOptions = null;
        final boolean isTopup = false;
        final Long closureLoanId = null;
        final String closureLoanAccountNo = null;
        final BigDecimal topupAmount = null;
        final boolean isEqualAmortization = false;
        return new LoanAccountData(id, accountNo, status, externalId, clientId, clientAccountNo, clientName, clientOfficeId, group,
                loanType, loanProductId, loanProductName, loanProductDescription, isLoanProductLinkedToFloatingRate, fundId, fundName,
                loanPurposeId, loanPurposeName, loanOfficerId, loanOfficerName, currencyData, proposedPrincipal, principal, principal,
                totalOverpaid, inArrearsTolerance, termFrequency, termPeriodFrequencyType, numberOfRepayments, repaymentEvery,
                repaymentFrequencyType, repaymentFrequencyNthDayType, repaymentFrequencyDayOfWeekType, transactionProcessingStrategyId,
                transactionProcessingStrategyName, amortizationType, interestRatePerPeriod, interestRateFrequencyType, annualInterestRate,
                interestType, isFloatingInterestRate, interestRateDifferential, interestCalculationPeriodType,
                allowPartialPeriodInterestCalcualtion, expectedFirstRepaymentOnDate, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment,
                graceOnInterestCharged, interestChargedFromDate, timeline, summary, feeChargesDueAtDisbursementCharged, repaymentSchedule,
                transactions, charges, collateral, guarantors, calendarData, productOptions, termFrequencyTypeOptions,
                repaymentFrequencyTypeOptions, repaymentFrequencyNthDayTypeOptions, repaymentFrequencyDayOfWeekTypeOptions,
                transactionProcessingStrategyOptions, interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions,
                interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, chargeTemplate, loanOfficerOptions, loanPurposeOptions,
                loanCollateralOptions, calendarOptions, syncDisbursementWithMeeting, loancounter, loanProductCounter, notes,
                accountLinkingOptions, linkedAccount, disbursementData, multiDisburseLoan, canDefineInstallmentAmount, fixedEmiAmount,
                maxOutstandingLoanBalance, emiAmountVariations, memberVariations, product, inArrears, graceOnArrearsAgeing, overdueCharges,
                isNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled, interestRecalculationData, originalSchedule,
                createStandingInstructionAtDisbursement, paidInAdvance, interestRatesPeriods, isVariableInstallmentsAllowed, minimumGap,
                maximumGap, subStatus, canUseForTopup, clientActiveLoanOptions, isTopup, closureLoanId, closureLoanAccountNo, topupAmount, isEqualAmortization);

    }

    /**
     * Used to produce a {@link LoanAccountData} with only client information
     * defaulted.
     */
    public static LoanAccountData clientDefaults(final Long clientId, final String clientAccountNo, final String clientName,
            final Long clientOfficeId, final LocalDate expectedDisbursementDate) {
        final Long id = null;
        final String accountNo = null;
        final LoanStatusEnumData status = null;
        final EnumOptionData subStatus = null;
        final String externalId = null;
        final GroupGeneralData group = null;
        final EnumOptionData loanType = null;
        final String groupName = null;
        final Long loanProductId = null;
        final String loanProductName = null;
        final String loanProductDescription = null;
        final boolean isLoanProductLinkedToFloatingRate = false;
        final Long fundId = null;
        final String fundName = null;
        final Long loanPurposeId = null;
        final String loanPurposeName = null;
        final Long loanOfficerId = null;
        final String loanOfficerName = null;
        final CurrencyData currencyData = null;
        final BigDecimal proposedPrincipal = null;
        final BigDecimal principal = null;
        final BigDecimal totalOverpaid = null;
        final BigDecimal inArrearsTolerance = null;
        final Integer termFrequency = null;
        final EnumOptionData termPeriodFrequencyType = null;
        final Integer numberOfRepayments = null;
        final Integer repaymentEvery = null;
        final EnumOptionData repaymentFrequencyType = null;
        final EnumOptionData repaymentFrequencyNthDayType = null;
        final EnumOptionData repaymentFrequencyDayOfWeekType = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;
        final EnumOptionData amortizationType = null;
        final BigDecimal interestRatePerPeriod = null;
        final EnumOptionData interestRateFrequencyType = null;
        final BigDecimal annualInterestRate = null;
        final EnumOptionData interestType = null;
        final boolean isFloatingInterestRate = false;
        final BigDecimal interestRateDifferential = null;
        final EnumOptionData interestCalculationPeriodType = null;
        final Boolean allowPartialPeriodInterestCalcualtion = null;
        final LocalDate expectedFirstRepaymentOnDate = null;
        final Integer graceOnPrincipalPayment = null;
        final Integer recurringMoratoriumOnPrincipalPeriods = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnArrearsAgeing = null;
        final Integer graceOnInterestCharged = null;
        final LocalDate interestChargedFromDate = null;
        final LoanApplicationTimelineData timeline = LoanApplicationTimelineData.templateDefault(expectedDisbursementDate);
        final LoanSummaryData summary = null;
        final BigDecimal feeChargesDueAtDisbursementCharged = null;

        final LoanScheduleData repaymentSchedule = null;
        final Collection<LoanTransactionData> transactions = null;
        final Collection<LoanChargeData> charges = null;
        final Collection<CollateralData> collateral = null;
        final Collection<GuarantorData> guarantors = null;
        final Collection<NoteData> notes = null;
        final CalendarData calendarData = null;
        final Collection<LoanProductData> productOptions = null;
        final Collection<EnumOptionData> termFrequencyTypeOptions = null;
        final Collection<EnumOptionData> repaymentFrequencyTypeOptions = null;
        final Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions = null;
        final Collection<EnumOptionData> repaymentFrequencyDayOfWeekTypeOptions = null;
        final Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = null;
        final Collection<EnumOptionData> interestRateFrequencyTypeOptions = null;
        final Collection<EnumOptionData> amortizationTypeOptions = null;
        final Collection<EnumOptionData> interestTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationPeriodTypeOptions = null;
        final Collection<FundData> fundOptions = null;
        final Collection<ChargeData> chargeOptions = null;
        final ChargeData chargeTemplate = null;
        final Collection<StaffData> loanOfficerOptions = null;
        final Collection<CodeValueData> loanPurposeOptions = null;
        final Collection<CodeValueData> loanCollateralOptions = null;
        final Collection<CalendarData> calendarOptions = null;
        final Boolean syncDisbursementWithMeeting = null;
        final Integer loancounter = null;
        final Integer loanProductCounter = null;
        final Collection<PortfolioAccountData> accountLinkingOptions = null;
        final PortfolioAccountData linkedAccount = null;
        final Collection<DisbursementData> disbursementData = null;
        final Boolean multiDisburseLoan = null;
        final Boolean canDefineInstallmentAmount = null;
        final BigDecimal fixedEmiAmount = null;
        final BigDecimal maxOutstandingLoanBalance = null;
        final Collection<LoanTermVariationsData> emiAmountVariations = null;
        final Map<Long, LoanBorrowerCycleData> memberVariations = null;
        final LoanProductData product = null;
        final Boolean inArrears = null;
        final Boolean isNPA = null;
        final Collection<ChargeData> overdueCharges = null;

        final EnumOptionData daysInMonthType = null;
        final EnumOptionData daysInYearType = null;
        final boolean isInterestRecalculationEnabled = false;
        final LoanInterestRecalculationData interestRecalculationData = null;
        final LoanScheduleData originalSchedule = null;
        final Boolean createStandingInstructionAtDisbursement = null;
        final PaidInAdvanceData paidInAdvance = null;
        final Collection<InterestRatePeriodData> interestRatesPeriods = null;

        final Boolean isVariableInstallmentsAllowed = Boolean.FALSE;
        final Integer minimumGap = null;
        final Integer maximumGap = null;
        final Boolean canUseForTopup = null;
        final Collection<LoanAccountSummaryData> clientActiveLoanOptions = null;
        final boolean isTopup = false;
        final Long closureLoanId = null;
        final String closureLoanAccountNo = null;
        final BigDecimal topupAmount = null;
        final boolean isEqualAmortization = false;
        return new LoanAccountData(id, accountNo, status, externalId, clientId, clientAccountNo, clientName, clientOfficeId, group,
                loanType, loanProductId, loanProductName, loanProductDescription, isLoanProductLinkedToFloatingRate, fundId, fundName,
                loanPurposeId, loanPurposeName, loanOfficerId, loanOfficerName, currencyData, proposedPrincipal, principal, principal,
                totalOverpaid, inArrearsTolerance, termFrequency, termPeriodFrequencyType, numberOfRepayments, repaymentEvery,
                repaymentFrequencyType, repaymentFrequencyNthDayType, repaymentFrequencyDayOfWeekType, transactionProcessingStrategyId,
                transactionProcessingStrategyName, amortizationType, interestRatePerPeriod, interestRateFrequencyType, annualInterestRate,
                interestType, isFloatingInterestRate, interestRateDifferential, interestCalculationPeriodType,
                allowPartialPeriodInterestCalcualtion, expectedFirstRepaymentOnDate, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment,
                graceOnInterestCharged, interestChargedFromDate, timeline, summary, feeChargesDueAtDisbursementCharged, repaymentSchedule,
                transactions, charges, collateral, guarantors, calendarData, productOptions, termFrequencyTypeOptions,
                repaymentFrequencyTypeOptions, repaymentFrequencyNthDayTypeOptions, repaymentFrequencyDayOfWeekTypeOptions,
                repaymentStrategyOptions, interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions,
                interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, chargeTemplate, loanOfficerOptions, loanPurposeOptions,
                loanCollateralOptions, calendarOptions, syncDisbursementWithMeeting, loancounter, loanProductCounter, notes,
                accountLinkingOptions, linkedAccount, disbursementData, multiDisburseLoan, canDefineInstallmentAmount, fixedEmiAmount,
                maxOutstandingLoanBalance, emiAmountVariations, memberVariations, product, inArrears, graceOnArrearsAgeing, overdueCharges,
                isNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled, interestRecalculationData, originalSchedule,
                createStandingInstructionAtDisbursement, paidInAdvance, interestRatesPeriods, isVariableInstallmentsAllowed, minimumGap,
                maximumGap, subStatus, canUseForTopup, clientActiveLoanOptions, isTopup, closureLoanId, closureLoanAccountNo, topupAmount, isEqualAmortization);

    }

    public static LoanAccountData populateClientDefaults(final LoanAccountData acc, final LoanAccountData clientAcc) {

        return new LoanAccountData(acc.id, acc.accountNo, acc.status, acc.externalId, clientAcc.clientId, clientAcc.clientAccountNo,
                clientAcc.clientName, clientAcc.clientOfficeId, acc.group, acc.loanType, acc.loanProductId, acc.loanProductName,
                acc.loanProductDescription, acc.isLoanProductLinkedToFloatingRate, acc.fundId, acc.fundName, acc.loanPurposeId,
                acc.loanPurposeName, acc.loanOfficerId, acc.loanOfficerName, acc.currency, acc.proposedPrincipal, acc.principal,
                acc.approvedPrincipal, acc.totalOverpaid, acc.inArrearsTolerance, acc.termFrequency, acc.termPeriodFrequencyType,
                acc.numberOfRepayments, acc.repaymentEvery, acc.repaymentFrequencyType, acc.repaymentFrequencyNthDayType,
                acc.repaymentFrequencyDayOfWeekType, acc.transactionProcessingStrategyId, acc.transactionProcessingStrategyName,
                acc.amortizationType, acc.interestRatePerPeriod, acc.interestRateFrequencyType, acc.annualInterestRate, acc.interestType,
                acc.isFloatingInterestRate, acc.interestRateDifferential, acc.interestCalculationPeriodType,
                acc.allowPartialPeriodInterestCalcualtion, acc.expectedFirstRepaymentOnDate, acc.graceOnPrincipalPayment, acc.recurringMoratoriumOnPrincipalPeriods,
                acc.graceOnInterestPayment, acc.graceOnInterestCharged, acc.interestChargedFromDate, clientAcc.timeline, acc.summary,
                acc.feeChargesAtDisbursementCharged, acc.repaymentSchedule, acc.transactions, acc.charges, acc.collateral, acc.guarantors,
                acc.meeting, acc.productOptions, acc.termFrequencyTypeOptions, acc.repaymentFrequencyTypeOptions,
                acc.repaymentFrequencyNthDayTypeOptions, acc.repaymentFrequencyDaysOfWeekTypeOptions,
                acc.transactionProcessingStrategyOptions, acc.interestRateFrequencyTypeOptions, acc.amortizationTypeOptions,
                acc.interestTypeOptions, acc.interestCalculationPeriodTypeOptions, acc.fundOptions, acc.chargeOptions, null,
                acc.loanOfficerOptions, acc.loanPurposeOptions, acc.loanCollateralOptions, acc.calendarOptions,
                acc.syncDisbursementWithMeeting, acc.loanCounter, acc.loanProductCounter, acc.notes, acc.accountLinkingOptions,
                acc.linkedAccount, acc.disbursementDetails, acc.multiDisburseLoan, acc.canDefineInstallmentAmount, acc.fixedEmiAmount,
                acc.maxOutstandingLoanBalance, acc.emiAmountVariations, acc.memberVariations, acc.product, acc.inArrears,
                acc.graceOnArrearsAgeing, acc.overdueCharges, acc.isNPA, acc.daysInMonthType, acc.daysInYearType,
                acc.isInterestRecalculationEnabled, acc.interestRecalculationData, acc.originalSchedule,
                acc.createStandingInstructionAtDisbursement, acc.paidInAdvance, acc.interestRatesPeriods,
                acc.isVariableInstallmentsAllowed, acc.minimumGap, acc.maximumGap, acc.subStatus, acc.canUseForTopup,
                acc.clientActiveLoanOptions, acc.isTopup, acc.closureLoanId, acc.closureLoanAccountNo, acc.topupAmount, acc.isEqualAmortization);
    }

    /**
     * Used to produce a {@link LoanAccountData} with only group information
     * defaulted.
     */
    public static LoanAccountData groupDefaults(final GroupGeneralData group, final LocalDate expectedDisbursementDate) {

        final Long id = null;
        final String accountNo = null;
        final LoanStatusEnumData status = null;
        final EnumOptionData subStatus = null;
        final String externalId = null;
        final Long clientId = null;
        final String clientAccountNo = null;
        final String clientName = null;
        final Long clientOfficeId = null;
        final EnumOptionData loanType = null;
        final Long loanProductId = null;
        final String loanProductName = null;
        final String loanProductDescription = null;
        final boolean isLoanProductLinkedToFloatingRate = false;
        final Long fundId = null;
        final String fundName = null;
        final Long loanPurposeId = null;
        final String loanPurposeName = null;
        final Long loanOfficerId = null;
        final String loanOfficerName = null;
        final CurrencyData currencyData = null;
        final BigDecimal proposedPrincipal = null;
        final BigDecimal principal = null;
        final BigDecimal totalOverpaid = null;
        final BigDecimal inArrearsTolerance = null;
        final Integer termFrequency = null;
        final EnumOptionData termPeriodFrequencyType = null;
        final Integer numberOfRepayments = null;
        final Integer repaymentEvery = null;
        final EnumOptionData repaymentFrequencyType = null;
        final EnumOptionData repaymentFrequencyNthDayType = null;
        final EnumOptionData repaymentFrequencyDayOfWeekType = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;
        final EnumOptionData amortizationType = null;
        final BigDecimal interestRatePerPeriod = null;
        final EnumOptionData interestRateFrequencyType = null;
        final BigDecimal annualInterestRate = null;
        final EnumOptionData interestType = null;
        final boolean isFloatingInterestRate = false;
        final BigDecimal interestRateDifferential = null;
        final EnumOptionData interestCalculationPeriodType = null;
        final Boolean allowPartialPeriodInterestCalcualtion = null;
        final LocalDate expectedFirstRepaymentOnDate = null;
        final Integer graceOnPrincipalPayment = null;
        final Integer recurringMoratoriumOnPrincipalPeriods = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnInterestCharged = null;
        final Integer graceOnArrearsAgeing = null;
        final LocalDate interestChargedFromDate = null;
        final LoanApplicationTimelineData timeline = LoanApplicationTimelineData.templateDefault(expectedDisbursementDate);
        final LoanSummaryData summary = null;
        final BigDecimal feeChargesDueAtDisbursementCharged = null;

        final LoanScheduleData repaymentSchedule = null;
        final Collection<LoanTransactionData> transactions = null;
        final Collection<LoanChargeData> charges = null;
        final Collection<CollateralData> collateral = null;
        final Collection<GuarantorData> guarantors = null;
        final Collection<NoteData> notes = null;
        final CalendarData calendarData = null;
        final Collection<LoanProductData> productOptions = null;
        final Collection<EnumOptionData> termFrequencyTypeOptions = null;
        final Collection<EnumOptionData> repaymentFrequencyTypeOptions = null;
        final Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions = null;
        final Collection<EnumOptionData> repaymentFrequencyDayOfWeekTypeOptions = null;
        final Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = null;
        final Collection<EnumOptionData> interestRateFrequencyTypeOptions = null;
        final Collection<EnumOptionData> amortizationTypeOptions = null;
        final Collection<EnumOptionData> interestTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationPeriodTypeOptions = null;
        final Collection<FundData> fundOptions = null;
        final Collection<ChargeData> chargeOptions = null;
        final ChargeData chargeTemplate = null;
        final Collection<StaffData> loanOfficerOptions = null;
        final Collection<CodeValueData> loanPurposeOptions = null;
        final Collection<CodeValueData> loanCollateralOptions = null;
        final Collection<CalendarData> calendarOptions = null;
        final Boolean syncDisbursementWithMeeting = null;
        final Integer loancounter = null;
        final Integer loanProductCounter = null;
        final Collection<PortfolioAccountData> accountLinkingOptions = null;
        final PortfolioAccountData linkedAccount = null;
        final Collection<DisbursementData> disbursementData = null;
        final Boolean multiDisburseLoan = null;
        final Boolean canDefineInstallmentAmount = null;
        final BigDecimal fixedEmiAmount = null;
        final BigDecimal maxOutstandingBalance = null;
        final Collection<LoanTermVariationsData> emiAmountVariations = null;
        final Map<Long, LoanBorrowerCycleData> memberVariations = null;
        final LoanProductData product = null;
        final Boolean inArrears = null;
        final Boolean isNPA = null;
        final Collection<ChargeData> overdueCharges = null;

        final EnumOptionData daysInMonthType = null;
        final EnumOptionData daysInYearType = null;
        final boolean isInterestRecalculationEnabled = false;
        final LoanInterestRecalculationData interestRecalculationData = null;
        final LoanScheduleData originalSchedule = null;
        final Boolean createStandingInstructionAtDisbursement = null;
        final PaidInAdvanceData paidInAdvance = null;
        final Collection<InterestRatePeriodData> interestRatesPeriods = null;

        final Boolean isVariableInstallmentsAllowed = Boolean.FALSE;
        final Integer minimumGap = null;
        final Integer maximumGap = null;
        final Boolean canUseForTopup = null;
        final Collection<LoanAccountSummaryData> clientActiveLoanOptions = null;
        final boolean isTopup = false;
        final Long closureLoanId = null;
        final String closureLoanAccountNo = null;
        final BigDecimal topupAmount = null;
        final boolean isEqualAmortization = false;
        return new LoanAccountData(id, accountNo, status, externalId, clientId, clientAccountNo, clientName, clientOfficeId, group,
                loanType, loanProductId, loanProductName, loanProductDescription, isLoanProductLinkedToFloatingRate, fundId, fundName,
                loanPurposeId, loanPurposeName, loanOfficerId, loanOfficerName, currencyData, proposedPrincipal, principal, principal,
                totalOverpaid, inArrearsTolerance, termFrequency, termPeriodFrequencyType, numberOfRepayments, repaymentEvery,
                repaymentFrequencyType, repaymentFrequencyNthDayType, repaymentFrequencyDayOfWeekType, transactionProcessingStrategyId,
                transactionProcessingStrategyName, amortizationType, interestRatePerPeriod, interestRateFrequencyType, annualInterestRate,
                interestType, isFloatingInterestRate, interestRateDifferential, interestCalculationPeriodType,
                allowPartialPeriodInterestCalcualtion, expectedFirstRepaymentOnDate, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment,
                graceOnInterestCharged, interestChargedFromDate, timeline, summary, feeChargesDueAtDisbursementCharged, repaymentSchedule,
                transactions, charges, collateral, guarantors, calendarData, productOptions, termFrequencyTypeOptions,
                repaymentFrequencyTypeOptions, repaymentFrequencyNthDayTypeOptions, repaymentFrequencyDayOfWeekTypeOptions,
                repaymentStrategyOptions, interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions,
                interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, chargeTemplate, loanOfficerOptions, loanPurposeOptions,
                loanCollateralOptions, calendarOptions, syncDisbursementWithMeeting, loancounter, loanProductCounter, notes,
                accountLinkingOptions, linkedAccount, disbursementData, multiDisburseLoan, canDefineInstallmentAmount, fixedEmiAmount,
                maxOutstandingBalance, emiAmountVariations, memberVariations, product, inArrears, graceOnArrearsAgeing, overdueCharges,
                isNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled, interestRecalculationData, originalSchedule,
                createStandingInstructionAtDisbursement, paidInAdvance, interestRatesPeriods, isVariableInstallmentsAllowed, minimumGap,
                maximumGap, subStatus, canUseForTopup, clientActiveLoanOptions, isTopup, closureLoanId, closureLoanAccountNo, topupAmount, isEqualAmortization);

    }

    public static LoanAccountData populateGroupDefaults(final LoanAccountData acc, final LoanAccountData groupAcc) {

        return new LoanAccountData(acc.id, acc.accountNo, acc.status, acc.externalId, acc.clientId, acc.clientAccountNo, acc.clientName,
                acc.clientOfficeId, groupAcc.group, acc.loanType, acc.loanProductId, acc.loanProductName, acc.loanProductDescription,
                acc.isLoanProductLinkedToFloatingRate, acc.fundId, acc.fundName, acc.loanPurposeId, acc.loanPurposeName, acc.loanOfficerId,
                acc.loanOfficerName, acc.currency, acc.proposedPrincipal, acc.principal, acc.approvedPrincipal, acc.totalOverpaid,
                acc.inArrearsTolerance, acc.termFrequency, acc.termPeriodFrequencyType, acc.numberOfRepayments, acc.repaymentEvery,
                acc.repaymentFrequencyType, acc.repaymentFrequencyNthDayType, acc.repaymentFrequencyDayOfWeekType,
                acc.transactionProcessingStrategyId, acc.transactionProcessingStrategyName, acc.amortizationType,
                acc.interestRatePerPeriod, acc.interestRateFrequencyType, acc.annualInterestRate, acc.interestType,
                acc.isFloatingInterestRate, acc.interestRateDifferential, acc.interestCalculationPeriodType,
                acc.allowPartialPeriodInterestCalcualtion, acc.expectedFirstRepaymentOnDate, acc.graceOnPrincipalPayment, acc.recurringMoratoriumOnPrincipalPeriods,
                acc.graceOnInterestPayment, acc.graceOnInterestCharged, acc.interestChargedFromDate, groupAcc.timeline, acc.summary,
                acc.feeChargesAtDisbursementCharged, acc.repaymentSchedule, acc.transactions, acc.charges, acc.collateral, acc.guarantors,
                acc.meeting, acc.productOptions, acc.termFrequencyTypeOptions, acc.repaymentFrequencyTypeOptions,
                acc.repaymentFrequencyNthDayTypeOptions, acc.repaymentFrequencyDaysOfWeekTypeOptions,
                acc.transactionProcessingStrategyOptions, acc.interestRateFrequencyTypeOptions, acc.amortizationTypeOptions,
                acc.interestTypeOptions, acc.interestCalculationPeriodTypeOptions, acc.fundOptions, acc.chargeOptions, null,
                acc.loanOfficerOptions, acc.loanPurposeOptions, acc.loanCollateralOptions, acc.calendarOptions,
                acc.syncDisbursementWithMeeting, acc.loanCounter, acc.loanProductCounter, acc.notes, acc.accountLinkingOptions,
                acc.linkedAccount, acc.disbursementDetails, acc.multiDisburseLoan, acc.canDefineInstallmentAmount, acc.fixedEmiAmount,
                acc.maxOutstandingLoanBalance, acc.emiAmountVariations, acc.memberVariations, acc.product, acc.inArrears,
                acc.graceOnArrearsAgeing, acc.overdueCharges, acc.isNPA, acc.daysInMonthType, acc.daysInYearType,
                acc.isInterestRecalculationEnabled, acc.interestRecalculationData, acc.originalSchedule,
                acc.createStandingInstructionAtDisbursement, acc.paidInAdvance, acc.interestRatesPeriods,
                acc.isVariableInstallmentsAllowed, acc.minimumGap, acc.maximumGap, acc.subStatus, acc.canUseForTopup,
                acc.clientActiveLoanOptions, acc.isTopup, acc.closureLoanId, acc.closureLoanAccountNo, acc.topupAmount, acc.isEqualAmortization);

    }

    public static LoanAccountData loanProductWithTemplateDefaults(final LoanProductData product,
            final Collection<EnumOptionData> termFrequencyTypeOptions, final Collection<EnumOptionData> repaymentFrequencyTypeOptions,
            final Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions,
            final Collection<EnumOptionData> repaymentFrequencyDayOfWeekTypeOptions,
            final Collection<TransactionProcessingStrategyData> repaymentStrategyOptions,
            final Collection<EnumOptionData> interestRateFrequencyTypeOptions, final Collection<EnumOptionData> amortizationTypeOptions,
            final Collection<EnumOptionData> interestTypeOptions, final Collection<EnumOptionData> interestCalculationPeriodTypeOptions,
            final Collection<FundData> fundOptions, final Collection<ChargeData> chargeOptions,
            final Collection<CodeValueData> loanPurposeOptions, final Collection<CodeValueData> loanCollateralOptions,
            final Integer loanCycleNumber, final Collection<LoanAccountSummaryData> clientActiveLoanOptions) {

        final Long id = null;
        final String accountNo = null;
        final LoanStatusEnumData status = null;
        final EnumOptionData subStatus = null;
        final String externalId = null;
        final Long clientId = null;
        final String clientAccountNo = null;
        final String clientName = null;
        final Long clientOfficeId = null;
        final GroupGeneralData group = null;
        final EnumOptionData loanType = null;
        final String groupName = null;
        final Long fundId = null;
        final String fundName = null;
        final Long loanPurposeId = null;
        final String loanPurposeName = null;
        final Long loanOfficerId = null;
        final String loanOfficerName = null;
        final CurrencyData currencyData = null;

        final BigDecimal totalOverpaid = null;
        final BigDecimal inArrearsTolerance = null;

        final Integer repaymentEvery = null;
        final EnumOptionData repaymentFrequencyType = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;
        final EnumOptionData amortizationType = null;

        final EnumOptionData interestRateFrequencyType = null;
        final BigDecimal annualInterestRate = null;
        final EnumOptionData interestType = null;
        final EnumOptionData interestCalculationPeriodType = null;
        final LocalDate expectedFirstRepaymentOnDate = null;
        final LocalDate interestChargedFromDate = null;
        final LoanApplicationTimelineData timeline = null;
        final LoanSummaryData summary = null;
        final BigDecimal feeChargesDueAtDisbursementCharged = null;
        final ChargeData chargeTemplate = null;
        final Boolean syncDisbursementWithMeeting = null;

        final LoanScheduleData repaymentSchedule = null;
        final Collection<LoanTransactionData> transactions = null;
        final Collection<CollateralData> collateral = null;
        final Collection<GuarantorData> guarantors = null;
        final Collection<NoteData> notes = null;
        final CalendarData calendarData = null;
        final Collection<LoanProductData> productOptions = null;
        final Collection<CalendarData> calendarOptions = null;
        final Collection<StaffData> loanOfficerOptions = null;

        final EnumOptionData termPeriodFrequencyType = product.getRepaymentFrequencyType();

        final Collection<LoanChargeData> charges = new ArrayList<LoanChargeData>();
        for (final ChargeData charge : product.charges()) {
            if (!charge.isOverdueInstallmentCharge()) {
                charges.add(charge.toLoanChargeData());
            }
        }

        final Integer loancounter = null;
        final Integer loanProductCounter = null;
        final PortfolioAccountData linkedAccount = null;
        final Collection<PortfolioAccountData> accountLinkingOptions = null;
        final Collection<DisbursementData> disbursementData = null;
        final Collection<LoanTermVariationsData> emiAmountVariations = null;
        BigDecimal principal = null;
        BigDecimal proposedPrincipal = null;

        BigDecimal interestRatePerPeriod = null;
        Integer numberOfRepayments = null;
        if (product.useBorrowerCycle() && loanCycleNumber > 0) {
            Collection<LoanProductBorrowerCycleVariationData> principalVariationsForBorrowerCycle = product
                    .getPrincipalVariationsForBorrowerCycle();
            Collection<LoanProductBorrowerCycleVariationData> interestForVariationsForBorrowerCycle = product
                    .getInterestRateVariationsForBorrowerCycle();
            Collection<LoanProductBorrowerCycleVariationData> repaymentVariationsForBorrowerCycle = product
                    .getNumberOfRepaymentVariationsForBorrowerCycle();
            principal = fetchLoanCycleDefaultValue(principalVariationsForBorrowerCycle, loanCycleNumber);
            proposedPrincipal = principal;
            interestRatePerPeriod = fetchLoanCycleDefaultValue(interestForVariationsForBorrowerCycle, loanCycleNumber);
            BigDecimal numberofRepaymentval = fetchLoanCycleDefaultValue(repaymentVariationsForBorrowerCycle, loanCycleNumber);
            if (numberofRepaymentval != null) {
                numberOfRepayments = numberofRepaymentval.intValue();
            }
        }
        if (principal == null) {
            principal = product.getPrincipal();
            proposedPrincipal = principal;
        }
        if (interestRatePerPeriod == null) {
            interestRatePerPeriod = product.getInterestRatePerPeriod();
        }
        if (numberOfRepayments == null) {
            numberOfRepayments = product.getNumberOfRepayments();
        }

        final Integer termFrequency = numberOfRepayments * product.getRepaymentEvery();
        final BigDecimal fixedEmi = null;
        Map<Long, LoanBorrowerCycleData> memberVariations = null;
        final Boolean inArrears = null;
        final Boolean isNPA = null;
        final LoanScheduleData originalSchedule = null;
        final Boolean createStandingInstructionAtDisbursement = null;
        final PaidInAdvanceData paidInAdvance = null;
        final Collection<InterestRatePeriodData> interestRatesPeriods = null;

        final Boolean isVariableInstallmentsAllowed = Boolean.FALSE;
        final Integer minimumGap = null;
        final Integer maximumGap = null;
        final Boolean canUseForTopup = product.canUseForTopup();
        final boolean isTopup = false;
        final Long closureLoanId = null;
        final String closureLoanAccountNo = null;
        final BigDecimal topupAmount = null;
        return new LoanAccountData(id, accountNo, status, externalId, clientId, clientAccountNo, clientName, clientOfficeId, group,
                loanType, product.getId(), product.getName(), product.getDescription(), product.isLinkedToFloatingInterestRates(),
                product.getFundId(), product.getFundName(), loanPurposeId, loanPurposeName, loanOfficerId, loanOfficerName,
                product.getCurrency(), proposedPrincipal, principal, principal, totalOverpaid, product.getInArrearsTolerance(),
                termFrequency, termPeriodFrequencyType, numberOfRepayments, product.getRepaymentEvery(),
                product.getRepaymentFrequencyType(), null, null, product.getTransactionProcessingStrategyId(),
                transactionProcessingStrategyName, product.getAmortizationType(), interestRatePerPeriod,
                product.getInterestRateFrequencyType(), product.getAnnualInterestRate(), product.getInterestType(),
                product.isFloatingInterestRateCalculationAllowed(), product.getDefaultDifferentialLendingRate(),
                product.getInterestCalculationPeriodType(), product.getAllowPartialPeriodInterestCalcualtion(),
                expectedFirstRepaymentOnDate, product.getGraceOnPrincipalPayment(), product.getRecurringMoratoriumOnPrincipalPeriods(), product.getGraceOnInterestPayment(),
                product.getGraceOnInterestCharged(), interestChargedFromDate, timeline, summary, feeChargesDueAtDisbursementCharged,
                repaymentSchedule, transactions, charges, collateral, guarantors, calendarData, productOptions, termFrequencyTypeOptions,
                repaymentFrequencyTypeOptions, repaymentFrequencyNthDayTypeOptions, repaymentFrequencyDayOfWeekTypeOptions,
                repaymentStrategyOptions, interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions,
                interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, chargeTemplate, loanOfficerOptions, loanPurposeOptions,
                loanCollateralOptions, calendarOptions, syncDisbursementWithMeeting, loancounter, loanProductCounter, notes,
                accountLinkingOptions, linkedAccount, disbursementData, product.getMultiDisburseLoan(),
                product.canDefineInstallmentAmount(), fixedEmi, product.getOutstandingLoanBalance(), emiAmountVariations, memberVariations,
                product, inArrears, product.getGraceOnArrearsAgeing(), product.overdueFeeCharges(), isNPA, product.getDaysInMonthType(),
                product.getDaysInYearType(), product.isInterestRecalculationEnabled(), product.toLoanInterestRecalculationData(),
                originalSchedule, createStandingInstructionAtDisbursement, paidInAdvance, interestRatesPeriods,
                product.isVariableInstallmentsAllowed(), product.getMinimumGapBetweenInstallments(),
                product.getMaximumGapBetweenInstallments(), subStatus, canUseForTopup, clientActiveLoanOptions, isTopup, closureLoanId,
                closureLoanAccountNo, topupAmount, product.isEqualAmortization());
    }

    public static LoanAccountData populateLoanProductDefaults(final LoanAccountData acc, final LoanProductData product) {

        final LoanScheduleData repaymentSchedule = null;
        final Collection<LoanTransactionData> transactions = null;
        final Collection<CollateralData> collateral = null;
        final Collection<GuarantorData> guarantors = null;
        final Collection<NoteData> notes = null;
        final CalendarData calendarData = null;
        final Collection<LoanProductData> productOptions = null;
        final Collection<EnumOptionData> termFrequencyTypeOptions = null;
        final Collection<EnumOptionData> repaymentFrequencyTypeOptions = null;
        final Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = null;
        final Collection<EnumOptionData> interestRateFrequencyTypeOptions = null;
        final Collection<EnumOptionData> amortizationTypeOptions = null;
        final Collection<EnumOptionData> interestTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationPeriodTypeOptions = null;
        final Collection<FundData> fundOptions = null;
        final Collection<ChargeData> chargeOptions = null;
        final ChargeData chargeTemplate = null;
        final Collection<StaffData> loanOfficerOptions = null;
        final Collection<CodeValueData> loanPurposeOptions = null;
        final Collection<CodeValueData> loanCollateralOptions = null;
        final Collection<CalendarData> calendarOptions = null;
        final PaidInAdvanceData paidInAdvance = null;

        final Integer termFrequency = product.getNumberOfRepayments() * product.getRepaymentEvery();
        final EnumOptionData termPeriodFrequencyType = product.getRepaymentFrequencyType();

        final Collection<LoanChargeData> charges = new ArrayList<LoanChargeData>();
        for (final ChargeData charge : product.charges()) {
            charges.add(charge.toLoanChargeData());
        }

        return new LoanAccountData(acc.id, acc.accountNo, acc.status, acc.externalId, acc.clientId, acc.clientAccountNo, acc.clientName,
                acc.clientOfficeId, acc.group, acc.loanType, product.getId(), product.getName(), product.getDescription(),
                product.isLinkedToFloatingInterestRates(), product.getFundId(), product.getFundName(), acc.loanPurposeId,
                acc.loanPurposeName, acc.loanOfficerId, acc.loanOfficerName, product.getCurrency(), product.getPrincipal(),
                product.getPrincipal(), product.getPrincipal(), acc.totalOverpaid, product.getInArrearsTolerance(), termFrequency,
                termPeriodFrequencyType, product.getNumberOfRepayments(), product.getRepaymentEvery(), product.getRepaymentFrequencyType(),
                null, null, product.getTransactionProcessingStrategyId(), product.getTransactionProcessingStrategyName(),
                product.getAmortizationType(), product.getInterestRatePerPeriod(), product.getInterestRateFrequencyType(),
                product.getAnnualInterestRate(), product.getInterestType(), product.isFloatingInterestRateCalculationAllowed(),
                product.getDefaultDifferentialLendingRate(), product.getInterestCalculationPeriodType(),
                product.getAllowPartialPeriodInterestCalcualtion(), acc.expectedFirstRepaymentOnDate, product.getGraceOnPrincipalPayment(), product.getRecurringMoratoriumOnPrincipalPeriods(),
                product.getGraceOnInterestPayment(), product.getGraceOnInterestCharged(), acc.interestChargedFromDate, acc.timeline,
                acc.summary, acc.feeChargesAtDisbursementCharged, repaymentSchedule, transactions, charges, collateral, guarantors,
                calendarData, productOptions, termFrequencyTypeOptions, repaymentFrequencyTypeOptions, null, null,
                repaymentStrategyOptions, interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions,
                interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, chargeTemplate, loanOfficerOptions, loanPurposeOptions,
                loanCollateralOptions, calendarOptions, acc.syncDisbursementWithMeeting, acc.loanCounter, acc.loanProductCounter, notes,
                acc.accountLinkingOptions, acc.linkedAccount, acc.disbursementDetails, product.getMultiDisburseLoan(),
                product.canDefineInstallmentAmount(), acc.fixedEmiAmount, product.getOutstandingLoanBalance(), acc.emiAmountVariations,
                acc.memberVariations, product, acc.inArrears, product.getGraceOnArrearsAgeing(), product.overdueFeeCharges(), acc.isNPA,
                product.getDaysInMonthType(), product.getDaysInYearType(), product.isInterestRecalculationEnabled(),
                product.toLoanInterestRecalculationData(), acc.originalSchedule, acc.createStandingInstructionAtDisbursement,
                paidInAdvance, acc.interestRatesPeriods, product.isVariableInstallmentsAllowed(),
                product.getMinimumGapBetweenInstallments(), product.getMaximumGapBetweenInstallments(), acc.subStatus, acc.canUseForTopup,
                acc.clientActiveLoanOptions, acc.isTopup, acc.closureLoanId, acc.closureLoanAccountNo, acc.topupAmount, product.isEqualAmortization());

    }

    /*
     * Used to send back loan account data with the basic details coming from
     * query.
     */
    public static LoanAccountData basicLoanDetails(final Long id, final String accountNo, final LoanStatusEnumData status,
            final String externalId, final Long clientId, final String clientAccountNo, final String clientName, final Long clientOfficeId,
            final GroupGeneralData group, final EnumOptionData loanType, final Long loanProductId, final String loanProductName,
            final String loanProductDescription, final boolean isLoanProductLinkedToFloatingRate, final Long fundId, final String fundName,
            final Long loanPurposeId, final String loanPurposeName, final Long loanOfficerId, final String loanOfficerName,
            final CurrencyData currencyData, final BigDecimal proposedPrincipal, final BigDecimal principal,
            final BigDecimal approvedPrincipal, final BigDecimal totalOverpaid, final BigDecimal inArrearsTolerance,
            final Integer termFrequency, final EnumOptionData termPeriodFrequencyType, final Integer numberOfRepayments,
            final Integer repaymentEvery, final EnumOptionData repaymentFrequencyType, EnumOptionData repaymentFrequencyNthDayType,
            EnumOptionData repaymentFrequencyDayOfWeekType, final Long transactionStrategyId, final String transactionStrategyName,
            final EnumOptionData amortizationType, final BigDecimal interestRatePerPeriod, final EnumOptionData interestRateFrequencyType,
            final BigDecimal annualInterestRate, final EnumOptionData interestType, final boolean isFloatingInterestRate,
            final BigDecimal interestRateDifferential, final EnumOptionData interestCalculationPeriodType,
            Boolean allowPartialPeriodInterestCalcualtion, final LocalDate expectedFirstRepaymentOnDate,
            final Integer graceOnPrincipalPayment, final Integer recurringMoratoriumOnPrincipalPeriods, final Integer graceOnInterestPayment, final Integer graceOnInterestCharged,
            final LocalDate interestChargedFromDate, final LoanApplicationTimelineData timeline, final LoanSummaryData loanSummary,
            final BigDecimal feeChargesDueAtDisbursementCharged, final Boolean syncDisbursementWithMeeting, final Integer loancounter,
            final Integer loanProductCounter, final Boolean multiDisburseLoan, Boolean canDefineInstallmentAmount,
            final BigDecimal fixedEmiAmont, final BigDecimal outstandingLoanBalance, final Boolean inArrears,
            final Integer graceOnArrearsAgeing, final Boolean isNPA, final EnumOptionData daysInMonthType,
            final EnumOptionData daysInYearType, final boolean isInterestRecalculationEnabled,
            final LoanInterestRecalculationData interestRecalculationData, final Boolean createStandingInstructionAtDisbursement,
            final Boolean isVariableInstallmentsAllowed, Integer minimumGap, Integer maximumGap, final EnumOptionData subStatus,
            final boolean canUseForTopup, final boolean isTopup, final Long closureLoanId, final String closureLoanAccountNo,
            final BigDecimal topupAmount, final boolean isEqualAmortization) {

        final LoanScheduleData repaymentSchedule = null;
        final Collection<LoanTransactionData> transactions = null;
        final Collection<LoanChargeData> charges = null;
        final Collection<CollateralData> collateral = null;
        final Collection<GuarantorData> guarantors = null;
        final Collection<NoteData> notes = null;
        final CalendarData calendarData = null;
        final Collection<LoanProductData> productOptions = null;
        final Collection<EnumOptionData> termFrequencyTypeOptions = null;
        final Collection<EnumOptionData> repaymentFrequencyTypeOptions = null;
        final Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions = null;
        final Collection<EnumOptionData> repaymentFrequencyDayOfWeekTypeOptions = null;
        final Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = null;
        final Collection<EnumOptionData> interestRateFrequencyTypeOptions = null;
        final Collection<EnumOptionData> amortizationTypeOptions = null;
        final Collection<EnumOptionData> interestTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationPeriodTypeOptions = null;
        final Collection<FundData> fundOptions = null;
        final Collection<ChargeData> chargeOptions = null;
        final ChargeData chargeTemplate = null;
        final Collection<StaffData> loanOfficerOptions = null;
        final Collection<CodeValueData> loanPurposeOptions = null;
        final Collection<CodeValueData> loanCollateralOptions = null;
        final Collection<CalendarData> calendarOptions = null;
        final Collection<PortfolioAccountData> accountLinkingOptions = null;
        final PortfolioAccountData linkedAccount = null;
        final Collection<DisbursementData> disbursementData = null;
        final Collection<LoanTermVariationsData> emiAmountVariations = null;
        final Map<Long, LoanBorrowerCycleData> memberVariations = null;
        final LoanProductData product = null;
        final Collection<ChargeData> overdueCharges = null;
        final LoanScheduleData originalSchedule = null;
        final PaidInAdvanceData paidInAdvance = null;
        final Collection<InterestRatePeriodData> interestRatesPeriods = null;
        final Collection<LoanAccountSummaryData> clientActiveLoanOptions = null;

        return new LoanAccountData(id, accountNo, status, externalId, clientId, clientAccountNo, clientName, clientOfficeId, group,
                loanType, loanProductId, loanProductName, loanProductDescription, isLoanProductLinkedToFloatingRate, fundId, fundName,
                loanPurposeId, loanPurposeName, loanOfficerId, loanOfficerName, currencyData, proposedPrincipal, principal,
                approvedPrincipal, totalOverpaid, inArrearsTolerance, termFrequency, termPeriodFrequencyType, numberOfRepayments,
                repaymentEvery, repaymentFrequencyType, repaymentFrequencyNthDayType, repaymentFrequencyDayOfWeekType,
                transactionStrategyId, transactionStrategyName, amortizationType, interestRatePerPeriod, interestRateFrequencyType,
                annualInterestRate, interestType, isFloatingInterestRate, interestRateDifferential, interestCalculationPeriodType,
                allowPartialPeriodInterestCalcualtion, expectedFirstRepaymentOnDate, graceOnPrincipalPayment, recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment,
                graceOnInterestCharged, interestChargedFromDate, timeline, loanSummary, feeChargesDueAtDisbursementCharged,
                repaymentSchedule, transactions, charges, collateral, guarantors, calendarData, productOptions, termFrequencyTypeOptions,
                repaymentFrequencyTypeOptions, repaymentFrequencyNthDayTypeOptions, repaymentFrequencyDayOfWeekTypeOptions,
                repaymentStrategyOptions, interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions,
                interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, chargeTemplate, loanOfficerOptions, loanPurposeOptions,
                loanCollateralOptions, calendarOptions, syncDisbursementWithMeeting, loancounter, loanProductCounter, notes,
                accountLinkingOptions, linkedAccount, disbursementData, multiDisburseLoan, canDefineInstallmentAmount, fixedEmiAmont,
                outstandingLoanBalance, emiAmountVariations, memberVariations, product, inArrears, graceOnArrearsAgeing, overdueCharges,
                isNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled, interestRecalculationData, originalSchedule,
                createStandingInstructionAtDisbursement, paidInAdvance, interestRatesPeriods, isVariableInstallmentsAllowed, minimumGap,
                maximumGap, subStatus, canUseForTopup, clientActiveLoanOptions, isTopup, closureLoanId, closureLoanAccountNo, topupAmount, isEqualAmortization);
    }

    /*
     * Used to combine the associations and template data on top of exist loan
     * account data
     */
    public static LoanAccountData associationsAndTemplate(final LoanAccountData acc, final LoanScheduleData repaymentSchedule,
            final Collection<LoanTransactionData> transactions, final Collection<LoanChargeData> charges,
            final Collection<CollateralData> collateral, final Collection<GuarantorData> guarantors, final CalendarData calendarData,
            final Collection<LoanProductData> productOptions, final Collection<EnumOptionData> termFrequencyTypeOptions,
            final Collection<EnumOptionData> repaymentFrequencyTypeOptions,
            final Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions,
            final Collection<EnumOptionData> repaymentFrequencyDayOfWeekTypeOptions,
            final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions,
            final Collection<EnumOptionData> interestRateFrequencyTypeOptions, final Collection<EnumOptionData> amortizationTypeOptions,
            final Collection<EnumOptionData> interestTypeOptions, final Collection<EnumOptionData> interestCalculationPeriodTypeOptions,
            final Collection<FundData> fundOptions, final Collection<ChargeData> chargeOptions, final ChargeData chargeTemplate,
            final Collection<StaffData> loanOfficerOptions, final Collection<CodeValueData> loanPurposeOptions,
            final Collection<CodeValueData> loanCollateralOptions, final Collection<CalendarData> calendarOptions,
            final Collection<NoteData> notes, final Collection<PortfolioAccountData> accountLinkingOptions,
            final PortfolioAccountData linkedAccount, final Collection<DisbursementData> disbursementDetails,
            final Collection<LoanTermVariationsData> emiAmountVariations, final Collection<ChargeData> overdueCharges,
            final PaidInAdvanceData paidInAdvance, Collection<InterestRatePeriodData> interestRatesPeriods,
            final Collection<LoanAccountSummaryData> clientActiveLoanOptions) {
        LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;
        if (acc.product != null) {
            loanProductConfigurableAttributes = acc.product.getloanProductConfigurableAttributes();
        }

        return new LoanAccountData(acc.id, acc.accountNo, acc.status, acc.externalId, acc.clientId, acc.clientAccountNo, acc.clientName,
                acc.clientOfficeId, acc.group, acc.loanType, acc.loanProductId, acc.loanProductName, acc.loanProductDescription,
                acc.isLoanProductLinkedToFloatingRate, acc.fundId, acc.fundName, acc.loanPurposeId, acc.loanPurposeName, acc.loanOfficerId,
                acc.loanOfficerName, acc.currency, acc.proposedPrincipal, acc.principal, acc.approvedPrincipal, acc.totalOverpaid,
                acc.inArrearsTolerance, acc.termFrequency, acc.termPeriodFrequencyType, acc.numberOfRepayments, acc.repaymentEvery,
                acc.repaymentFrequencyType, acc.repaymentFrequencyNthDayType, acc.repaymentFrequencyDayOfWeekType,
                acc.transactionProcessingStrategyId, acc.transactionProcessingStrategyName, acc.amortizationType,
                acc.interestRatePerPeriod, acc.interestRateFrequencyType, acc.annualInterestRate, acc.interestType,
                acc.isFloatingInterestRate, acc.interestRateDifferential, acc.interestCalculationPeriodType,
                acc.allowPartialPeriodInterestCalcualtion, acc.expectedFirstRepaymentOnDate, acc.graceOnPrincipalPayment, acc.recurringMoratoriumOnPrincipalPeriods,
                acc.graceOnInterestPayment, acc.graceOnInterestCharged, acc.interestChargedFromDate, acc.timeline, acc.summary,
                acc.feeChargesAtDisbursementCharged, repaymentSchedule, transactions, charges, collateral, guarantors, calendarData,
                productOptions, termFrequencyTypeOptions, repaymentFrequencyTypeOptions, repaymentFrequencyNthDayTypeOptions,
                repaymentFrequencyDayOfWeekTypeOptions, transactionProcessingStrategyOptions, interestRateFrequencyTypeOptions,
                amortizationTypeOptions, interestTypeOptions, interestCalculationPeriodTypeOptions, fundOptions, chargeOptions,
                chargeTemplate, loanOfficerOptions, loanPurposeOptions, loanCollateralOptions, calendarOptions,
                acc.syncDisbursementWithMeeting, acc.loanCounter, acc.loanProductCounter, notes, accountLinkingOptions, linkedAccount,
                disbursementDetails, acc.multiDisburseLoan, acc.canDefineInstallmentAmount, acc.fixedEmiAmount,
                acc.maxOutstandingLoanBalance, emiAmountVariations, acc.memberVariations, acc.product, acc.inArrears,
                acc.graceOnArrearsAgeing, overdueCharges, acc.isNPA, acc.daysInMonthType, acc.daysInYearType,
                acc.isInterestRecalculationEnabled, acc.interestRecalculationData, acc.originalSchedule,
                acc.createStandingInstructionAtDisbursement, paidInAdvance, interestRatesPeriods, acc.isVariableInstallmentsAllowed,
                acc.minimumGap, acc.maximumGap, acc.subStatus, acc.canUseForTopup, clientActiveLoanOptions, acc.isTopup,
                acc.closureLoanId, acc.closureLoanAccountNo, acc.topupAmount, acc.isEqualAmortization);
    }

    public static LoanAccountData associationsAndTemplate(final LoanAccountData acc, final Collection<LoanProductData> productOptions,
            final Collection<StaffData> allowedLoanOfficers, final Collection<CalendarData> calendarOptions,
            final Collection<PortfolioAccountData> accountLinkingOptions) {
        return associationsAndTemplate(acc, acc.repaymentSchedule, acc.transactions, acc.charges, acc.collateral, acc.guarantors,
                acc.meeting, productOptions, acc.termFrequencyTypeOptions, acc.repaymentFrequencyTypeOptions,
                acc.repaymentFrequencyNthDayTypeOptions, acc.repaymentFrequencyDaysOfWeekTypeOptions,
                acc.transactionProcessingStrategyOptions, acc.interestRateFrequencyTypeOptions, acc.amortizationTypeOptions,
                acc.interestTypeOptions, acc.interestCalculationPeriodTypeOptions, acc.fundOptions, acc.chargeOptions, null,
                allowedLoanOfficers, acc.loanPurposeOptions, acc.loanCollateralOptions, calendarOptions, acc.notes, accountLinkingOptions,
                acc.linkedAccount, acc.disbursementDetails, acc.emiAmountVariations, acc.overdueCharges, acc.paidInAdvance,
                acc.interestRatesPeriods, acc.clientActiveLoanOptions);
    }

    public static LoanAccountData associateGroup(final LoanAccountData acc, final GroupGeneralData group) {

        return new LoanAccountData(acc.id, acc.accountNo, acc.status, acc.externalId, acc.clientId, acc.clientAccountNo, acc.clientName,
                acc.clientOfficeId, group, acc.loanType, acc.loanProductId, acc.loanProductName, acc.loanProductDescription,
                acc.isLoanProductLinkedToFloatingRate, acc.fundId, acc.fundName, acc.loanPurposeId, acc.loanPurposeName, acc.loanOfficerId,
                acc.loanOfficerName, acc.currency, acc.proposedPrincipal, acc.principal, acc.approvedPrincipal, acc.totalOverpaid,
                acc.inArrearsTolerance, acc.termFrequency, acc.termPeriodFrequencyType, acc.numberOfRepayments, acc.repaymentEvery,
                acc.repaymentFrequencyType, acc.repaymentFrequencyNthDayType, acc.repaymentFrequencyDayOfWeekType,
                acc.transactionProcessingStrategyId, acc.transactionProcessingStrategyName, acc.amortizationType,
                acc.interestRatePerPeriod, acc.interestRateFrequencyType, acc.annualInterestRate, acc.interestType,
                acc.isFloatingInterestRate, acc.interestRateDifferential, acc.interestCalculationPeriodType,
                acc.allowPartialPeriodInterestCalcualtion, acc.expectedFirstRepaymentOnDate, acc.graceOnPrincipalPayment, acc.recurringMoratoriumOnPrincipalPeriods,
                acc.graceOnInterestPayment, acc.graceOnInterestCharged, acc.interestChargedFromDate, acc.timeline, acc.summary,
                acc.feeChargesAtDisbursementCharged, acc.repaymentSchedule, acc.transactions, acc.charges, acc.collateral, acc.guarantors,
                acc.meeting, acc.productOptions, acc.termFrequencyTypeOptions, acc.repaymentFrequencyTypeOptions,
                acc.repaymentFrequencyNthDayTypeOptions, acc.repaymentFrequencyDaysOfWeekTypeOptions,
                acc.transactionProcessingStrategyOptions, acc.interestRateFrequencyTypeOptions, acc.amortizationTypeOptions,
                acc.interestTypeOptions, acc.interestCalculationPeriodTypeOptions, acc.fundOptions, acc.chargeOptions, null,
                acc.loanOfficerOptions, acc.loanPurposeOptions, acc.loanCollateralOptions, acc.calendarOptions,
                acc.syncDisbursementWithMeeting, acc.loanCounter, acc.loanProductCounter, acc.notes, acc.accountLinkingOptions,
                acc.linkedAccount, acc.disbursementDetails, acc.multiDisburseLoan, acc.canDefineInstallmentAmount, acc.fixedEmiAmount,
                acc.maxOutstandingLoanBalance, acc.emiAmountVariations, acc.memberVariations, acc.product, acc.inArrears,
                acc.graceOnArrearsAgeing, acc.overdueCharges, acc.isNPA, acc.daysInMonthType, acc.daysInYearType,
                acc.isInterestRecalculationEnabled, acc.interestRecalculationData, acc.originalSchedule,
                acc.createStandingInstructionAtDisbursement, acc.paidInAdvance, acc.interestRatesPeriods,
                acc.isVariableInstallmentsAllowed, acc.minimumGap, acc.maximumGap, acc.subStatus, acc.canUseForTopup,
                acc.clientActiveLoanOptions, acc.isTopup, acc.closureLoanId, acc.closureLoanAccountNo, acc.topupAmount, acc.isEqualAmortization);
    }

    public static LoanAccountData associateMemberVariations(final LoanAccountData acc, final Map<Long, Integer> memberLoanCycle) {

        final Map<Long, LoanBorrowerCycleData> memberVariations = new HashMap<Long, LoanBorrowerCycleData>();
        for (Map.Entry<Long, Integer> mapEntry : memberLoanCycle.entrySet()) {
            BigDecimal principal = null;
            BigDecimal interestRatePerPeriod = null;
            Integer numberOfRepayments = null;
            Long clientId = mapEntry.getKey();
            Integer loanCycleNumber = mapEntry.getValue();
            if (acc.product.useBorrowerCycle() && loanCycleNumber != null && loanCycleNumber > 0) {
                Collection<LoanProductBorrowerCycleVariationData> principalVariationsForBorrowerCycle = acc.product
                        .getPrincipalVariationsForBorrowerCycle();
                Collection<LoanProductBorrowerCycleVariationData> interestForVariationsForBorrowerCycle = acc.product
                        .getInterestRateVariationsForBorrowerCycle();
                Collection<LoanProductBorrowerCycleVariationData> repaymentVariationsForBorrowerCycle = acc.product
                        .getNumberOfRepaymentVariationsForBorrowerCycle();
                principal = fetchLoanCycleDefaultValue(principalVariationsForBorrowerCycle, loanCycleNumber);
                interestRatePerPeriod = fetchLoanCycleDefaultValue(interestForVariationsForBorrowerCycle, loanCycleNumber);
                BigDecimal numberofRepaymentval = fetchLoanCycleDefaultValue(repaymentVariationsForBorrowerCycle, loanCycleNumber);
                if (numberofRepaymentval != null) {
                    numberOfRepayments = numberofRepaymentval.intValue();
                }
            }
            if (principal == null) {
                principal = acc.product.getPrincipal();
            }
            if (interestRatePerPeriod == null) {
                interestRatePerPeriod = acc.product.getInterestRatePerPeriod();
            }
            if (numberOfRepayments == null) {
                numberOfRepayments = acc.product.getNumberOfRepayments();
            }
            final Integer termFrequency = numberOfRepayments * acc.product.getRepaymentEvery();
            LoanBorrowerCycleData borrowerCycleData = new LoanBorrowerCycleData(principal, interestRatePerPeriod, numberOfRepayments,
                    termFrequency);
            memberVariations.put(clientId, borrowerCycleData);
        }

        return new LoanAccountData(acc.id, acc.accountNo, acc.status, acc.externalId, acc.clientId, acc.clientAccountNo, acc.clientName,
                acc.clientOfficeId, acc.group, acc.loanType, acc.loanProductId, acc.loanProductName, acc.loanProductDescription,
                acc.isLoanProductLinkedToFloatingRate, acc.fundId, acc.fundName, acc.loanPurposeId, acc.loanPurposeName, acc.loanOfficerId,
                acc.loanOfficerName, acc.currency, acc.proposedPrincipal, acc.principal, acc.approvedPrincipal, acc.totalOverpaid,
                acc.inArrearsTolerance, acc.termFrequency, acc.termPeriodFrequencyType, acc.numberOfRepayments, acc.repaymentEvery,
                acc.repaymentFrequencyType, acc.repaymentFrequencyNthDayType, acc.repaymentFrequencyDayOfWeekType,
                acc.transactionProcessingStrategyId, acc.transactionProcessingStrategyName, acc.amortizationType,
                acc.interestRatePerPeriod, acc.interestRateFrequencyType, acc.annualInterestRate, acc.interestType,
                acc.isFloatingInterestRate, acc.interestRateDifferential, acc.interestCalculationPeriodType,
                acc.allowPartialPeriodInterestCalcualtion, acc.expectedFirstRepaymentOnDate, acc.graceOnPrincipalPayment, acc.recurringMoratoriumOnPrincipalPeriods,
                acc.graceOnInterestPayment, acc.graceOnInterestCharged, acc.interestChargedFromDate, acc.timeline, acc.summary,
                acc.feeChargesAtDisbursementCharged, acc.repaymentSchedule, acc.transactions, acc.charges, acc.collateral, acc.guarantors,
                acc.meeting, acc.productOptions, acc.termFrequencyTypeOptions, acc.repaymentFrequencyTypeOptions,
                acc.repaymentFrequencyNthDayTypeOptions, acc.repaymentFrequencyDaysOfWeekTypeOptions,
                acc.transactionProcessingStrategyOptions, acc.interestRateFrequencyTypeOptions, acc.amortizationTypeOptions,
                acc.interestTypeOptions, acc.interestCalculationPeriodTypeOptions, acc.fundOptions, acc.chargeOptions, null,
                acc.loanOfficerOptions, acc.loanPurposeOptions, acc.loanCollateralOptions, acc.calendarOptions,
                acc.syncDisbursementWithMeeting, acc.loanCounter, acc.loanProductCounter, acc.notes, acc.accountLinkingOptions,
                acc.linkedAccount, acc.disbursementDetails, acc.multiDisburseLoan, acc.canDefineInstallmentAmount, acc.fixedEmiAmount,
                acc.maxOutstandingLoanBalance, acc.emiAmountVariations, memberVariations, acc.product, acc.inArrears,
                acc.graceOnArrearsAgeing, acc.overdueCharges, acc.isNPA, acc.daysInMonthType, acc.daysInYearType,
                acc.isInterestRecalculationEnabled, acc.interestRecalculationData, acc.originalSchedule,
                acc.createStandingInstructionAtDisbursement, acc.paidInAdvance, acc.interestRatesPeriods,
                acc.isVariableInstallmentsAllowed, acc.minimumGap, acc.maximumGap, acc.subStatus, acc.canUseForTopup,
                acc.clientActiveLoanOptions, acc.isTopup, acc.closureLoanId, acc.closureLoanAccountNo, acc.topupAmount, acc.isEqualAmortization);

    }

    public static LoanAccountData withInterestRecalculationCalendarData(final LoanAccountData acc, final CalendarData calendarData,
            final CalendarData compoundingCalendarData) {

        final LoanInterestRecalculationData interestRecalculationData = LoanInterestRecalculationData.withCalendarData(
                acc.interestRecalculationData, calendarData, compoundingCalendarData);

        return new LoanAccountData(acc.id, acc.accountNo, acc.status, acc.externalId, acc.clientId, acc.clientAccountNo, acc.clientName,
                acc.clientOfficeId, acc.group, acc.loanType, acc.loanProductId, acc.loanProductName, acc.loanProductDescription,
                acc.isLoanProductLinkedToFloatingRate, acc.fundId, acc.fundName, acc.loanPurposeId, acc.loanPurposeName, acc.loanOfficerId,
                acc.loanOfficerName, acc.currency, acc.proposedPrincipal, acc.principal, acc.approvedPrincipal, acc.totalOverpaid,
                acc.inArrearsTolerance, acc.termFrequency, acc.termPeriodFrequencyType, acc.numberOfRepayments, acc.repaymentEvery,
                acc.repaymentFrequencyType, acc.repaymentFrequencyNthDayType, acc.repaymentFrequencyDayOfWeekType,
                acc.transactionProcessingStrategyId, acc.transactionProcessingStrategyName, acc.amortizationType,
                acc.interestRatePerPeriod, acc.interestRateFrequencyType, acc.annualInterestRate, acc.interestType,
                acc.isFloatingInterestRate, acc.interestRateDifferential, acc.interestCalculationPeriodType,
                acc.allowPartialPeriodInterestCalcualtion, acc.expectedFirstRepaymentOnDate, acc.graceOnPrincipalPayment, acc.recurringMoratoriumOnPrincipalPeriods,
                acc.graceOnInterestPayment, acc.graceOnInterestCharged, acc.interestChargedFromDate, acc.timeline, acc.summary,
                acc.feeChargesAtDisbursementCharged, acc.repaymentSchedule, acc.transactions, acc.charges, acc.collateral, acc.guarantors,
                acc.meeting, acc.productOptions, acc.termFrequencyTypeOptions, acc.repaymentFrequencyTypeOptions,
                acc.repaymentFrequencyNthDayTypeOptions, acc.repaymentFrequencyDaysOfWeekTypeOptions,
                acc.transactionProcessingStrategyOptions, acc.interestRateFrequencyTypeOptions, acc.amortizationTypeOptions,
                acc.interestTypeOptions, acc.interestCalculationPeriodTypeOptions, acc.fundOptions, acc.chargeOptions, null,
                acc.loanOfficerOptions, acc.loanPurposeOptions, acc.loanCollateralOptions, acc.calendarOptions,
                acc.syncDisbursementWithMeeting, acc.loanCounter, acc.loanProductCounter, acc.notes, acc.accountLinkingOptions,
                acc.linkedAccount, acc.disbursementDetails, acc.multiDisburseLoan, acc.canDefineInstallmentAmount, acc.fixedEmiAmount,
                acc.maxOutstandingLoanBalance, acc.emiAmountVariations, acc.memberVariations, acc.product, acc.inArrears,
                acc.graceOnArrearsAgeing, acc.overdueCharges, acc.isNPA, acc.daysInMonthType, acc.daysInYearType,
                acc.isInterestRecalculationEnabled, interestRecalculationData, acc.originalSchedule,
                acc.createStandingInstructionAtDisbursement, acc.paidInAdvance, acc.interestRatesPeriods,
                acc.isVariableInstallmentsAllowed, acc.minimumGap, acc.maximumGap, acc.subStatus, acc.canUseForTopup,
                acc.clientActiveLoanOptions, acc.isTopup, acc.closureLoanId, acc.closureLoanAccountNo, acc.topupAmount, acc.isEqualAmortization);
    }

    public static LoanAccountData withLoanCalendarData(final LoanAccountData acc, final CalendarData calendarData) {
        return new LoanAccountData(acc.id, acc.accountNo, acc.status, acc.externalId, acc.clientId, acc.clientAccountNo, acc.clientName,
                acc.clientOfficeId, acc.group, acc.loanType, acc.loanProductId, acc.loanProductName, acc.loanProductDescription,
                acc.isLoanProductLinkedToFloatingRate, acc.fundId, acc.fundName, acc.loanPurposeId, acc.loanPurposeName, acc.loanOfficerId,
                acc.loanOfficerName, acc.currency, acc.proposedPrincipal, acc.principal, acc.approvedPrincipal, acc.totalOverpaid,
                acc.inArrearsTolerance, acc.termFrequency, acc.termPeriodFrequencyType, acc.numberOfRepayments, acc.repaymentEvery,
                acc.repaymentFrequencyType, calendarData.getRepeatsOnNthDayOfMonth(), calendarData.getRepeatsOnDay(),
                acc.transactionProcessingStrategyId, acc.transactionProcessingStrategyName, acc.amortizationType,
                acc.interestRatePerPeriod, acc.interestRateFrequencyType, acc.annualInterestRate, acc.interestType,
                acc.isFloatingInterestRate, acc.interestRateDifferential, acc.interestCalculationPeriodType,
                acc.allowPartialPeriodInterestCalcualtion, acc.expectedFirstRepaymentOnDate, acc.graceOnPrincipalPayment,
                acc.recurringMoratoriumOnPrincipalPeriods, acc.graceOnInterestPayment, acc.graceOnInterestCharged,
                acc.interestChargedFromDate, acc.timeline, acc.summary, acc.feeChargesAtDisbursementCharged, acc.repaymentSchedule,
                acc.transactions, acc.charges, acc.collateral, acc.guarantors, acc.meeting, acc.productOptions,
                acc.termFrequencyTypeOptions, acc.repaymentFrequencyTypeOptions, acc.repaymentFrequencyNthDayTypeOptions,
                acc.repaymentFrequencyDaysOfWeekTypeOptions, acc.transactionProcessingStrategyOptions,
                acc.interestRateFrequencyTypeOptions, acc.amortizationTypeOptions, acc.interestTypeOptions,
                acc.interestCalculationPeriodTypeOptions, acc.fundOptions, acc.chargeOptions, null, acc.loanOfficerOptions,
                acc.loanPurposeOptions, acc.loanCollateralOptions, acc.calendarOptions, acc.syncDisbursementWithMeeting, acc.loanCounter,
                acc.loanProductCounter, acc.notes, acc.accountLinkingOptions, acc.linkedAccount, acc.disbursementDetails,
                acc.multiDisburseLoan, acc.canDefineInstallmentAmount, acc.fixedEmiAmount, acc.maxOutstandingLoanBalance,
                acc.emiAmountVariations, acc.memberVariations, acc.product, acc.inArrears, acc.graceOnArrearsAgeing, acc.overdueCharges,
                acc.isNPA, acc.daysInMonthType, acc.daysInYearType, acc.isInterestRecalculationEnabled, acc.interestRecalculationData,
                acc.originalSchedule, acc.createStandingInstructionAtDisbursement, acc.paidInAdvance, acc.interestRatesPeriods,
                acc.isVariableInstallmentsAllowed, acc.minimumGap, acc.maximumGap, acc.subStatus, acc.canUseForTopup,
                acc.clientActiveLoanOptions, acc.isTopup, acc.closureLoanId, acc.closureLoanAccountNo, acc.topupAmount, acc.isEqualAmortization);
    }

    public static LoanAccountData withOriginalSchedule(final LoanAccountData acc, final LoanScheduleData originalSchedule) {

        return new LoanAccountData(acc.id, acc.accountNo, acc.status, acc.externalId, acc.clientId, acc.clientAccountNo, acc.clientName,
                acc.clientOfficeId, acc.group, acc.loanType, acc.loanProductId, acc.loanProductName, acc.loanProductDescription,
                acc.isLoanProductLinkedToFloatingRate, acc.fundId, acc.fundName, acc.loanPurposeId, acc.loanPurposeName, acc.loanOfficerId,
                acc.loanOfficerName, acc.currency, acc.proposedPrincipal, acc.principal, acc.approvedPrincipal, acc.totalOverpaid,
                acc.inArrearsTolerance, acc.termFrequency, acc.termPeriodFrequencyType, acc.numberOfRepayments, acc.repaymentEvery,
                acc.repaymentFrequencyType, acc.repaymentFrequencyNthDayType, acc.repaymentFrequencyDayOfWeekType,
                acc.transactionProcessingStrategyId, acc.transactionProcessingStrategyName, acc.amortizationType,
                acc.interestRatePerPeriod, acc.interestRateFrequencyType, acc.annualInterestRate, acc.interestType,
                acc.isFloatingInterestRate, acc.interestRateDifferential, acc.interestCalculationPeriodType,
                acc.allowPartialPeriodInterestCalcualtion, acc.expectedFirstRepaymentOnDate, acc.graceOnPrincipalPayment, acc.recurringMoratoriumOnPrincipalPeriods,
                acc.graceOnInterestPayment, acc.graceOnInterestCharged, acc.interestChargedFromDate, acc.timeline, acc.summary,
                acc.feeChargesAtDisbursementCharged, acc.repaymentSchedule, acc.transactions, acc.charges, acc.collateral, acc.guarantors,
                acc.meeting, acc.productOptions, acc.termFrequencyTypeOptions, acc.repaymentFrequencyTypeOptions,
                acc.repaymentFrequencyNthDayTypeOptions, acc.repaymentFrequencyDaysOfWeekTypeOptions,
                acc.transactionProcessingStrategyOptions, acc.interestRateFrequencyTypeOptions, acc.amortizationTypeOptions,
                acc.interestTypeOptions, acc.interestCalculationPeriodTypeOptions, acc.fundOptions, acc.chargeOptions, null,
                acc.loanOfficerOptions, acc.loanPurposeOptions, acc.loanCollateralOptions, acc.calendarOptions,
                acc.syncDisbursementWithMeeting, acc.loanCounter, acc.loanProductCounter, acc.notes, acc.accountLinkingOptions,
                acc.linkedAccount, acc.disbursementDetails, acc.multiDisburseLoan, acc.canDefineInstallmentAmount, acc.fixedEmiAmount,
                acc.maxOutstandingLoanBalance, acc.emiAmountVariations, acc.memberVariations, acc.product, acc.inArrears,
                acc.graceOnArrearsAgeing, acc.overdueCharges, acc.isNPA, acc.daysInMonthType, acc.daysInYearType,
                acc.isInterestRecalculationEnabled, acc.interestRecalculationData, originalSchedule,
                acc.createStandingInstructionAtDisbursement, acc.paidInAdvance, acc.interestRatesPeriods,
                acc.isVariableInstallmentsAllowed, acc.minimumGap, acc.maximumGap, acc.subStatus, acc.canUseForTopup,
                acc.clientActiveLoanOptions, acc.isTopup, acc.closureLoanId, acc.closureLoanAccountNo, acc.topupAmount, acc.isEqualAmortization);
    }

    private LoanAccountData(final Long id, //
            final String accountNo, //
            final LoanStatusEnumData status, //
            final String externalId, //
            final Long clientId, final String clientAccountNo, final String clientName, final Long clientOfficeId, //
            final GroupGeneralData group, final EnumOptionData loanType, final Long loanProductId, final String loanProductName,
            final String loanProductDescription, //
            final boolean isLoanProductLinkedToFloatingRate, final Long fundId, final String fundName, final Long loanPurposeId,
            final String loanPurposeName, //
            final Long loanOfficerId, final String loanOfficerName, //
            final CurrencyData currency, BigDecimal proposedPrincipal, final BigDecimal principal, final BigDecimal approvedPrincipal,
            final BigDecimal totalOverpaid, //
            final BigDecimal inArrearsTolerance, final Integer termFrequency, //
            final EnumOptionData termPeriodFrequencyType, final Integer numberOfRepayments, final Integer repaymentEvery,
            final EnumOptionData repaymentFrequencyType, //
            final EnumOptionData repaymentFrequencyNthDayType, final EnumOptionData repaymentFrequencyDayOfWeekType,
            final Long transactionProcessingStrategyId, final String transactionProcessingStrategyName,
            final EnumOptionData amortizationType, final BigDecimal interestRatePerPeriod, final EnumOptionData interestRateFrequencyType,
            final BigDecimal annualInterestRate, final EnumOptionData interestType, final boolean isFloatingInterestRate,
            final BigDecimal interestRateDifferential, final EnumOptionData interestCalculationPeriodType,
            final Boolean allowPartialPeriodInterestCalcualtion, final LocalDate expectedFirstRepaymentOnDate,
            final Integer graceOnPrincipalPayment, final Integer recurringMoratoriumOnPrincipalPeriods, final Integer graceOnInterestPayment, final Integer graceOnInterestCharged,
            final LocalDate interestChargedFromDate, final LoanApplicationTimelineData timeline, final LoanSummaryData summary, final BigDecimal feeChargesDueAtDisbursementCharged,
            final LoanScheduleData repaymentSchedule, final Collection<LoanTransactionData> transactions, final Collection<LoanChargeData> charges,
            final Collection<CollateralData> collateral, final Collection<GuarantorData> guarantors, final CalendarData meeting, final Collection<LoanProductData> productOptions,
            final Collection<EnumOptionData> termFrequencyTypeOptions, final Collection<EnumOptionData> repaymentFrequencyTypeOptions,
            final Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions,
            final Collection<EnumOptionData> repaymentFrequencyDaysOfWeekTypeOptions,
            final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions,
            final Collection<EnumOptionData> interestRateFrequencyTypeOptions, final Collection<EnumOptionData> amortizationTypeOptions,
            final Collection<EnumOptionData> interestTypeOptions, final Collection<EnumOptionData> interestCalculationPeriodTypeOptions,
            final Collection<FundData> fundOptions, final Collection<ChargeData> chargeOptions, final ChargeData chargeTemplate,
            final Collection<StaffData> loanOfficerOptions, final Collection<CodeValueData> loanPurposeOptions,
            final Collection<CodeValueData> loanCollateralOptions, final Collection<CalendarData> calendarOptions,
            final Boolean syncDisbursementWithMeeting, final Integer loanCounter, final Integer loanProductCounter,
            final Collection<NoteData> notes, final Collection<PortfolioAccountData> accountLinkingOptions,
            final PortfolioAccountData linkedAccount, final Collection<DisbursementData> disbursementDetails,
            final Boolean multiDisburseLoan, final Boolean canDefineInstallmentAmount, BigDecimal fixedEmiAmount,
            final BigDecimal maxOutstandingLoanBalance, final Collection<LoanTermVariationsData> emiAmountVariations,
            final Map<Long, LoanBorrowerCycleData> memberVariations, final LoanProductData product, final Boolean inArrears,
            final Integer graceOnArrearsAgeing, final Collection<ChargeData> overdueCharges, final Boolean isNPA,
            final EnumOptionData daysInMonthType, final EnumOptionData daysInYearType, final boolean isInterestRecalculationEnabled,
            final LoanInterestRecalculationData interestRecalculationData, final LoanScheduleData originalSchedule,
            final Boolean createStandingInstructionAtDisbursement, final PaidInAdvanceData paidInAdvance,
            final Collection<InterestRatePeriodData> interestRatesPeriods, final Boolean isVariableInstallmentsAllowed,
            final Integer minimumGap, final Integer maximumGap, final EnumOptionData subStatus, final Boolean canUseForTopup,
            final Collection<LoanAccountSummaryData> clientActiveLoanOptions, final boolean isTopup,
            final Long closureLoanId, final String closureLoanAccountNo, final BigDecimal topupAmount, final boolean isEqualAmortization) {

        this.id = id;
        this.accountNo = accountNo;
        this.status = status;
        this.subStatus = subStatus;
        this.externalId = externalId;
        this.clientId = clientId;
        this.clientAccountNo = clientAccountNo;
        this.clientName = clientName;
        this.clientOfficeId = clientOfficeId;
        this.group = group;
        this.loanType = loanType;
        this.loanProductId = loanProductId;
        this.loanProductName = loanProductName;
        this.loanProductDescription = loanProductDescription;
        this.isLoanProductLinkedToFloatingRate = isLoanProductLinkedToFloatingRate;
        this.fundId = fundId;
        this.fundName = fundName;
        this.loanPurposeId = loanPurposeId;
        this.loanPurposeName = loanPurposeName;
        this.loanOfficerId = loanOfficerId;
        this.loanOfficerName = loanOfficerName;
        this.currency = currency;
        this.proposedPrincipal = proposedPrincipal;
        this.principal = principal;
        this.approvedPrincipal = approvedPrincipal;
        this.totalOverpaid = totalOverpaid;
        this.inArrearsTolerance = inArrearsTolerance;
        this.termFrequency = termFrequency;
        this.termPeriodFrequencyType = termPeriodFrequencyType;
        this.numberOfRepayments = numberOfRepayments;
        this.repaymentEvery = repaymentEvery;
        this.repaymentFrequencyType = repaymentFrequencyType;
        this.repaymentFrequencyNthDayType = repaymentFrequencyNthDayType;
        this.repaymentFrequencyDayOfWeekType = repaymentFrequencyDayOfWeekType;
        this.transactionProcessingStrategyId = transactionProcessingStrategyId;
        this.transactionProcessingStrategyName = transactionProcessingStrategyName;
        this.amortizationType = amortizationType;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.interestRateFrequencyType = interestRateFrequencyType;
        this.annualInterestRate = annualInterestRate;
        this.interestType = interestType;
        this.isFloatingInterestRate = isFloatingInterestRate;
        this.interestRateDifferential = interestRateDifferential;
        this.interestCalculationPeriodType = interestCalculationPeriodType;
        this.allowPartialPeriodInterestCalcualtion = allowPartialPeriodInterestCalcualtion;
        this.expectedFirstRepaymentOnDate = expectedFirstRepaymentOnDate;
        this.graceOnPrincipalPayment = graceOnPrincipalPayment;
        this.recurringMoratoriumOnPrincipalPeriods = recurringMoratoriumOnPrincipalPeriods;
        
        this.graceOnInterestPayment = graceOnInterestPayment;
        this.graceOnInterestCharged = graceOnInterestCharged;
        this.interestChargedFromDate = interestChargedFromDate;
        this.timeline = timeline;
        this.feeChargesAtDisbursementCharged = feeChargesDueAtDisbursementCharged;
        this.syncDisbursementWithMeeting = syncDisbursementWithMeeting;

        // totals
        this.summary = summary;

        // associations
        this.repaymentSchedule = repaymentSchedule;
        this.transactions = transactions;
        this.charges = charges;
        this.collateral = collateral;
        this.guarantors = guarantors;
        this.meeting = meeting;
        this.notes = notes;

        // template
        this.productOptions = productOptions;
        this.termFrequencyTypeOptions = termFrequencyTypeOptions;
        this.repaymentFrequencyTypeOptions = repaymentFrequencyTypeOptions;
        this.repaymentFrequencyNthDayTypeOptions = repaymentFrequencyNthDayTypeOptions;
        this.repaymentFrequencyDaysOfWeekTypeOptions = repaymentFrequencyDaysOfWeekTypeOptions;
        this.interestRateFrequencyTypeOptions = interestRateFrequencyTypeOptions;
        this.amortizationTypeOptions = amortizationTypeOptions;
        this.interestTypeOptions = interestTypeOptions;
        this.interestCalculationPeriodTypeOptions = interestCalculationPeriodTypeOptions;

        if (CollectionUtils.isEmpty(transactionProcessingStrategyOptions)) {
            this.transactionProcessingStrategyOptions = null;
        } else {
            this.transactionProcessingStrategyOptions = transactionProcessingStrategyOptions;
        }

        if (CollectionUtils.isEmpty(fundOptions)) {
            this.fundOptions = null;
        } else {
            this.fundOptions = fundOptions;
        }

        if (CollectionUtils.isEmpty(chargeOptions)) {
            this.chargeOptions = null;
        } else {
            this.chargeOptions = chargeOptions;
        }

        if (CollectionUtils.isEmpty(loanOfficerOptions)) {
            this.loanOfficerOptions = null;
        } else {
            this.loanOfficerOptions = loanOfficerOptions;
        }

        if (CollectionUtils.isEmpty(loanPurposeOptions)) {
            this.loanPurposeOptions = null;
        } else {
            this.loanPurposeOptions = loanPurposeOptions;
        }

        if (CollectionUtils.isEmpty(loanCollateralOptions)) {
            this.loanCollateralOptions = null;
        } else {
            this.loanCollateralOptions = loanCollateralOptions;
        }

        if (CollectionUtils.isEmpty(calendarOptions)) {
            this.calendarOptions = null;
        } else {
            this.calendarOptions = calendarOptions;
        }

        this.loanCounter = loanCounter;
        this.loanProductCounter = loanProductCounter;

        this.linkedAccount = linkedAccount;
        this.accountLinkingOptions = accountLinkingOptions;
        this.disbursementDetails = disbursementDetails;
        this.multiDisburseLoan = multiDisburseLoan;

        Boolean canDefineEMIAmount = canDefineInstallmentAmount;
        if (canDefineInstallmentAmount == null) {
            canDefineEMIAmount = multiDisburseLoan;
        } else if (multiDisburseLoan != null) {
            canDefineEMIAmount = canDefineInstallmentAmount || multiDisburseLoan;
        }
        this.canDefineInstallmentAmount = canDefineEMIAmount;
        this.fixedEmiAmount = fixedEmiAmount;
        this.maxOutstandingLoanBalance = maxOutstandingLoanBalance;

        if (this.status != null && LoanStatus.fromInt(this.status.id().intValue()).isApproved()) {
            this.canDisburse = true;
        } else {
            boolean canDisburse = false;
            if (this.multiDisburseLoan != null && this.multiDisburseLoan && this.disbursementDetails != null) {
                for (DisbursementData disbursementData : this.disbursementDetails) {
                    if (!disbursementData.isDisbursed()) {
                        canDisburse = true;
                    }
                }
            }
            this.canDisburse = canDisburse;
        }
        this.emiAmountVariations = emiAmountVariations;
        this.memberVariations = memberVariations;
        this.product = product;
        this.inArrears = inArrears;
        this.graceOnArrearsAgeing = graceOnArrearsAgeing;
        this.overdueCharges = overdueCharges;
        this.isNPA = isNPA;

        this.daysInMonthType = daysInMonthType;
        this.daysInYearType = daysInYearType;
        this.isInterestRecalculationEnabled = isInterestRecalculationEnabled;
        this.interestRecalculationData = interestRecalculationData;
        this.originalSchedule = originalSchedule;
        this.createStandingInstructionAtDisbursement = createStandingInstructionAtDisbursement;
        this.paidInAdvance = paidInAdvance;
        if (this.product != null) {
            this.product.setloanProductConfigurableAttributes(product.getloanProductConfigurableAttributes());
        }
        this.interestRatesPeriods = interestRatesPeriods;
        this.isVariableInstallmentsAllowed = isVariableInstallmentsAllowed;
        this.minimumGap = minimumGap;
        this.maximumGap = maximumGap;
        this.canUseForTopup = canUseForTopup;
        this.clientActiveLoanOptions = clientActiveLoanOptions;
        this.isTopup = isTopup;
        this.closureLoanId = closureLoanId;
        this.closureLoanAccountNo = closureLoanAccountNo;
        this.topupAmount = topupAmount;
        this.isEqualAmortization = isEqualAmortization;

    }

    public RepaymentScheduleRelatedLoanData repaymentScheduleRelatedData() {
        return this.timeline.repaymentScheduleRelatedData(this.currency, this.principal, this.approvedPrincipal, this.inArrearsTolerance,
                this.feeChargesAtDisbursementCharged);
    }

    public Long officeId() {
        Long officeId = this.clientOfficeId;
        if (officeId == null) {
            officeId = groupOfficeId();
        }
        return officeId;
    }

    public Long loanOfficerId() {
        return this.loanOfficerId;
    }

    public Collection<LoanChargeData> charges() {
        return this.charges;
    }

    public Long groupOfficeId() {
        return this.group == null ? null : this.group.officeId();
    }

    public Long groupId() {
        return this.group == null ? null : this.group.getId();
    }

    public CurrencyData currency() {
        return this.currency;
    }

    public Long clientId() {
        return this.clientId;
    }

    private static BigDecimal fetchLoanCycleDefaultValue(Collection<LoanProductBorrowerCycleVariationData> borrowerCycleVariationData,
            Integer loanCycleNumber) {
        BigDecimal defaultValue = null;
        Integer cycleNumberSelected = 0;
        for (LoanProductBorrowerCycleVariationData data : borrowerCycleVariationData) {
            if (isLoanCycleValuesWhenConditionEqual(loanCycleNumber, data)
                    || isLoanCycleValuesWhenConditionGreterthan(loanCycleNumber, cycleNumberSelected, data)) {
                cycleNumberSelected = data.getBorrowerCycleNumber();
                defaultValue = data.getDefaultValue();
            }
        }

        return defaultValue;
    }

    private static boolean isLoanCycleValuesWhenConditionGreterthan(Integer loanCycleNumber, Integer cycleNumberSelected,
            LoanProductBorrowerCycleVariationData data) {
        return data.getBorrowerCycleNumber() < loanCycleNumber
                && data.getValueConditionType().equals(LoanProductValueConditionType.GREATERTHAN)
                && cycleNumberSelected < data.getBorrowerCycleNumber();
    }

    private static boolean isLoanCycleValuesWhenConditionEqual(Integer loanCycleNumber, LoanProductBorrowerCycleVariationData data) {
        return data.getBorrowerCycleNumber().equals(loanCycleNumber)
                && data.getValueConditionType().equals(LoanProductValueConditionType.EQUAL);
    }

    public LoanProductData product() {
        return this.product;
    }

    public void setProduct(LoanProductData product) {
        this.product = product;
    }

    public GroupGeneralData groupData() {
        return this.group;
    }

    public Long loanProductId() {
        return this.loanProductId;
    }

    public BigDecimal getTotalOutstandingAmount() {
        return this.summary.getTotalOutstanding();
    }

    public boolean isInterestRecalculationEnabled() {
        return this.isInterestRecalculationEnabled;
    }

    public Long getInterestRecalculationDetailId() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getId(); }
        return null;
    }

    public boolean isActive() {
        return LoanStatus.fromInt(this.status.id().intValue()).isActive();
    }

    public boolean isMultiDisburseLoan() {
        return multiDisburseLoan;
    }

    public BigDecimal getTotalPaidFeeCharges() {
        if (this.summary != null) return this.summary.getTotalPaidFeeCharges();
        return BigDecimal.ZERO;
    }

    public boolean isMonthlyRepaymentFrequencyType() {
        return (this.repaymentFrequencyType.getId().intValue() == PeriodFrequencyType.MONTHS.getValue());
    }

    /**
     * Used to produce a {@link LoanAccountData} with only collateral options for now.
     * 
     * @return {@link LoanAccountData} object
     */
    public static LoanAccountData emptyTemplate() {
        final Collection<CodeValueData> loanCollateralOptions = null;
        
        return LoanAccountData.collateralTemplate(loanCollateralOptions);
    }
    
    public boolean isLoanProductLinkedToFloatingRate() {
        return this.isLoanProductLinkedToFloatingRate;
    }
    
    public LocalDate getDisbursementDate(){
        return this.timeline.getDisbursementDate();
    }

    
    public boolean isFloatingInterestRate() {
        return this.isFloatingInterestRate;
    }

    
    public BigDecimal getInterestRateDifferential() {
        return this.interestRateDifferential;
    }

    public void setDatatables(final List<DatatableData> datatables) {
            this.datatables = datatables;
    }

    public String getStatusStringValue(){
        return this.status.value();
    }
}
