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

import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.util.ConvertChargeDataToSpecificChargeData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.floatingrates.data.InterestRatePeriodData;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductBorrowerCycleVariationData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductValueConditionType;
import org.apache.fineract.portfolio.note.data.NoteData;
import org.apache.fineract.portfolio.rate.data.RateData;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@SuppressWarnings("ObjectToString")
public class LoanAccountData {

    // basic loan details

    // identity
    private Long id;
    private String accountNo;
    private ExternalId externalId = ExternalId.empty();

    // status
    private LoanStatusEnumData status;
    private EnumOptionData subStatus;

    // related to
    private Long clientId;
    private String clientAccountNo;
    private String clientName;
    private ExternalId clientExternalId;
    private Long clientOfficeId;
    private GroupGeneralData group;
    private Long loanProductId;
    private String loanProductName;
    private String loanProductDescription;
    // TODO: avoid prefix "is"
    private boolean isLoanProductLinkedToFloatingRate;
    private Long fundId;
    private String fundName;
    private Long loanPurposeId;
    private String loanPurposeName;
    private Long loanOfficerId;
    private String loanOfficerName;
    private EnumOptionData loanType;

    // terms
    private CurrencyData currency;
    private BigDecimal principal;
    private BigDecimal approvedPrincipal;
    private BigDecimal proposedPrincipal;
    private BigDecimal netDisbursalAmount;

    private Integer termFrequency;
    private EnumOptionData termPeriodFrequencyType;
    private Integer numberOfRepayments;
    private Integer repaymentEvery;
    private Integer fixedLength;
    private EnumOptionData repaymentFrequencyType;
    private EnumOptionData repaymentFrequencyNthDayType;
    private EnumOptionData repaymentFrequencyDayOfWeekType;
    private BigDecimal interestRatePerPeriod;
    private EnumOptionData interestRateFrequencyType;
    private BigDecimal annualInterestRate;
    // TODO: avoid prefix "is"
    private boolean isFloatingInterestRate;
    private BigDecimal interestRateDifferential;

    // settings
    private EnumOptionData amortizationType;
    private EnumOptionData interestType;
    private EnumOptionData interestCalculationPeriodType;
    private Boolean allowPartialPeriodInterestCalculation;
    private BigDecimal inArrearsTolerance;
    private String transactionProcessingStrategyCode;
    private String transactionProcessingStrategyName;
    private Integer graceOnPrincipalPayment;
    private Integer recurringMoratoriumOnPrincipalPeriods;
    private Integer graceOnInterestPayment;
    private Integer graceOnInterestCharged;
    private Integer graceOnArrearsAgeing;
    private LocalDate interestChargedFromDate;
    private LocalDate expectedFirstRepaymentOnDate;
    private Boolean syncDisbursementWithMeeting;
    private Boolean disallowExpectedDisbursements;

    // timeline
    private LoanApplicationTimelineData timeline;

    // totals
    private LoanSummaryData summary;

    // associations
    private LoanScheduleData repaymentSchedule;
    private Collection<LoanTransactionData> transactions;
    private Collection<LoanChargeData> charges;
    private Collection<LoanCollateralManagementData> collateral;
    private Collection<GuarantorData> guarantors;
    private CalendarData meeting;
    private Collection<NoteData> notes;
    private Collection<DisbursementData> disbursementDetails;
    private LoanScheduleData originalSchedule;
    // template
    private Collection<LoanProductData> productOptions;
    private Collection<StaffData> loanOfficerOptions;
    private Collection<CodeValueData> loanPurposeOptions;
    private Collection<FundData> fundOptions;
    private Collection<EnumOptionData> termFrequencyTypeOptions;
    private Collection<EnumOptionData> repaymentFrequencyTypeOptions;
    private Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions;
    private Collection<EnumOptionData> repaymentFrequencyDaysOfWeekTypeOptions;

    private Collection<EnumOptionData> interestRateFrequencyTypeOptions;
    private Collection<EnumOptionData> amortizationTypeOptions;
    private Collection<EnumOptionData> interestTypeOptions;
    private Collection<EnumOptionData> interestCalculationPeriodTypeOptions;
    private Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions;
    private Collection<ChargeData> chargeOptions;
    private Collection<CodeValueData> loanCollateralOptions;
    private Collection<CalendarData> calendarOptions;
    private List<EnumOptionData> loanScheduleTypeOptions;
    private List<EnumOptionData> loanScheduleProcessingTypeOptions;

