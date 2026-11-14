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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.common.service.CommonEnumerations;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanApprovalData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.data.OutstandingAmountsDTO;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeInterestHandlingType;
import org.apache.fineract.portfolio.loanaccount.domain.reamortization.LoanReAmortizationInterestHandlingType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.SemiMonthlyScheduleDates;
import org.apache.fineract.portfolio.loanaccount.repository.LoanBuyDownFeeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanForeclosureValidator;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanTemplateReadPlatformServiceImpl implements LoanTemplateReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final LoanDropdownReadPlatformService loanDropdownReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final PaymentTypeReadService paymentTypeReadPlatformService;
    private final LoanUtilService loanUtilService;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final LoanForeclosureValidator loanForeclosureValidator;
    private final LoanTransactionProcessingService loadTransactionProcessingService;
    private final LoanBalanceService loanBalanceService;
    private final LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository;
    private final LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository;
    private final InterestRefundServiceDelegate interestRefundServiceDelegate;
    private final LoanMaximumAmountCalculator loanMaximumAmountCalculator;
    private final LoanAccountDataReadService loanAccountDataReadService;

    private CurrencyData retriveLoanCurrencyData(final Long loanId) {
        final LoanCurrencyDataMapper loanCurrencyMapper = new LoanCurrencyDataMapper(sqlGenerator);
        final String sql = "select " + loanCurrencyMapper.schema() + " where l.id = ?";

        return this.jdbcTemplate.queryForObject(sql, loanCurrencyMapper, loanId);
    }

    @Override
    public LoanTransactionData retrieveLoanTransactionTemplate(final Long loanId, final LoanTransactionType transactionType,
            final Long transactionId) {

        LoanTransactionData loanTransactionData = null;
        Collection<PaymentTypeData> paymentOptions = null;
        List<CodeValueData> classificationOptions = null;
        BigDecimal transactionAmount = BigDecimal.ZERO;
        switch (transactionType) {
            case CAPITALIZED_INCOME:
                final Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
                BigDecimal capitalizedIncomeBalance = BigDecimal.ZERO;
                if (loan.getLoanProduct().getLoanProductRelatedDetail().isEnableIncomeCapitalization()) {
                    capitalizedIncomeBalance = loanCapitalizedIncomeBalanceRepository.findAllByLoanIdAndDeletedFalseAndClosedFalse(loanId)
                            .stream().map(LoanCapitalizedIncomeBalance::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                }
                transactionAmount = loan.getLoanProduct().isAllowApprovedDisbursedAmountsOverApplied()
                        ? loanMaximumAmountCalculator.getOverAppliedMax(loan)
                        : loan.getApprovedPrincipal();
                transactionAmount = transactionAmount.subtract(loan.getDisbursedAmount()).subtract(capitalizedIncomeBalance);
                paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
                classificationOptions = this.codeValueReadPlatformService
                        .retrieveCodeValuesByCode(LoanTransactionApiConstants.CAPITALIZED_INCOME_CLASSIFICATION_CODE);
                loanTransactionData = LoanTransactionData.loanTransactionDataForCreditTemplate(
                        LoanEnumerations.transactionType(transactionType), DateUtils.getBusinessLocalDate(), transactionAmount,
                        paymentOptions, retriveLoanCurrencyData(loanId), classificationOptions);
            break;
            case BUY_DOWN_FEE:
                paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
                classificationOptions = this.codeValueReadPlatformService
                        .retrieveCodeValuesByCode(LoanTransactionApiConstants.BUY_DOWN_FEE_CLASSIFICATION_CODE);
                loanTransactionData = LoanTransactionData.loanTransactionDataForCreditTemplate(
                        LoanEnumerations.transactionType(transactionType), DateUtils.getBusinessLocalDate(), transactionAmount,
                        paymentOptions, retriveLoanCurrencyData(loanId), classificationOptions);
            break;
            case CAPITALIZED_INCOME_ADJUSTMENT:
                final LoanCapitalizedIncomeBalance loanCapitalizedIncomeBalance = loanCapitalizedIncomeBalanceRepository
                        .findByLoanIdAndLoanTransactionIdAndDeletedFalseAndClosedFalse(loanId, transactionId);

                transactionAmount = (loanCapitalizedIncomeBalance == null) ? BigDecimal.ZERO
                        : loanCapitalizedIncomeBalance.getAmount()
                                .subtract(MathUtil.nullToZero(loanCapitalizedIncomeBalance.getAmountAdjustment()));
                loanTransactionData = LoanTransactionData.loanTransactionDataForCreditTemplate(
                        LoanEnumerations.transactionType(transactionType), DateUtils.getBusinessLocalDate(), transactionAmount,
                        paymentOptions, retriveLoanCurrencyData(loanId), classificationOptions);
            break;
            case BUY_DOWN_FEE_ADJUSTMENT:
                final LoanBuyDownFeeBalance loanBuyDownFeeBalance = loanBuyDownFeeBalanceRepository
                        .findByLoanIdAndLoanTransactionIdAndDeletedFalseAndClosedFalse(loanId, transactionId);

                transactionAmount = (loanBuyDownFeeBalance == null) ? BigDecimal.ZERO
                        : loanBuyDownFeeBalance.getAmount().subtract(MathUtil.nullToZero(loanBuyDownFeeBalance.getAmountAdjustment()));
                loanTransactionData = LoanTransactionData.loanTransactionDataForCreditTemplate(
                        LoanEnumerations.transactionType(transactionType), DateUtils.getBusinessLocalDate(), transactionAmount,
                        paymentOptions, retriveLoanCurrencyData(loanId), classificationOptions);
            break;
            default:
                loanTransactionData = LoanTransactionData.templateOnTop(retrieveLoanTransactionTemplate(loanId),
                        LoanEnumerations.transactionType(transactionType));
            break;
        }

        return loanTransactionData;
    }

    @Override
    public LoanTransactionData retrieveLoanTransactionTemplate(final Long loanId) {
        this.context.authenticatedUser();

        final RepaymentTransactionTemplateMapper mapper = new RepaymentTransactionTemplateMapper(sqlGenerator);
        LoanTransactionData loanTransactionData = this.jdbcTemplate.queryForObject("select " + mapper.schema(), mapper, // NOSONAR
                LoanTransactionType.REPAYMENT.getValue(), LoanTransactionType.DOWN_PAYMENT.getValue(), loanId);
        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        return LoanTransactionData.templateOnTop(loanTransactionData, paymentOptions);
    }

    @Override
    public LoanTransactionData retrieveLoanPrePaymentTemplate(final LoanTransactionType repaymentTransactionType, final Long loanId,
            LocalDate onDate) {
        return retrieveLoanPayOffTemplate(repaymentTransactionType, loanId, onDate, false);
    }

    @Override
    public LoanTransactionData retrieveLoanEarlyTerminationTemplate(final Long loanId, final LocalDate onDate) {
        return retrieveLoanPayOffTemplate(LoanTransactionType.REPAYMENT, loanId, onDate, true);
    }

    private LoanTransactionData retrieveLoanPayOffTemplate(final LoanTransactionType repaymentTransactionType, final Long loanId,
            final LocalDate onDate, final boolean earlyTermination) {

        this.context.authenticatedUser();
        this.loanUtilService.validateRepaymentTransactionType(repaymentTransactionType);

        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final CurrencyData currencyData = applicationCurrency.toData();

        final LocalDate earliestUnpaidInstallmentDate = DateUtils.getBusinessLocalDate();
        final LocalDate recalculateFrom = null;
        final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        final OutstandingAmountsDTO outstandingAmounts = earlyTermination
                ? loadTransactionProcessingService.fetchEarlyTerminationDetail(scheduleGeneratorDTO, onDate, loan)
                : loadTransactionProcessingService.fetchPrepaymentDetail(scheduleGeneratorDTO, onDate, loan);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(repaymentTransactionType);
        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final BigDecimal outstandingLoanBalance = outstandingAmounts.principal().getAmount();
        final BigDecimal unrecognizedIncomePortion = null;

        BigDecimal adjustedChargeAmount = adjustPrepayInstallmentCharge(loan, onDate);
        BigDecimal totalAdjusted = outstandingAmounts.getTotalOutstanding().getAmount().subtract(adjustedChargeAmount);

        return LoanTransactionData.builder().type(transactionType).currency(currencyData).date(earliestUnpaidInstallmentDate)
                .amount(totalAdjusted).netDisbursalAmount(loan.getNetDisbursalAmount())
                .principalPortion(outstandingAmounts.principal().getAmount()).interestPortion(outstandingAmounts.interest().getAmount())
                .feeChargesPortion(outstandingAmounts.feeCharges().getAmount().subtract(adjustedChargeAmount))
                .penaltyChargesPortion(outstandingAmounts.penaltyCharges().getAmount()).unrecognizedIncomePortion(unrecognizedIncomePortion)
                .paymentTypeOptions(paymentOptions).externalId(ExternalId.empty()).outstandingLoanBalance(outstandingLoanBalance)
                .manuallyReversed(false).loanId(loanId).externalLoanId(loan.getExternalId()).build();
    }

    private BigDecimal adjustPrepayInstallmentCharge(Loan loan, final LocalDate onDate) {
        BigDecimal chargeAmount = BigDecimal.ZERO;
        /*
         * for(LoanCharge loanCharge: loan.charges()){ if(loanCharge.isInstalmentFee() &&
         * loanCharge.getCharge().getChargeCalculation()==ChargeCalculationType. FLAT.getValue()){ for
         * (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
         * if(onDate.isBefore(installment.getDueDate())){ LoanInstallmentCharge loanInstallmentCharge =
         * loanCharge.getInstallmentLoanCharge(installment.getInstallmentNumber( )); if(loanInstallmentCharge != null){
         * chargeAmount = chargeAmount.add(loanInstallmentCharge.getAmountOutstanding()); }
         *
         * break; } } } }
         */
        return chargeAmount;
    }

    @Override
    public LoanTransactionData retrieveWaiveInterestDetails(final Long loanId) {

        // TODO - KW -OPTIMIZE - write simple sql query to fetch back overdue
        // interest that can be waived along with the date of repayment period
        // interest is overdue.
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        final CurrencyData currencyData = applicationCurrency.toData();

        final LoanTransaction waiveOfInterest = deriveDefaultInterestWaiverTransaction(loan);

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.WAIVE_INTEREST);

        final BigDecimal amount = waiveOfInterest.getAmount(currency).getAmount();
        final BigDecimal outstandingLoanBalance = null;
        final BigDecimal unrecognizedIncomePortion = null;

        return LoanTransactionData.builder().type(transactionType).currency(currencyData).date(waiveOfInterest.getTransactionDate())
                .amount(amount).netDisbursalAmount(loan.getNetDisbursalAmount()).outstandingLoanBalance(outstandingLoanBalance)
                .unrecognizedIncomePortion(unrecognizedIncomePortion).externalId(ExternalId.empty()).manuallyReversed(false).loanId(loanId)
                .externalLoanId(loan.getExternalId()).build();
    }

    @Override
    public LoanTransactionData retrieveNewClosureDetails() {

        this.context.authenticatedUser();
        final BigDecimal outstandingLoanBalance = null;
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.WRITEOFF);
        final BigDecimal unrecognizedIncomePortion = null;
        return LoanTransactionData.builder().type(transactionType).date(DateUtils.getBusinessLocalDate()).externalId(ExternalId.empty())
                .outstandingLoanBalance(outstandingLoanBalance).unrecognizedIncomePortion(unrecognizedIncomePortion).manuallyReversed(false)
                .build();

    }

    @Override
    public LoanApprovalData retrieveApprovalTemplate(final Long loanId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final ApplicationCurrency appCurrency = applicationCurrencyRepository.findOneWithNotFoundDetection(loan.getCurrency());
        final BigDecimal availableDisbursementAmountWithOverApplied = delinquencyReadPlatformService
                .calculateAvailableDisbursementAmountWithOverApplied(loan);
        return new LoanApprovalData(loan.getProposedPrincipal(), DateUtils.getBusinessLocalDate(), loan.getNetDisbursalAmount(),
                appCurrency.toData(), availableDisbursementAmountWithOverApplied, loan.getExpectedDisbursementDate());
    }

    @Override
    public LoanTransactionData retrieveDisbursalTemplate(final Long loanId, boolean paymentDetailsRequired) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.DISBURSEMENT);
        Collection<PaymentTypeData> paymentOptions = null;
        if (paymentDetailsRequired) {
            paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        }
        final ApplicationCurrency appCurrency = applicationCurrencyRepository.findOneWithNotFoundDetection(loan.getCurrency());
        final BigDecimal availableDisbursementAmountWithOverApplied = delinquencyReadPlatformService
                .calculateAvailableDisbursementAmountWithOverApplied(loan);
        return LoanTransactionData.loanTransactionDataForDisbursalTemplate(transactionType,
                loan.getExpectedDisbursedOnLocalDateForTemplate(), loan.getDisburseAmountForTemplate(), loan.getNetDisbursalAmount(),
                paymentOptions, loan.retriveLastEmiAmount(), loan.getNextPossibleRepaymentDateForRescheduling(), appCurrency.toData(),
                availableDisbursementAmountWithOverApplied);
    }

    @Override
    public LoanAccountData retrieveLoanProductDetailsTemplate(final Long productId, final Long clientId, final Long groupId,
            final Long officeId) {

        this.context.authenticatedUser();

        final LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
        final Collection<EnumOptionData> loanTermFrequencyTypeOptions = this.loanDropdownReadPlatformService
                .retrieveLoanTermFrequencyTypeOptions();
        final Collection<EnumOptionData> repaymentFrequencyTypeOptions = this.loanDropdownReadPlatformService
                .retrieveRepaymentFrequencyTypeOptions();
        final Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions = this.loanDropdownReadPlatformService
                .retrieveRepaymentFrequencyOptionsForNthDayOfMonth();
        final Collection<EnumOptionData> repaymentFrequencyDaysOfWeekTypeOptions = this.loanDropdownReadPlatformService
                .retrieveRepaymentFrequencyOptionsForDaysOfWeek();
        final Collection<EnumOptionData> interestRateFrequencyTypeOptions = this.loanDropdownReadPlatformService
                .retrieveInterestRateFrequencyTypeOptions();
        final Collection<EnumOptionData> amortizationTypeOptions = this.loanDropdownReadPlatformService
                .retrieveLoanAmortizationTypeOptions();
        Collection<EnumOptionData> interestTypeOptions = null;
        if (loanProduct.isLinkedToFloatingInterestRates()) {
            interestTypeOptions = List.of(interestType(InterestMethod.DECLINING_BALANCE));
        } else {
            interestTypeOptions = this.loanDropdownReadPlatformService.retrieveLoanInterestTypeOptions();
        }
        final Collection<EnumOptionData> interestCalculationPeriodTypeOptions = this.loanDropdownReadPlatformService
                .retrieveLoanInterestRateCalculatedInPeriodOptions();
        final Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
        final Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = this.loanDropdownReadPlatformService
                .retrieveTransactionProcessingStrategies();
        final Collection<CodeValueData> loanPurposeOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanPurpose");
        final Collection<CodeValueData> loanCollateralOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode("LoanCollateral");
        Collection<ChargeData> chargeOptions = null;
        if (loanProduct.getMultiDisburseLoan()) {
            chargeOptions = this.chargeReadPlatformService.retrieveLoanProductApplicableCharges(productId,
                    new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT }, officeId);
        } else {
            chargeOptions = this.chargeReadPlatformService.retrieveLoanProductApplicableCharges(productId,
                    new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT, ChargeTimeType.TRANCHE_DISBURSEMENT }, officeId);
        }

        Integer loanCycleCounter = null;
        if (loanProduct.isUseBorrowerCycle()) {
            if (clientId == null) {
                loanCycleCounter = loanAccountDataReadService.retriveLoanCounter(groupId, AccountType.GROUP.getValue(),
                        loanProduct.getId());
            } else {
                loanCycleCounter = loanAccountDataReadService.retriveLoanCounter(clientId, loanProduct.getId());
            }
        }

        Collection<LoanAccountSummaryData> activeLoanOptions = null;
        if (loanProduct.isCanUseForTopup() && clientId != null) {
            activeLoanOptions = this.accountDetailsReadPlatformService.retrieveClientActiveLoanAccountSummary(clientId);
        } else if (loanProduct.isCanUseForTopup() && groupId != null) {
            activeLoanOptions = this.accountDetailsReadPlatformService.retrieveGroupActiveLoanAccountSummary(groupId);
        }

        LoanAccountData loanAccountData = new LoanAccountData().withProductData(loanProduct, loanCycleCounter) //
                .setTermFrequencyTypeOptions(loanTermFrequencyTypeOptions) //
                .setRepaymentFrequencyTypeOptions(repaymentFrequencyTypeOptions) //
                .setRepaymentFrequencyNthDayTypeOptions(repaymentFrequencyNthDayTypeOptions) //
                .setRepaymentFrequencyDaysOfWeekTypeOptions(repaymentFrequencyDaysOfWeekTypeOptions) //
                .setTransactionProcessingStrategyOptions(repaymentStrategyOptions) //
                .setInterestRateFrequencyTypeOptions(interestRateFrequencyTypeOptions) //
                .setAmortizationTypeOptions(amortizationTypeOptions) //
                .setInterestTypeOptions(interestTypeOptions) //
                .setInterestCalculationPeriodTypeOptions(interestCalculationPeriodTypeOptions) //
                .setFundOptions(fundOptions) //
                .setChargeOptions(chargeOptions) //
                .setLoanPurposeOptions(loanPurposeOptions) //
                .setLoanCollateralOptions(loanCollateralOptions) //
                .setClientActiveLoanOptions(activeLoanOptions) //
                .setLoanScheduleTypeOptions(LoanScheduleType.getValuesAsEnumOptionDataList()) //
                .setLoanScheduleProcessingTypeOptions(LoanScheduleProcessingType.getValuesAsEnumOptionDataList()); //

        if (officeId != null) {
            final Set<Long> visibleChargeIds = this.chargeReadPlatformService.retrieveLoanProductCharges(productId, officeId).stream()
                    .map(ChargeData::getId).collect(Collectors.toSet());
            if (loanAccountData.getCharges() != null) {
                loanAccountData.setCharges(
                        loanAccountData.getCharges().stream().filter(charge -> visibleChargeIds.contains(charge.getChargeId())).toList());
            }
            if (loanAccountData.getOverdueCharges() != null) {
                loanAccountData.setOverdueCharges(
                        loanAccountData.getOverdueCharges().stream().filter(charge -> visibleChargeIds.contains(charge.getId())).toList());
            }
        }

        return loanAccountData;
    }

    @Override
    public LoanTransactionData retrieveRecoveryPaymentTemplate(Long loanId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.RECOVERY_REPAYMENT);
        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        BigDecimal outstandingLoanBalance = null;
        final BigDecimal unrecognizedIncomePortion = null;
        return LoanTransactionData.builder().type(transactionType).amount(loan.getTotalWrittenOff())
                .netDisbursalAmount(loan.getNetDisbursalAmount()).unrecognizedIncomePortion(unrecognizedIncomePortion)
                .paymentTypeOptions(paymentOptions).externalId(ExternalId.empty()).outstandingLoanBalance(outstandingLoanBalance)
                .manuallyReversed(false).loanId(loanId).externalLoanId(loan.getExternalId()).build();

    }

    @Override
    public LoanTransactionData retrieveLoanWriteoffTemplate(final Long loanId) {

        final LoanAccountData loan = this.loanAccountDataReadService.retrieveOne(loanId);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.WRITEOFF);
        final BigDecimal totalOutstanding = loan.getSummary() != null ? loan.getSummary().getTotalOutstanding() : null;
        final List<CodeValueData> writeOffReasonOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(LoanApiConstants.WRITEOFFREASONS));
        LoanTransactionData loanTransactionData = LoanTransactionData.builder().type(transactionType).currency(loan.getCurrency())
                .date(DateUtils.getBusinessLocalDate()).amount(totalOutstanding).netDisbursalAmount(loan.getNetDisbursalAmount())
                .externalId(ExternalId.empty()).manuallyReversed(false).loanId(loanId).externalLoanId(loan.getExternalId())
                .writeOffReasonOptions(writeOffReasonOptions).build();
        return loanTransactionData;
    }

    @Override
    public LoanTransactionData retrieveLoanReAgeTemplate(final Long loanId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.REAGE);
        final BigDecimal totalOutstanding = loan.getSummary() != null ? loan.getSummary().getTotalOutstanding() : null;
        final List<CodeValueData> reAgeReasonOptions = new ArrayList<>(
                codeValueReadPlatformService.retrieveCodeValuesByCode(LoanApiConstants.REAGE_REASONS));

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();

        int futureInstallmentCount = 0;
        int pastInstallmentCount = 0;
        LocalDate nextInstallmentDueDate = null;

        for (LoanRepaymentScheduleInstallment installment : installments) {
            LocalDate dueDate = installment.getDueDate();
            if (DateUtils.isAfter(dueDate, businessDate)) {
                futureInstallmentCount++;
                if (nextInstallmentDueDate == null || DateUtils.isBefore(dueDate, nextInstallmentDueDate)) {
                    nextInstallmentDueDate = dueDate;
                }
            } else {
                pastInstallmentCount++;
            }
        }

        final PeriodFrequencyType frequencyType = loan.getLoanRepaymentScheduleDetail().getRepaymentPeriodFrequencyType();
        final LocalDate calculatedStartDate = calculateReAgeStartDate(businessDate, frequencyType,
                loan.getLoanRepaymentScheduleDetail().getFirstRepaymentDayOfMonth(),
                loan.getLoanRepaymentScheduleDetail().getSecondRepaymentDayOfMonth());

        final CurrencyData currencyData = new CurrencyData(loan.getCurrencyCode(), null, loan.getCurrency().getDigitsAfterDecimal(),
                loan.getCurrency().getInMultiplesOf(), null, null);

        return LoanTransactionData.builder().type(transactionType).currency(currencyData).date(businessDate).amount(totalOutstanding)
                .netDisbursalAmount(loan.getNetDisbursalAmount()).loanId(loanId).externalLoanId(loan.getExternalId())
                .periodFrequencyOptions(CommonEnumerations.BASIC_PERIOD_FREQUENCY_TYPES).reAgeReasonOptions(reAgeReasonOptions)
                .reAgeInterestHandlingOptions(LoanReAgeInterestHandlingType.getValuesAsEnumOptionDataList())
                .numberOfFutureInstallments(futureInstallmentCount).numberOfPastInstallments(pastInstallmentCount)
                .nextInstallmentDueDate(nextInstallmentDueDate).calculatedStartDate(calculatedStartDate).build();
    }

    private LocalDate calculateReAgeStartDate(LocalDate businessDate, PeriodFrequencyType frequencyType, Integer firstRepaymentDayOfMonth,
            Integer secondRepaymentDayOfMonth) {
        if (frequencyType == null) {
            return null;
        }
        // calculated start date = current date + 1 unit of original frequency type
        return switch (frequencyType) {
            case DAYS -> businessDate.plusDays(1);
            case WEEKS -> businessDate.plusWeeks(1);
            case MONTHS -> businessDate.plusMonths(1);
            case YEARS -> businessDate.plusYears(1);
            case SEMI_MONTHLY -> SemiMonthlyScheduleDates.next(businessDate,
                    SemiMonthlyScheduleDates.requireDueDays(firstRepaymentDayOfMonth, secondRepaymentDayOfMonth));
            case WHOLE_TERM, INVALID -> null;
        };
    }

    @Override
    public LoanTransactionData retrieveLoanReAmortizationTemplate(final Long loanId) {
        final LoanAccountData loan = this.loanAccountDataReadService.retrieveOne(loanId);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.REAMORTIZE);
        final BigDecimal totalOutstanding = loan.getSummary() != null ? loan.getSummary().getTotalOutstanding() : null;
        final List<CodeValueData> reAmortizationReasonOptions = new ArrayList<>(
                codeValueReadPlatformService.retrieveCodeValuesByCode(LoanApiConstants.REAMORTIZATION_REASONS));
        return LoanTransactionData.builder().type(transactionType).currency(loan.getCurrency()).date(DateUtils.getBusinessLocalDate())
                .amount(totalOutstanding).netDisbursalAmount(loan.getNetDisbursalAmount()).loanId(loanId)
                .externalLoanId(loan.getExternalId()).reAmortizationReasonOptions(reAmortizationReasonOptions)
                .reAmortizationInterestHandlingOptions(LoanReAmortizationInterestHandlingType.getValuesAsEnumOptionDataList()).build();
    }

    @Override
    public LoanTransactionData retrieveLoanChargeOffTemplate(final Long loanId) {

        final LoanAccountData loan = this.loanAccountDataReadService.retrieveOne(loanId);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.CHARGE_OFF);
        final BigDecimal totalOutstanding = loan.getSummary() != null ? loan.getSummary().getTotalOutstanding() : null;
        final BigDecimal totalPrincipalOutstanding = loan.getSummary() != null ? loan.getSummary().getPrincipalOutstanding() : null;
        final BigDecimal totalInterestOutstanding = loan.getSummary() != null ? loan.getSummary().getInterestOutstanding() : null;
        final BigDecimal totalFeeOutstanding = loan.getSummary() != null ? loan.getSummary().getFeeChargesOutstanding() : null;
        final BigDecimal totalPenaltyOutstanding = loan.getSummary() != null ? loan.getSummary().getPenaltyChargesOutstanding() : null;
        final List<CodeValueData> chargeOffReasonOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(LoanApiConstants.CHARGE_OFF_REASONS));
        LoanTransactionData loanTransactionData = LoanTransactionData.builder().type(transactionType).currency(loan.getCurrency())
                .date(DateUtils.getBusinessLocalDate()).amount(totalOutstanding).netDisbursalAmount(loan.getNetDisbursalAmount())
                .principalPortion(totalPrincipalOutstanding).interestPortion(totalInterestOutstanding)
                .feeChargesPortion(totalFeeOutstanding).penaltyChargesPortion(totalPenaltyOutstanding).externalId(ExternalId.empty())
                .manuallyReversed(false).loanId(loanId).externalLoanId(loan.getExternalId()).chargeOffReasonOptions(chargeOffReasonOptions)
                .build();
        return loanTransactionData;
    }

    @Override
    public LoanTransactionData retrieveRefundByCashTemplate(Long loanId) {
        this.context.authenticatedUser();

        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        return retrieveRefundTemplate(loanId, LoanTransactionType.REFUND_FOR_ACTIVE_LOAN, paymentOptions, loan.getCurrency(),
                loanAccountDataReadService.retrieveTotalPaidInAdvance(loan.getId()).getPaidInAdvance(), loan.getNetDisbursalAmount(),
                loan.getExternalId());
    }

    @Override
    public LoanTransactionData retrieveCreditBalanceRefundTemplate(Long loanId) {
        this.context.authenticatedUser();

        final Collection<PaymentTypeData> paymentOptions = null;
        final BigDecimal netDisbursal = null;
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        return retrieveRefundTemplate(loanId, LoanTransactionType.CREDIT_BALANCE_REFUND, paymentOptions, loan.getCurrency(),
                loan.getTotalOverpaid(), netDisbursal, loan.getExternalId());

    }

    private LoanTransactionData retrieveRefundTemplate(Long loanId, LoanTransactionType loanTransactionType,
            Collection<PaymentTypeData> paymentOptions, MonetaryCurrency currency, BigDecimal transactionAmount, BigDecimal netDisbursal,
            ExternalId externalLoanId) {

        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final CurrencyData currencyData = applicationCurrency.toData();

        final LocalDate currentDate = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(loanTransactionType);
        return LoanTransactionData.builder().type(transactionType).currency(currencyData).date(currentDate).amount(transactionAmount)
                .netDisbursalAmount(netDisbursal).paymentTypeOptions(paymentOptions).externalId(ExternalId.empty()).manuallyReversed(false)
                .loanId(loanId).externalLoanId(externalLoanId).build();
    }

    @Override
    public LoanTransactionData retrieveLoanForeclosureTemplate(final Long loanId, final LocalDate transactionDate) {
        this.context.authenticatedUser();

        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        loanForeclosureValidator.validateForForeclosure(loan, transactionDate);
        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final CurrencyData currencyData = applicationCurrency.toData();

        final LocalDate earliestUnpaidInstallmentDate = DateUtils.getBusinessLocalDate();

        final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = loanBalanceService.fetchLoanForeclosureDetail(loan,
                transactionDate);
        BigDecimal unrecognizedIncomePortion = null;
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.REPAYMENT);
        final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final BigDecimal outstandingLoanBalance = loanRepaymentScheduleInstallment.getPrincipalOutstanding(currency).getAmount();
        final Boolean isManuallyReversed = false;

        final Money outStandingAmount = loanRepaymentScheduleInstallment.getTotalOutstanding(currency);

        return LoanTransactionData.builder().type(transactionType).currency(currencyData).date(earliestUnpaidInstallmentDate)
                .amount(outStandingAmount.getAmount()).netDisbursalAmount(loan.getNetDisbursalAmount())
                .principalPortion(loanRepaymentScheduleInstallment.getPrincipalOutstanding(currency).getAmount())
                .interestPortion(loanRepaymentScheduleInstallment.getInterestOutstanding(currency).getAmount())
                .feeChargesPortion(loanRepaymentScheduleInstallment.getFeeChargesOutstanding(currency).getAmount())
                .penaltyChargesPortion(loanRepaymentScheduleInstallment.getPenaltyChargesOutstanding(currency).getAmount())
                .unrecognizedIncomePortion(unrecognizedIncomePortion).paymentTypeOptions(paymentTypeOptions).externalId(ExternalId.empty())
                .outstandingLoanBalance(outstandingLoanBalance).manuallyReversed(isManuallyReversed).loanId(loanId)
                .externalLoanId(loan.getExternalId()).build();
    }

    @Override
    public LoanTransactionData retrieveManualInterestRefundTemplate(final Long loanId, final Long targetTransactionId) {
        final Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        final LoanTransaction targetTxn = loan.getLoanTransaction(txn -> txn.getId() != null && txn.getId().equals(targetTransactionId));
        if (targetTxn == null) {
            throw new LoanTransactionNotFoundException(targetTransactionId);
        }
        if (!(targetTxn.isMerchantIssuedRefund() || targetTxn.isPayoutRefund())) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.not.refund.type", "Only for refund transactions");
        }
        if (targetTxn.isReversed()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.reversed", "Refund transaction is reversed");
        }
        final boolean alreadyExists = loan.getLoanTransactions().stream().anyMatch(txn -> txn.isInterestRefund() && !txn
                .getLoanTransactionRelations(rel -> rel.getToTransaction() != null && rel.getToTransaction().equals(targetTxn)).isEmpty());
        if (alreadyExists) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.interest.refund.already.exists",
                    "Interest Refund already exists for this refund");
        }

        final InterestRefundService interestRefundService = interestRefundServiceDelegate.lookupInterestRefundService(loan);
        final Money totalInterest = interestRefundService.totalInterestByTransactions(null, loan.getId(), targetTxn.getTransactionDate(),
                List.of(), loan.getLoanTransactions().stream().map(AbstractPersistableCustom::getId).toList(),
                loan.getActiveLoanTermVariations());
        final Money newTotalInterest = interestRefundService.totalInterestByTransactions(null, loan.getId(), targetTxn.getTransactionDate(),
                List.of(targetTxn), loan.getLoanTransactions().stream().map(AbstractPersistableCustom::getId).toList(),
                loan.getActiveLoanTermVariations());
        final BigDecimal interestRefundAmount = totalInterest.minus(newTotalInterest).getAmount();
        final Collection<PaymentTypeData> paymentTypeOptions = paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.INTEREST_REFUND);

        return LoanTransactionData.builder().transactionType(LoanTransactionType.INTEREST_REFUND.name()).type(transactionType)
                .date(targetTxn.getTransactionDate()).amount(interestRefundAmount).paymentTypeOptions(paymentTypeOptions)
                .currency(loan.getCurrency().toData()).build();
    }

    private LoanTransaction deriveDefaultInterestWaiverTransaction(final Loan loan) {
        final Money totalInterestOutstanding = loan.getTotalInterestOutstandingOnLoan();
        Money possibleInterestToWaive = totalInterestOutstanding.copy();
        LocalDate transactionDate = DateUtils.getBusinessLocalDate();

        if (totalInterestOutstanding.isGreaterThanZero()) {
            // find earliest known instance of overdue interest and default to
            // that
            List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
            for (final LoanRepaymentScheduleInstallment scheduledRepayment : installments) {

                final Money outstandingForPeriod = scheduledRepayment.getInterestOutstanding(loan.getCurrency());
                if (scheduledRepayment.isOverdueOn(DateUtils.getBusinessLocalDate()) && scheduledRepayment.isNotFullyPaidOff()
                        && outstandingForPeriod.isGreaterThanZero()) {
                    transactionDate = scheduledRepayment.getDueDate();
                    possibleInterestToWaive = outstandingForPeriod;
                    break;
                }
            }
        }

        return LoanTransaction.waiver(loan.getOffice(), loan, possibleInterestToWaive, transactionDate, possibleInterestToWaive,
                possibleInterestToWaive.zero(), ExternalId.empty());
    }
}
