/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.accountdetails.domain.AccountType;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstanceRepository;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.mifosplatform.portfolio.loanaccount.serialization.CalculateLoanScheduleQueryFromApiJsonHelper;
import org.mifosplatform.portfolio.loanaccount.serialization.LoanApplicationCommandFromApiJsonHelper;
import org.mifosplatform.portfolio.loanaccount.service.LoanAssembler;
import org.mifosplatform.portfolio.loanaccount.service.LoanReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.mifosplatform.portfolio.loanproduct.serialization.LoanProductDataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanScheduleCalculationPlatformServiceImpl implements LoanScheduleCalculationPlatformService {

    private final CalculateLoanScheduleQueryFromApiJsonHelper fromApiJsonDeserializer;
    private final LoanScheduleAssembler loanScheduleAssembler;
    private final FromJsonHelper fromJsonHelper;
    private final LoanProductRepository loanProductRepository;
    private final LoanProductDataValidator loanProductCommandFromApiJsonDeserializer;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanApplicationCommandFromApiJsonHelper loanApiJsonDeserializer;
    private final LoanAssembler loanAssembler;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final LoanAccountDomainService accountDomainService;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final ConfigurationDomainService configurationDomainService;

    @Autowired
    public LoanScheduleCalculationPlatformServiceImpl(final CalculateLoanScheduleQueryFromApiJsonHelper fromApiJsonDeserializer,
            final LoanScheduleAssembler loanScheduleAssembler, final FromJsonHelper fromJsonHelper,
            final LoanProductRepository loanProductRepository, final LoanProductDataValidator loanProductCommandFromApiJsonDeserializer,
            final LoanReadPlatformService loanReadPlatformService, final LoanApplicationCommandFromApiJsonHelper loanApiJsonDeserializer,
            final LoanAssembler loanAssembler, final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            final LoanAccountDomainService accountDomainService, final CalendarInstanceRepository calendarInstanceRepository,
            final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            final ConfigurationDomainService configurationDomainService) {
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanScheduleAssembler = loanScheduleAssembler;
        this.fromJsonHelper = fromJsonHelper;
        this.loanProductRepository = loanProductRepository;
        this.loanProductCommandFromApiJsonDeserializer = loanProductCommandFromApiJsonDeserializer;
        this.loanReadPlatformService = loanReadPlatformService;
        this.loanApiJsonDeserializer = loanApiJsonDeserializer;
        this.loanAssembler = loanAssembler;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.accountDomainService = accountDomainService;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.loanRepaymentScheduleTransactionProcessorFactory = loanRepaymentScheduleTransactionProcessorFactory;
        this.configurationDomainService = configurationDomainService;
    }

    @Override
    public LoanScheduleModel calculateLoanSchedule(final JsonQuery query, Boolean validateParams) {

        /***
         * TODO: Vishwas, this is probably not required, test and remove the
         * same
         **/
        final Long productId = this.fromJsonHelper.extractLongNamed("productId", query.parsedJson());
        final LoanProduct loanProduct = this.loanProductRepository.findOne(productId);
        if (loanProduct == null) { throw new LoanProductNotFoundException(productId); }

        if (validateParams) {
            boolean isMeetingMandatoryForJLGLoans = configurationDomainService.isMeetingMandatoryForJLGLoans();
            this.loanApiJsonDeserializer.validateForCreate(query.json(), isMeetingMandatoryForJLGLoans, loanProduct);
        }
        this.fromApiJsonDeserializer.validate(query.json());

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        if (loanProduct.useBorrowerCycle()) {
            final Long clientId = this.fromJsonHelper.extractLongNamed("clientId", query.parsedJson());
            final Long groupId = this.fromJsonHelper.extractLongNamed("groupId", query.parsedJson());
            Integer cycleNumber = 0;
            if (clientId != null) {
                cycleNumber = this.loanReadPlatformService.retriveLoanCounter(clientId, loanProduct.getId());
            } else if (groupId != null) {
                cycleNumber = this.loanReadPlatformService.retriveLoanCounter(groupId, AccountType.GROUP.getValue(), loanProduct.getId());
            }
            this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(query.parsedJson(), baseDataValidator, loanProduct,
                    cycleNumber);
        } else {
            this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(query.parsedJson(), baseDataValidator, loanProduct);
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

        return this.loanScheduleAssembler.assembleLoanScheduleFrom(query.parsedJson());
    }

    @Override
    public void updateFutureSchedule(LoanScheduleData loanScheduleData, final Long loanId) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);

        LocalDate today = DateUtils.getLocalDateOfTenant();
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = loanRepaymentScheduleTransactionProcessorFactory
                .determineProcessor(loan.transactionProcessingStrategy());

        if (!loan.repaymentScheduleDetail().isInterestRecalculationEnabled() || loan.isNpa() || !loan.status().isActive()
                || !loanRepaymentScheduleTransactionProcessor.isInterestFirstRepaymentScheduleTransactionProcessor()) { return; }

        if (loan.loanProduct().isMultiDisburseLoan()) {
            BigDecimal disbursedAmount = loan.getDisbursedAmount();
            BigDecimal principalRepaid = loan.getLoanSummary().getTotalPrincipalRepaid();
            BigDecimal principalWrittenOff = loan.getLoanSummary().getTotalPrincipalWrittenOff();
            if (disbursedAmount.subtract(principalWrittenOff).subtract(principalRepaid).compareTo(BigDecimal.ZERO) != 1) { return; }
        }
        MonetaryCurrency currency = loan.getCurrency();
        Money totalPrincipal = Money.zero(currency);
        final List<LoanSchedulePeriodData> futureInstallments = new ArrayList<>();
        for (final LoanRepaymentScheduleInstallment currentInstallment : loan.fetchRepaymentScheduleInstallments()) {
            if (currentInstallment.isNotFullyPaidOff()) {
                if (!currentInstallment.getDueDate().isAfter(today)) {
                    totalPrincipal = totalPrincipal.plus(currentInstallment.getPrincipalOutstanding(currency));
                }
            }
        }
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());
        final CalendarInstance restCalendarInstance = calendarInstanceRepository.findCalendarInstaneByEntityId(
                loan.loanInterestRecalculationDetailId(), CalendarEntityType.LOAN_RECALCULATION_DETAIL.getValue());
        LocalDate calculatedRepaymentsStartingFromDate = accountDomainService.getCalculatedRepaymentsStartingFromDate(
                loan.getDisbursementDate(), loan, calendarInstance);
        LoanApplicationTerms loanApplicationTerms = loan.constructLoanApplicationTerms(applicationCurrency,
                calculatedRepaymentsStartingFromDate, restCalendarInstance);
        LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = this.loanScheduleAssembler.calculatePrepaymentAmount(
                loan.fetchRepaymentScheduleInstallments(), currency, today, loanApplicationTerms, loan.charges(), loan.getOfficeId());
        Money totalAmount = totalPrincipal.plus(loanRepaymentScheduleInstallment.getFeeChargesOutstanding(currency)).plus(
                loanRepaymentScheduleInstallment.getPenaltyChargesOutstanding(currency));
        Money interestDue = Money.zero(currency);
        if (loanRepaymentScheduleInstallment.isInterestDue(currency)) {
            interestDue = loanRepaymentScheduleInstallment.getInterestOutstanding(currency);
            totalAmount = totalAmount.plus(interestDue);
        }
        boolean isNewPaymentRequired = loanRepaymentScheduleInstallment.isInterestDue(currency) || totalPrincipal.isGreaterThanZero();
        final List<LoanTransaction> modifiedTransactions = new ArrayList<>();
        List<LoanTransaction> transactions = loan.retreiveListOfTransactionsPostDisbursementExcludeAccruals();
        for (LoanTransaction loanTransaction : transactions) {
            modifiedTransactions.add(LoanTransaction.copyTransactionProperties(loanTransaction));
        }
        if (isNewPaymentRequired) {
            LoanTransaction ondayPaymentTransaction = LoanTransaction.repayment(null, totalAmount, null, today, null,
                    DateUtils.getLocalDateTimeOfTenant(), null);
            modifiedTransactions.add(ondayPaymentTransaction);
        }

        LoanScheduleModel model = this.loanScheduleAssembler.assembleForInterestRecalculation(loanApplicationTerms, loan.getOfficeId(),
                modifiedTransactions, loan.charges(), loanRepaymentScheduleTransactionProcessor, today);
        LoanScheduleData scheduleDate = model.toData();
        Collection<LoanSchedulePeriodData> periodDatas = scheduleDate.getPeriods();
        for (LoanSchedulePeriodData periodData : periodDatas) {
            if ((periodData.periodDueDate().isEqual(today) || periodData.periodDueDate().isAfter(today)) && isNewPaymentRequired) {
                LoanSchedulePeriodData loanSchedulePeriodData = LoanSchedulePeriodData.repaymentOnlyPeriod(periodData.periodNumber(),
                        periodData.periodFromDate(), periodData.periodDueDate(), totalPrincipal.getAmount(), periodData
                                .principalLoanBalanceOutstanding(), interestDue.getAmount(), loanRepaymentScheduleInstallment
                                .getFeeChargesCharged(currency).getAmount(),
                        loanRepaymentScheduleInstallment.getPenaltyChargesCharged(currency).getAmount(), totalAmount.getAmount());
                futureInstallments.add(loanSchedulePeriodData);
                isNewPaymentRequired = false;
            } else if (periodData.periodDueDate().isAfter(today)) {
                futureInstallments.add(periodData);
            }

        }
        loanScheduleData.updateFuturePeriods(futureInstallments);
    }
}