    @Transient
    private BigDecimal feeChargesAtDisbursementCharged;
    private BigDecimal totalOverpaid;

    // loanCycle
    private Integer loanCounter;
    private Integer loanProductCounter;

    // linkable account details
    private PortfolioAccountData linkedAccount;
    private Collection<PortfolioAccountData> accountLinkingOptions;

    private Boolean multiDisburseLoan;

    private Boolean canDefineInstallmentAmount;

    private BigDecimal fixedEmiAmount;

    private BigDecimal maxOutstandingLoanBalance;

    private Boolean canDisburse;

    private Collection<LoanTermVariationsData> emiAmountVariations;
    private Collection<LoanAccountSummaryData> clientActiveLoanOptions;
    private Boolean canUseForTopup;
    // TODO: avoid prefix "is"
    private boolean isTopup;
    private boolean fraud;
    private Long closureLoanId;
    private String closureLoanAccountNo;
    private BigDecimal topupAmount;

    private LoanProductData product;

    private Map<Long, LoanBorrowerCycleData> memberVariations;

    private Boolean inArrears;
    // TODO: avoid prefix "is"
    private Boolean isNPA;
    private Collection<ChargeData> overdueCharges;

    private EnumOptionData daysInMonthType;
    private EnumOptionData daysInYearType;
    // TODO: avoid prefix "is"
    private boolean isInterestRecalculationEnabled;

    private LoanInterestRecalculationData interestRecalculationData;
    private Boolean createStandingInstructionAtDisbursement;

    // Paid In Advance
    private PaidInAdvanceData paidInAdvance;

    private Collection<InterestRatePeriodData> interestRatesPeriods;

    // VariableInstallments
    // TODO: avoid prefix "is"
    private Boolean isVariableInstallmentsAllowed;
    private Integer minimumGap;
    private Integer maximumGap;

    private List<DatatableData> datatables;
    // TODO: avoid prefix "is"
    private Boolean isEqualAmortization;
    private BigDecimal fixedPrincipalPercentagePerInstallment;

    // Rate
    private List<RateData> rates;
    // TODO: avoid prefix "is"
    private Boolean isRatesEnabled;

    // import fields
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

    private LocalDate overpaidOnDate;
    private CollectionData delinquent;
    private DelinquencyRangeData delinquencyRange;
    private Boolean enableInstallmentLevelDelinquency;
    private LocalDate lastClosedBusinessDate;
    private Boolean chargedOff;

    private Boolean enableDownPayment;
    private BigDecimal disbursedAmountPercentageForDownPayment;
    private Boolean enableAutoRepaymentForDownPayment;

    private EnumOptionData loanScheduleType;
    private EnumOptionData loanScheduleProcessingType;

    public static LoanAccountData importInstanceIndividual(EnumOptionData loanTypeEnumOption, Long clientId, Long productId,
            Long loanOfficerId, LocalDate submittedOnDate, Long fundId, BigDecimal principal, Integer numberOfRepayments,
            Integer repaymentEvery, EnumOptionData repaidEveryFrequencyEnums, Integer loanTermFrequency,
            EnumOptionData loanTermFrequencyTypeEnum, BigDecimal nominalInterestRate, LocalDate expectedDisbursementDate,
            EnumOptionData amortizationEnumOption, EnumOptionData interestMethodEnum, EnumOptionData interestCalculationPeriodTypeEnum,
            BigDecimal inArrearsTolerance, String transactionProcessingStrategyCode, Integer graceOnPrincipalPayment,
            Integer graceOnInterestPayment, Integer graceOnInterestCharged, LocalDate interestChargedFromDate,
            LocalDate repaymentsStartingFromDate, Integer rowIndex, ExternalId externalId, Long groupId, Collection<LoanChargeData> charges,
            String linkAccountId, String locale, String dateFormat, List<LoanCollateralManagementData> loanCollateralManagementData,
            Integer fixedLength) {

        return new LoanAccountData().setLoanType(loanTypeEnumOption).setClientId(clientId).setProductId(productId)
                .setLoanOfficerId(loanOfficerId).setSubmittedOnDate(submittedOnDate).setFundId(fundId).setPrincipal(principal)
                .setNumberOfRepayments(numberOfRepayments).setRepaymentEvery(repaymentEvery)
                .setRepaymentFrequencyType(repaidEveryFrequencyEnums).setLoanTermFrequency(loanTermFrequency)
                .setLoanTermFrequencyType(loanTermFrequencyTypeEnum).setInterestRatePerPeriod(nominalInterestRate)
                .setExpectedDisbursementDate(expectedDisbursementDate).setAmortizationType(amortizationEnumOption)
                .setInterestType(interestMethodEnum).setInterestCalculationPeriodType(interestCalculationPeriodTypeEnum)
                .setInArrearsTolerance(inArrearsTolerance).setTransactionProcessingStrategyCode(transactionProcessingStrategyCode)
                .setGraceOnPrincipalPayment(graceOnPrincipalPayment).setGraceOnInterestPayment(graceOnInterestPayment)
                .setGraceOnInterestCharged(graceOnInterestCharged).setInterestChargedFromDate(interestChargedFromDate)
                .setRepaymentsStartingFromDate(repaymentsStartingFromDate).setRowIndex(rowIndex).setExternalId(externalId)
                .setGroupId(groupId).setCharges(charges).setLinkAccountId(linkAccountId).setLocale(locale).setDateFormat(dateFormat)
                .setCollateral(loanCollateralManagementData).setFixedLength(fixedLength);
    }

