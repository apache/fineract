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
package org.apache.fineract.portfolio.account.service;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferRequest;
import org.apache.fineract.portfolio.account.data.AccountTransferResponse;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.exception.DifferentCurrenciesException;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
import static org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants.transferAmountParamName;
import static org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants.transferDateParamName;

@Service
@RequiredArgsConstructor
public class AccountTransferWritePlatformServiceImpl implements AccountTransferWritePlatformService {

  @Override
  public AccountTransferResponse create(Command<AccountTransferRequest> command) {

    final AccountTransferRequest request = command.getPayload();
    boolean isRegularTransaction = true;

    final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(request.getDateFormat()).withLocale(Locale.of(request.getLocale()));
    final PortfolioAccountType fromAccountType = PortfolioAccountType.fromInt(Integer.valueOf(request.getFromAccountType()));
    final PortfolioAccountType toAccountType = PortfolioAccountType.fromInt(Integer.valueOf(request.getToAccountType()));

    final PaymentDetail paymentDetail = null;
    Long fromSavingsAccountId = null;
    Long transferDetailId = null;
    boolean isInterestTransfer = false;
    boolean isAccountTransfer = true;
    Long fromLoanAccountId = null;
    boolean isWithdrawBalance = false;
    final boolean backdatedTxnsAllowedTill = false;

    if (isSavingsToSavingsAccountTransfer(fromAccountType, toAccountType)) {

      fromSavingsAccountId = Long.valueOf(request.getFromAccountId()); // command.longValueOfParameterNamed(fromAccountIdParamName);
      final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(fromSavingsAccountId,
          backdatedTxnsAllowedTill);

      final SavingsTransactionBooleanValues transactionBooleanValues = new SavingsTransactionBooleanValues(isAccountTransfer,
          isRegularTransaction, fromSavingsAccount.isWithdrawalFeeApplicableForTransfer(), isInterestTransfer, isWithdrawBalance);
      final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(fromSavingsAccount, fmt,
          transactionDate, transactionAmount, paymentDetail, transactionBooleanValues, backdatedTxnsAllowedTill);

      final SavingsAccount toSavingsAccount = this.savingsAccountAssembler.assembleFrom(request.getToAccountId(), backdatedTxnsAllowedTill);

      final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount, fmt, transactionDate,
          transactionAmount, paymentDetail, isAccountTransfer, isRegularTransaction, backdatedTxnsAllowedTill);

      if (!fromSavingsAccount.getCurrency().getCode().equals(toSavingsAccount.getCurrency().getCode())) {
        throw new DifferentCurrenciesException(fromSavingsAccount.getCurrency().getCode(),
            toSavingsAccount.getCurrency().getCode());
      }

      final AccountTransferDetails accountTransferDetails = this.accountTransferAssembler.assembleSavingsToSavingsTransfer(command,
          fromSavingsAccount, toSavingsAccount, withdrawal, deposit);
      this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
      transferDetailId = accountTransferDetails.getId();

    } else if (isSavingsToLoanAccountTransfer(fromAccountType, toAccountType)) {

      final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(request.getFromAccountId(),
          backdatedTxnsAllowedTill);

      final SavingsTransactionBooleanValues transactionBooleanValues = new SavingsTransactionBooleanValues(isAccountTransfer,
          isRegularTransaction, fromSavingsAccount.isWithdrawalFeeApplicableForTransfer(), isInterestTransfer, isWithdrawBalance);
      final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(fromSavingsAccount, fmt,
          transactionDate, transactionAmount, paymentDetail, transactionBooleanValues, backdatedTxnsAllowedTill);

      final Long toLoanAccountId = command.longValueOfParameterNamed(toAccountIdParamName);
      Loan toLoanAccount = this.loanAccountAssembler.assembleFrom(toLoanAccountId);

      final Boolean isHolidayValidationDone = false;
      final HolidayDetailDTO holidayDetailDto = null;
      final boolean isRecoveryRepayment = false;
      final String chargeRefundChargeType = null;

      ExternalId externalId = externalIdFactory.create();
      final LoanTransaction loanRepaymentTransaction = this.loanAccountDomainService.makeRepayment(LoanTransactionType.REPAYMENT,
          toLoanAccount, transactionDate, transactionAmount, paymentDetail, null, externalId, isRecoveryRepayment,
          chargeRefundChargeType, isAccountTransfer, holidayDetailDto, isHolidayValidationDone);
      toLoanAccount = loanRepaymentTransaction.getLoan();
      final AccountTransferDetails accountTransferDetails = this.accountTransferAssembler.assembleSavingsToLoanTransfer(command,
          fromSavingsAccount, toLoanAccount, withdrawal, loanRepaymentTransaction);
      this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
      transferDetailId = accountTransferDetails.getId();

    } else if (isLoanToSavingsAccountTransfer(fromAccountType, toAccountType)) {
      // FIXME - kw - ADD overpaid loan to savings account transfer
      // support.

      fromLoanAccountId = command.longValueOfParameterNamed(fromAccountIdParamName);
      final Loan fromLoanAccount = this.loanAccountAssembler.assembleFrom(fromLoanAccountId);
      ExternalId externalId = externalIdFactory.create();
      final LoanTransaction loanRefundTransaction = this.loanAccountDomainService.makeRefund(fromLoanAccountId,
          new CommandProcessingResultBuilder(), transactionDate, transactionAmount, paymentDetail, null, externalId);

      final Long toSavingsAccountId = command.longValueOfParameterNamed(toAccountIdParamName);
      final SavingsAccount toSavingsAccount = this.savingsAccountAssembler.assembleFrom(toSavingsAccountId, backdatedTxnsAllowedTill);

      final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount, fmt, transactionDate,
          transactionAmount, paymentDetail, isAccountTransfer, isRegularTransaction, backdatedTxnsAllowedTill);

      final AccountTransferDetails accountTransferDetails = this.accountTransferAssembler.assembleLoanToSavingsTransfer(command,
          fromLoanAccount, toSavingsAccount, deposit, loanRefundTransaction);
      this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
      transferDetailId = accountTransferDetails.getId();
    }

    final CommandProcessingResultBuilder builder = new CommandProcessingResultBuilder().withEntityId(transferDetailId);

    if (fromAccountType.isSavingsAccount()) {
      builder.withSavingsId(fromSavingsAccountId);
    }
    if (fromAccountType.isLoanAccount()) {
      builder.withLoanId(fromLoanAccountId);
    }

    return AccountTransferResponse.builder()
        .savingsId(fromSavingsAccountId)
        .loanId(fromLoanAccountId)
        .resourceId(transferDetailId).build();
  }

  private boolean isLoanToSavingsAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
    return fromAccountType == PortfolioAccountType.LOAN && toAccountType == PortfolioAccountType.SAVINGS;
  }

  private boolean isSavingsToLoanAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
    return fromAccountType == PortfolioAccountType.SAVINGS && toAccountType == PortfolioAccountType.LOAN;
  }

  private boolean isSavingsToSavingsAccountTransfer(final PortfolioAccountType fromAccountType,
                                                    final PortfolioAccountType toAccountType) {
    return fromAccountType == PortfolioAccountType.SAVINGS && toAccountType == PortfolioAccountType.SAVINGS;
  }
}
