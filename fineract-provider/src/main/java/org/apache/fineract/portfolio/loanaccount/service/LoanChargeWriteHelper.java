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

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.exception.InvalidCurrencyException;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.exception.LoanChargeNotFoundException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanproduct.exception.LinkedAccountRequiredException;

/**
 * Steps shared by the loan charge write services.
 */
@RequiredArgsConstructor
public class LoanChargeWriteHelper {

    private final LoanChargeRepository loanChargeRepository;
    private final LoanUtilService loanUtilService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final LoanChargeValidator loanChargeValidator;
    private final LoanScheduleService loanScheduleService;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanAccountService loanAccountService;
    private final LoanChargeService loanChargeService;

    public void checkClientOrGroupActive(final Loan loan) {
        final Client client = loan.client();
        if (client != null) {
            if (client.isNotActive()) {
                throw new ClientNotActiveException(client.getId());
            }
        }
        final Group group = loan.group();
        if (group != null) {
            if (group.isNotActive()) {
                throw new GroupNotActiveException(group.getId());
            }
        }
    }

    public LoanCharge retrieveLoanChargeBy(final Long loanId, final Long loanChargeId) {
        final LoanCharge loanCharge = this.loanChargeRepository.findById(loanChargeId)
                .orElseThrow(() -> new LoanChargeNotFoundException(loanChargeId));

        if (loanCharge.hasNotLoanIdentifiedBy(loanId)) {
            throw new LoanChargeNotFoundException(loanChargeId, loanId);
        }
        return loanCharge;
    }

    public boolean addCharge(final Loan loan, final Charge chargeDefinition, LoanCharge loanCharge) {
        if (!loan.hasCurrencyCodeOf(chargeDefinition.getCurrencyCode())) {
            final String errorMessage = "Charge and Loan must have the same currency.";
            throw new InvalidCurrencyException("loanCharge", "attach.to.loan", errorMessage);
        }

        if (loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
            final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService
                    .retriveLoanLinkedAssociation(loan.getId());
            if (portfolioAccountData == null) {
                final String errorMessage = loanCharge.name() + "Charge  requires linked savings account for payment";
                throw new LinkedAccountRequiredException("loanCharge.add", errorMessage, loanCharge.name());
            }
        }

        loanChargeValidator.validateChargeAdditionForDisbursedLoan(loan, loanCharge);
        loanChargeValidator.validateChargeHasValidSpecifiedDateIfApplicable(loan, loanCharge, loan.getDisbursementDate());
        loanChargeService.addLoanCharge(loan, loanCharge);
        loanCharge = this.loanChargeRepository.saveAndFlush(loanCharge);

        // we want to apply charge transactions only for those loans charges that are applied when a loan is active and
        // the loan product uses Upfront Accrual accounting (or None/Cash when allow-cash-and-non-cash-accrual is true),
        // or only when the loan is closed too
        final boolean accrualEnabledForActiveStatus = configurationDomainService.isAllowCashAndNonCashAccrual()
                ? loan.isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()
                : loan.isUpfrontAccrualAccountingEnabledOnLoanProduct();
        if ((loan.getStatus().isActive() && accrualEnabledForActiveStatus) || loan.getStatus().isOverpaid()
                || loan.getStatus().isClosedObligationsMet()) {
            final LoanTransaction applyLoanChargeTransaction = loanChargeService.handleChargeAppliedTransaction(loan, loanCharge, null);
            if (applyLoanChargeTransaction != null) {
                loanAccrualsProcessingService.postChargeAccrual(applyLoanChargeTransaction);
            }
        } else if (configurationDomainService.isImmediateChargeAccrualPostMaturityEnabled()
                && DateUtils.getBusinessLocalDate().isAfter(loan.getMaturityDate())
                && !loanAccrualsProcessingService.isRealTimeChargeAccrualApplicable(loan)) {
            loanAccrualsProcessingService.postChargeAccrual(loanChargeService.createChargeAppliedTransaction(loan, loanCharge, null));
        }

        return DateUtils.isBeforeBusinessDate(loanCharge.getDueLocalDate());
    }

    public Loan runScheduleRecalculation(Loan loan, final LocalDate recalculateFrom) {
        if (loan.isInterestBearingAndInterestRecalculationEnabled() && !loan.isChargedOff()) {
            ScheduleGeneratorDTO generatorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
            loanScheduleService.handleRegenerateRepaymentScheduleWithInterestRecalculation(loan, generatorDTO);
            loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
            loan = loanAccountService.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
        }
        return loan;
    }

    // The stored installments must not depend on external event serialization, which recalculates them till the
    // business date, but not after maturity.
    public void updateProgressiveInterestModel(final Loan loan) {
        if (loan.isProgressiveSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()
                && !loan.isMatured(DateUtils.getBusinessLocalDate())) {
            reprocessLoanTransactionsService.updateModel(loan);
        }
    }
}