    public static LoanAccountData importInstanceGroup(EnumOptionData loanTypeEnumOption, Long groupIdforGroupLoan, Long productId,
            Long loanOfficerId, LocalDate submittedOnDate, Long fundId, BigDecimal principal, Integer numberOfRepayments,
            Integer repaidEvery, EnumOptionData repaidEveryFrequencyEnums, Integer loanTermFrequency,
            EnumOptionData loanTermFrequencyTypeEnum, BigDecimal nominalInterestRate, EnumOptionData amortizationEnumOption,
            EnumOptionData interestMethodEnum, EnumOptionData interestCalculationPeriodEnum, BigDecimal arrearsTolerance,
            String transactionProcessingStrategyCode, Integer graceOnPrincipalPayment, Integer graceOnInterestPayment,
            Integer graceOnInterestCharged, LocalDate interestChargedFromDate, LocalDate repaymentsStartingFromDate, Integer rowIndex,
            ExternalId externalId, String linkAccountId, String locale, String dateFormat, Integer fixedLength) {

        return new LoanAccountData().setLoanType(loanTypeEnumOption).setGroupId(groupIdforGroupLoan).setProductId(productId)
                .setLoanOfficerId(loanOfficerId).setSubmittedOnDate(submittedOnDate).setFundId(fundId).setPrincipal(principal)
                .setNumberOfRepayments(numberOfRepayments).setRepaymentEvery(repaidEvery)
                .setRepaymentFrequencyType(repaidEveryFrequencyEnums).setLoanTermFrequency(loanTermFrequency)
                .setLoanTermFrequencyType(loanTermFrequencyTypeEnum).setInterestRatePerPeriod(nominalInterestRate)
                .setAmortizationTypeOptions(List.of(amortizationEnumOption)).setInterestType(interestMethodEnum)
                .setInterestCalculationPeriodType(interestCalculationPeriodEnum).setInArrearsTolerance(arrearsTolerance)
                .setTransactionProcessingStrategyCode(transactionProcessingStrategyCode).setGraceOnPrincipalPayment(graceOnPrincipalPayment)
                .setGraceOnInterestPayment(graceOnInterestPayment).setGraceOnInterestCharged(graceOnInterestCharged)
                .setInterestChargedFromDate(interestChargedFromDate).setRepaymentsStartingFromDate(repaymentsStartingFromDate)
                .setRowIndex(rowIndex).setExternalId(externalId).setLinkAccountId(linkAccountId).setLocale(locale).setDateFormat(dateFormat)
                .setFixedLength(fixedLength);
    }

    public LoanAccountData withClientData(final ClientData clientData) {
        return this.setClientId(clientData.getId()) //
                .setClientAccountNo(clientData.getAccountNo()) //
                .setClientName(clientData.getDisplayName()) //
                .setClientOfficeId(clientData.getOfficeId()) //
                .setClientExternalId(clientData.getExternalId()); //
    }

    public LoanAccountData withExpectedDisbursementDate(final LocalDate expectedDisbursementDate) {
        if (getTimeline() == null) {
            setTimeline(new LoanApplicationTimelineData());
        }
        this.getTimeline().setExpectedDisbursementDate(expectedDisbursementDate);
        return this.setExpectedDisbursementDate(expectedDisbursementDate);
    }

    public LoanAccountData withProductData(final LoanProductData product, final Integer loanCycleNumber) {

        final EnumOptionData termPeriodFrequencyType = product.getRepaymentFrequencyType();

        final Collection<LoanChargeData> charges = new ArrayList<LoanChargeData>();
        for (final ChargeData charge : product.charges()) {
            if (!charge.isOverdueInstallmentCharge()) {
                charges.add(ConvertChargeDataToSpecificChargeData.toLoanChargeData(charge));
            }
        }

        BigDecimal principal = null;
        BigDecimal proposedPrincipal = null;
        BigDecimal interestRatePerPeriod = null;

        Integer numberOfRepayments = null;
        if (product.isUseBorrowerCycle() && loanCycleNumber != null && loanCycleNumber > 0) {
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

        // Add net get net disbursal amount from charges and principal
        BigDecimal netDisbursalAmount = principal;

        if (!charges.isEmpty()) {
            for (LoanChargeData charge : charges) {
                netDisbursalAmount = netDisbursalAmount.subtract(charge.getAmount());
            }
        }

        if (interestRatePerPeriod == null) {
            interestRatePerPeriod = product.getInterestRatePerPeriod();
        }
        if (numberOfRepayments == null) {
            numberOfRepayments = product.getNumberOfRepayments();
        }

        return this.setProductId(product.getId()).setLoanProductName(product.getName()).setLoanProductDescription(product.getDescription())
                .setLoanProductLinkedToFloatingRate(product.isLinkedToFloatingInterestRates()).setFundId(product.getFundId())
                .setFundName(product.getFundName()).setCurrency(product.getCurrency()).setProposedPrincipal(proposedPrincipal)
                .setPrincipal(principal).setApprovedPrincipal(principal).setNetDisbursalAmount(netDisbursalAmount)
                .setInArrearsTolerance(product.getInArrearsTolerance()).setTermFrequency(numberOfRepayments * product.getRepaymentEvery())
                .setTermPeriodFrequencyType(termPeriodFrequencyType).setNumberOfRepayments(numberOfRepayments)
                .setRepaymentEvery(product.getRepaymentEvery()).setRepaymentFrequencyType(product.getRepaymentFrequencyType())
                .setTransactionProcessingStrategyCode(product.getTransactionProcessingStrategyCode())
                .setAmortizationType(product.getAmortizationType()).setInterestRatePerPeriod(interestRatePerPeriod)
                .setInterestRateFrequencyType(product.getInterestRateFrequencyType()).setAnnualInterestRate(product.getAnnualInterestRate())
                .setInterestType(product.getInterestType()).setFloatingInterestRate(product.isFloatingInterestRateCalculationAllowed())
                .setInterestRateDifferential(product.getDefaultDifferentialLendingRate())
                .setInterestCalculationPeriodType(product.getInterestCalculationPeriodType())
                .setAllowPartialPeriodInterestCalculation(product.isAllowPartialPeriodInterestCalculation())
                .setGraceOnPrincipalPayment(product.getGraceOnPrincipalPayment())
                .setRecurringMoratoriumOnPrincipalPeriods(product.getRecurringMoratoriumOnPrincipalPeriods())
                .setGraceOnInterestPayment(product.getGraceOnInterestPayment())
                .setGraceOnInterestCharged(product.getGraceOnInterestCharged()).setCharges(charges)
                .setMultiDisburseLoan(product.getMultiDisburseLoan()).setCanDefineInstallmentAmount(product.isCanDefineInstallmentAmount())
                .setMaxOutstandingLoanBalance(product.getOutstandingLoanBalance()).setProduct(product)
                .setGraceOnArrearsAgeing(product.getGraceOnArrearsAgeing()).setOverdueCharges(product.overdueFeeCharges())
                .setDaysInMonthType(product.getDaysInMonthType()).setDaysInYearType(product.getDaysInYearType())
                .setInterestRecalculationEnabled(product.isInterestRecalculationEnabled())
                .setInterestRecalculationData(product.toLoanInterestRecalculationData())
                .setIsVariableInstallmentsAllowed(product.isAllowVariableInstallments()).setMinimumGap(product.getMinimumGap())
                .setMaximumGap(product.getMaximumGap()).setTopup(product.isCanUseForTopup())
                .setIsEqualAmortization(product.isEqualAmortization())
                .setFixedPrincipalPercentagePerInstallment(product.getFixedPrincipalPercentagePerInstallment())
                .setDelinquent(CollectionData.template()).setDisallowExpectedDisbursements(product.getDisallowExpectedDisbursements())
                .setLoanScheduleType(product.getLoanScheduleType()).setLoanScheduleProcessingType(product.getLoanScheduleProcessingType());
    }

    /*
     * Used to send back loan account data with the basic details coming from query.
     */
    public static LoanAccountData basicLoanDetails(final Long id, final String accountNo, final LoanStatusEnumData status,
            final ExternalId externalId, final Long clientId, final String clientAccountNo, final String clientName,
            final Long clientOfficeId, final ExternalId clientExternalId, final GroupGeneralData group, final EnumOptionData loanType,
            final Long loanProductId, final String loanProductName, final String loanProductDescription,
            final boolean isLoanProductLinkedToFloatingRate, final Long fundId, final String fundName, final Long loanPurposeId,
            final String loanPurposeName, final Long loanOfficerId, final String loanOfficerName, final CurrencyData currencyData,
            final BigDecimal proposedPrincipal, final BigDecimal principal, final BigDecimal approvedPrincipal,
            final BigDecimal netDisbursalAmount, final BigDecimal totalOverpaid, final BigDecimal inArrearsTolerance,
            final Integer termFrequency, final EnumOptionData termPeriodFrequencyType, final Integer numberOfRepayments,
            final Integer repaymentEvery, final EnumOptionData repaymentFrequencyType, EnumOptionData repaymentFrequencyNthDayType,
            EnumOptionData repaymentFrequencyDayOfWeekType, final String transactionStrategy, final String transactionStrategyName,
            final EnumOptionData amortizationType, final BigDecimal interestRatePerPeriod, final EnumOptionData interestRateFrequencyType,
            final BigDecimal annualInterestRate, final EnumOptionData interestType, final boolean isFloatingInterestRate,
            final BigDecimal interestRateDifferential, final EnumOptionData interestCalculationPeriodType,
            Boolean allowPartialPeriodInterestCalcualtion, final LocalDate expectedFirstRepaymentOnDate,
            final Integer graceOnPrincipalPayment, final Integer recurringMoratoriumOnPrincipalPeriods,
            final Integer graceOnInterestPayment, final Integer graceOnInterestCharged, final LocalDate interestChargedFromDate,
            final LoanApplicationTimelineData timeline, final LoanSummaryData loanSummary,
            final BigDecimal feeChargesDueAtDisbursementCharged, final Boolean syncDisbursementWithMeeting, final Integer loanCounter,
            final Integer loanProductCounter, final Boolean multiDisburseLoan, Boolean canDefineInstallmentAmount,
            final BigDecimal fixedEmiAmont, final BigDecimal outstandingLoanBalance, final Boolean inArrears,
            final Integer graceOnArrearsAgeing, final Boolean isNPA, final EnumOptionData daysInMonthType,
            final EnumOptionData daysInYearType, final boolean isInterestRecalculationEnabled,
            final LoanInterestRecalculationData interestRecalculationData, final Boolean createStandingInstructionAtDisbursement,
            final Boolean isVariableInstallmentsAllowed, Integer minimumGap, Integer maximumGap, final EnumOptionData subStatus,
            final boolean canUseForTopup, final boolean isTopup, final Long closureLoanId, final String closureLoanAccountNo,
            final BigDecimal topupAmount, final boolean isEqualAmortization, final BigDecimal fixedPrincipalPercentagePerInstallment,
            final DelinquencyRangeData delinquencyRange, final boolean disallowExpectedDisbursements, final boolean fraud,
            LocalDate lastClosedBusinessDate, LocalDate overpaidOnDate, final boolean chargedOff, final boolean enableDownPayment,
            final BigDecimal disbursedAmountPercentageForDownPayment, final boolean enableAutoRepaymentForDownPayment,
            final boolean enableInstallmentLevelDelinquency, final EnumOptionData loanScheduleType,
            final EnumOptionData loanScheduleProcessingType, final Integer fixedLength) {

        final CollectionData delinquent = CollectionData.template();

        return new LoanAccountData().setId(id).setAccountNo(accountNo).setStatus(status).setExternalId(externalId).setClientId(clientId)
                .setClientAccountNo(clientAccountNo).setClientName(clientName).setClientOfficeId(clientOfficeId)
                .setClientExternalId(clientExternalId).setGroup(group).setLoanType(loanType).setLoanProductId(loanProductId)
                .setLoanProductName(loanProductName).setLoanProductDescription(loanProductDescription)
                .setLoanProductLinkedToFloatingRate(isLoanProductLinkedToFloatingRate).setFundId(fundId).setFundName(fundName)
                .setLoanPurposeId(loanPurposeId).setLoanPurposeName(loanPurposeName).setLoanOfficerId(loanOfficerId)
                .setLoanOfficerName(loanOfficerName).setCurrency(currencyData).setProposedPrincipal(proposedPrincipal)
                .setPrincipal(principal).setApprovedPrincipal(approvedPrincipal).setNetDisbursalAmount(netDisbursalAmount)
                .setTotalOverpaid(totalOverpaid).setInArrearsTolerance(inArrearsTolerance).setTermFrequency(termFrequency)
                .setTermPeriodFrequencyType(termPeriodFrequencyType).setNumberOfRepayments(numberOfRepayments)
                .setRepaymentEvery(repaymentEvery).setRepaymentFrequencyType(repaymentFrequencyType)
                .setRepaymentFrequencyNthDayType(repaymentFrequencyNthDayType)
                .setRepaymentFrequencyDayOfWeekType(repaymentFrequencyDayOfWeekType)
                .setTransactionProcessingStrategyCode(transactionStrategy).setTransactionProcessingStrategyName(transactionStrategyName)
                .setAmortizationType(amortizationType).setInterestRatePerPeriod(interestRatePerPeriod)
                .setInterestRateFrequencyType(interestRateFrequencyType).setAnnualInterestRate(annualInterestRate)
                .setInterestType(interestType).setFloatingInterestRate(isFloatingInterestRate)
                .setInterestRateDifferential(interestRateDifferential).setInterestCalculationPeriodType(interestCalculationPeriodType)
                .setAllowPartialPeriodInterestCalculation(allowPartialPeriodInterestCalcualtion)
                .setExpectedFirstRepaymentOnDate(expectedFirstRepaymentOnDate).setGraceOnPrincipalPayment(graceOnPrincipalPayment)
                .setRecurringMoratoriumOnPrincipalPeriods(recurringMoratoriumOnPrincipalPeriods)
                .setGraceOnInterestPayment(graceOnInterestPayment).setGraceOnInterestCharged(graceOnInterestCharged)
                .setInterestChargedFromDate(interestChargedFromDate).setTimeline(timeline).setSummary(loanSummary)
                .setFeeChargesAtDisbursementCharged(feeChargesDueAtDisbursementCharged)
                .setSyncDisbursementWithMeeting(syncDisbursementWithMeeting).setLoanCounter(loanCounter)
                .setLoanProductCounter(loanProductCounter).setMultiDisburseLoan(multiDisburseLoan)
                .setCanDefineInstallmentAmount(canDefineInstallmentAmount).setFixedEmiAmount(fixedEmiAmont)
                .setMaxOutstandingLoanBalance(outstandingLoanBalance).setInArrears(inArrears).setGraceOnArrearsAgeing(graceOnArrearsAgeing)
                .setIsNPA(isNPA).setDaysInMonthType(daysInMonthType).setDaysInYearType(daysInYearType)
                .setInterestRecalculationEnabled(isInterestRecalculationEnabled).setInterestRecalculationData(interestRecalculationData)
                .setCreateStandingInstructionAtDisbursement(createStandingInstructionAtDisbursement)
                .setIsVariableInstallmentsAllowed(isVariableInstallmentsAllowed).setMinimumGap(minimumGap).setMaximumGap(maximumGap)
                .setSubStatus(subStatus).setCanUseForTopup(canUseForTopup).setTopup(isTopup).setClosureLoanId(closureLoanId)
                .setClosureLoanAccountNo(closureLoanAccountNo).setTopupAmount(topupAmount).setIsEqualAmortization(isEqualAmortization)
                .setFixedPrincipalPercentagePerInstallment(fixedPrincipalPercentagePerInstallment).setDelinquent(delinquent)
                .setDelinquencyRange(delinquencyRange).setDisallowExpectedDisbursements(disallowExpectedDisbursements).setFraud(fraud)
                .setLastClosedBusinessDate(lastClosedBusinessDate).setOverpaidOnDate(overpaidOnDate).setChargedOff(chargedOff)
                .setEnableDownPayment(enableDownPayment).setDisbursedAmountPercentageForDownPayment(disbursedAmountPercentageForDownPayment)
                .setEnableAutoRepaymentForDownPayment(enableAutoRepaymentForDownPayment)
                .setEnableInstallmentLevelDelinquency(enableInstallmentLevelDelinquency).setLoanScheduleType(loanScheduleType)
                .setLoanScheduleProcessingType(loanScheduleProcessingType).setFixedLength(fixedLength);
    }

    /*
     * Used to combine the associations and template data on top of exist loan account data
     */
    public LoanAccountData associationsAndTemplate(final LoanScheduleData repaymentSchedule,
            final Collection<LoanTransactionData> transactions, final Collection<LoanChargeData> charges,
            final Collection<LoanCollateralManagementData> collateral, final Collection<GuarantorData> guarantors,
            final CalendarData calendarData, final Collection<LoanProductData> productOptions,
            final Collection<EnumOptionData> termFrequencyTypeOptions, final Collection<EnumOptionData> repaymentFrequencyTypeOptions,
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
            final Collection<LoanAccountSummaryData> clientActiveLoanOptions, final List<RateData> rates, final Boolean isRatesEnabled,
            final CollectionData delinquent, final List<EnumOptionData> loanScheduleTypeOptions,
            final List<EnumOptionData> loanScheduleProcessingTypeOptions) {

        // TODO: why are these variables 'calendarData', 'chargeTemplate' never used (see original private constructor)

        return this.setRepaymentSchedule(repaymentSchedule).setTransactions(transactions).setCharges(charges).setCollateral(collateral)
                .setGuarantors(guarantors).setProductOptions(productOptions).setTermFrequencyTypeOptions(termFrequencyTypeOptions)
                .setRepaymentFrequencyTypeOptions(repaymentFrequencyTypeOptions)
                .setRepaymentFrequencyNthDayTypeOptions(repaymentFrequencyNthDayTypeOptions)
                .setRepaymentFrequencyDaysOfWeekTypeOptions(repaymentFrequencyDayOfWeekTypeOptions)
                .setTransactionProcessingStrategyOptions(transactionProcessingStrategyOptions)
                .setInterestRateFrequencyTypeOptions(interestRateFrequencyTypeOptions).setAmortizationTypeOptions(amortizationTypeOptions)
                .setInterestTypeOptions(interestTypeOptions).setInterestCalculationPeriodTypeOptions(interestCalculationPeriodTypeOptions)
                .setFundOptions(fundOptions).setChargeOptions(chargeOptions).setLoanOfficerOptions(loanOfficerOptions)
                .setLoanPurposeOptions(loanPurposeOptions).setLoanCollateralOptions(loanCollateralOptions)
                // .setMeeting(calendarData)
                .setCalendarOptions(calendarOptions).setNotes(notes).setAccountLinkingOptions(accountLinkingOptions)
                .setLinkedAccount(linkedAccount).setDisbursementDetails(disbursementDetails).setEmiAmountVariations(emiAmountVariations)
                .setOverdueCharges(overdueCharges).setPaidInAdvance(paidInAdvance).setInterestRatesPeriods(interestRatesPeriods)
                .setClientActiveLoanOptions(clientActiveLoanOptions).setRates(rates).setIsRatesEnabled(isRatesEnabled)
                .setDelinquent(delinquent).setLoanScheduleTypeOptions(loanScheduleTypeOptions)
                .setLoanScheduleProcessingTypeOptions(loanScheduleProcessingTypeOptions);
    }

    public LoanAccountData associationsAndTemplate(final Collection<LoanProductData> productOptions,
            final Collection<StaffData> allowedLoanOfficers, final Collection<CalendarData> calendarOptions,
            final Collection<PortfolioAccountData> accountLinkingOptions, final Boolean isRatesEnabled) {
        return this.setProductOptions(productOptions) //
                .setLoanOfficerOptions(allowedLoanOfficers) //
                .setCalendarOptions(calendarOptions) //
                .setAccountLinkingOptions(accountLinkingOptions) //
                .setIsRatesEnabled(isRatesEnabled); //
    }

    public LoanAccountData associateMemberVariations(final Map<Long, Integer> memberLoanCycle) {
        final Map<Long, LoanBorrowerCycleData> memberVariations = new HashMap<>();
        for (Map.Entry<Long, Integer> mapEntry : memberLoanCycle.entrySet()) {
            BigDecimal principal = null;
            BigDecimal interestRatePerPeriod = null;
            Integer numberOfRepayments = null;
            Long clientId = mapEntry.getKey();
            Integer loanCycleNumber = mapEntry.getValue();
            if (product.isUseBorrowerCycle() && loanCycleNumber != null && loanCycleNumber > 0) {
                Collection<LoanProductBorrowerCycleVariationData> principalVariationsForBorrowerCycle = product
                        .getPrincipalVariationsForBorrowerCycle();
                Collection<LoanProductBorrowerCycleVariationData> interestForVariationsForBorrowerCycle = product
                        .getInterestRateVariationsForBorrowerCycle();
                Collection<LoanProductBorrowerCycleVariationData> repaymentVariationsForBorrowerCycle = product
                        .getNumberOfRepaymentVariationsForBorrowerCycle();
                principal = fetchLoanCycleDefaultValue(principalVariationsForBorrowerCycle, loanCycleNumber);
                interestRatePerPeriod = fetchLoanCycleDefaultValue(interestForVariationsForBorrowerCycle, loanCycleNumber);
                BigDecimal numberofRepaymentval = fetchLoanCycleDefaultValue(repaymentVariationsForBorrowerCycle, loanCycleNumber);
                if (numberofRepaymentval != null) {
                    numberOfRepayments = numberofRepaymentval.intValue();
                }
            }
            if (principal == null) {
                principal = product.getPrincipal();
            }
            if (interestRatePerPeriod == null) {
                interestRatePerPeriod = product.getInterestRatePerPeriod();
            }
            if (numberOfRepayments == null) {
                numberOfRepayments = product.getNumberOfRepayments();
            }
            final Integer termFrequency = numberOfRepayments * product.getRepaymentEvery();
            LoanBorrowerCycleData borrowerCycleData = new LoanBorrowerCycleData(principal, interestRatePerPeriod, numberOfRepayments,
                    termFrequency);
            memberVariations.put(clientId, borrowerCycleData);
        }
        return this.setMemberVariations(memberVariations);
    }

    public LoanAccountData withInterestRecalculationCalendarData(final CalendarData calendarData,
            final CalendarData compoundingCalendarData) {
        if (interestRecalculationData == null) {
            interestRecalculationData = new LoanInterestRecalculationData();
        }
        final LoanInterestRecalculationData newInterestRecalculationData = interestRecalculationData.withCalendarData(calendarData,
                compoundingCalendarData);
        return this.setInterestRecalculationData(newInterestRecalculationData);
    }

    public static final Comparator<LoanAccountData> LOAN_ACCOUNT_DATA_COMPARATOR_BY_CLIENT_NAME = (loan1, loan2) -> {
        String clientOfLoan1 = loan1.getClientName().toUpperCase(Locale.ENGLISH);
        String clientOfLoan2 = loan2.getClientName().toUpperCase(Locale.ENGLISH);
        return clientOfLoan1.compareTo(clientOfLoan2);
    };

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
                && data.getLoanProductValueConditionType().equals(LoanProductValueConditionType.GREATERTHAN)
                && cycleNumberSelected < data.getBorrowerCycleNumber();
    }

    private static boolean isLoanCycleValuesWhenConditionEqual(Integer loanCycleNumber, LoanProductBorrowerCycleVariationData data) {
        return data.getBorrowerCycleNumber().equals(loanCycleNumber)
                && data.getLoanProductValueConditionType().equals(LoanProductValueConditionType.EQUAL);
    }

    public Long getInterestRecalculationDetailId() {
        if (isInterestRecalculationEnabled) {
            return this.interestRecalculationData.getId();
        }
        return null;
    }

    public boolean isActive() {
        return LoanStatus.fromInt(getStatus().getId().intValue()).isActive();
    }
}
