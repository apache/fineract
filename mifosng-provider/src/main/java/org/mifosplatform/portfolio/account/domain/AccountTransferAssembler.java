/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.domain;

import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.fromAccountIdParamName;
import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.fromClientIdParamName;
import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.fromOfficeIdParamName;
import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.toAccountIdParamName;
import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.toClientIdParamName;
import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.toOfficeIdParamName;
import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.transferAmountParamName;
import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.transferDateParamName;
import static org.mifosplatform.portfolio.account.AccountTransfersApiConstants.transferDescriptionParamName;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.portfolio.account.data.AccountTransferDTO;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountAssembler;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class AccountTransferAssembler {

    private final ClientRepositoryWrapper clientRepository;
    private final OfficeRepository officeRepository;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public AccountTransferAssembler(final ClientRepositoryWrapper clientRepository, final OfficeRepository officeRepository,
            final SavingsAccountAssembler savingsAccountAssembler, final FromJsonHelper fromApiJsonHelper) {
        this.clientRepository = clientRepository;
        this.officeRepository = officeRepository;
        this.savingsAccountAssembler = savingsAccountAssembler;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public AccountTransfer assembleSavingsToSavingsTransfer(final JsonCommand command, final SavingsAccountTransaction withdrawal,
            final SavingsAccountTransaction deposit) {

        final JsonElement element = command.parsedJson();

        final Long fromOfficeId = this.fromApiJsonHelper.extractLongNamed(fromOfficeIdParamName, element);
        final Office fromOffice = this.officeRepository.findOne(fromOfficeId);

        final Long fromClientId = this.fromApiJsonHelper.extractLongNamed(fromClientIdParamName, element);
        final Client fromClient = this.clientRepository.findOneWithNotFoundDetection(fromClientId);

        final Long fromSavingsId = command.longValueOfParameterNamed(fromAccountIdParamName);
        final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(fromSavingsId);

        final Long toOfficeId = this.fromApiJsonHelper.extractLongNamed(toOfficeIdParamName, element);
        final Office toOffice = this.officeRepository.findOne(toOfficeId);

        final Long toClientId = this.fromApiJsonHelper.extractLongNamed(toClientIdParamName, element);
        final Client toClient = this.clientRepository.findOneWithNotFoundDetection(toClientId);

        final Long toSavingsId = command.longValueOfParameterNamed(toAccountIdParamName);
        final SavingsAccount toSavingsAccount = this.savingsAccountAssembler.assembleFrom(toSavingsId);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transferDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(transferAmountParamName);
        final Money transactionMonetaryAmount = Money.of(fromSavingsAccount.getCurrency(), transactionAmount);

        final String description = command.stringValueOfParameterNamed(transferDescriptionParamName);

        return AccountTransfer.savingsToSavingsTransfer(fromOffice, fromClient, fromSavingsAccount, toOffice, toClient, toSavingsAccount,
                withdrawal, deposit, transactionDate, transactionMonetaryAmount, description);
    }

    public AccountTransfer assembleSavingsToLoanTransfer(final JsonCommand command, final SavingsAccount fromSavingsAccount,
            final Loan toLoanAccount, final SavingsAccountTransaction withdrawal, final LoanTransaction loanRepaymentTransaction) {

        final JsonElement element = command.parsedJson();

        final Long fromOfficeId = this.fromApiJsonHelper.extractLongNamed(fromOfficeIdParamName, element);
        final Office fromOffice = this.officeRepository.findOne(fromOfficeId);

        final Long fromClientId = this.fromApiJsonHelper.extractLongNamed(fromClientIdParamName, element);
        final Client fromClient = this.clientRepository.findOneWithNotFoundDetection(fromClientId);

        final Long toOfficeId = this.fromApiJsonHelper.extractLongNamed(toOfficeIdParamName, element);
        final Office toOffice = this.officeRepository.findOne(toOfficeId);

        final Long toClientId = this.fromApiJsonHelper.extractLongNamed(toClientIdParamName, element);
        final Client toClient = this.clientRepository.findOneWithNotFoundDetection(toClientId);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transferDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(transferAmountParamName);
        final Money transactionMonetaryAmount = Money.of(fromSavingsAccount.getCurrency(), transactionAmount);

        final String description = command.stringValueOfParameterNamed(transferDescriptionParamName);

        return AccountTransfer.savingsToLoanTransfer(fromOffice, fromClient, fromSavingsAccount, toOffice, toClient, toLoanAccount,
                withdrawal, loanRepaymentTransaction, transactionDate, transactionMonetaryAmount, description);
    }

    public AccountTransfer assembleLoanToSavingsTransfer(final JsonCommand command, final Loan fromLoanAccount,
            final SavingsAccount toSavingsAccount, final SavingsAccountTransaction deposit, final LoanTransaction loanRefundTransaction) {

        final JsonElement element = command.parsedJson();

        final Long fromOfficeId = this.fromApiJsonHelper.extractLongNamed(fromOfficeIdParamName, element);
        final Office fromOffice = this.officeRepository.findOne(fromOfficeId);

        final Long fromClientId = this.fromApiJsonHelper.extractLongNamed(fromClientIdParamName, element);
        final Client fromClient = this.clientRepository.findOneWithNotFoundDetection(fromClientId);

        final Long toOfficeId = this.fromApiJsonHelper.extractLongNamed(toOfficeIdParamName, element);
        final Office toOffice = this.officeRepository.findOne(toOfficeId);

        final Long toClientId = this.fromApiJsonHelper.extractLongNamed(toClientIdParamName, element);
        final Client toClient = this.clientRepository.findOneWithNotFoundDetection(toClientId);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transferDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(transferAmountParamName);
        final Money transactionMonetaryAmount = Money.of(toSavingsAccount.getCurrency(), transactionAmount);

        final String description = command.stringValueOfParameterNamed(transferDescriptionParamName);

        return AccountTransfer.LoanTosavingsTransfer(fromOffice, fromClient, fromLoanAccount, toOffice, toClient, toSavingsAccount,
                deposit, loanRefundTransaction, transactionDate, transactionMonetaryAmount, description);
    }

    public AccountTransfer assembleSavingsToLoanTransfer(final AccountTransferDTO accountTransferDTO,
            final SavingsAccount fromSavingsAccount, final Loan toLoanAccount, final SavingsAccountTransaction savingsAccountTransaction,
            final LoanTransaction loanTransaction) {
        final Office fromOffice = fromSavingsAccount.office();
        final Client fromClient = fromSavingsAccount.getClient();
        final Office toOffice = toLoanAccount.getOffice();
        final Client toClient = toLoanAccount.client();

        final Money transactionMonetaryAmount = Money.of(fromSavingsAccount.getCurrency(), accountTransferDTO.getTransactionAmount());
        return AccountTransfer.savingsToLoanTransfer(fromOffice, fromClient, fromSavingsAccount, toOffice, toClient, toLoanAccount,
                savingsAccountTransaction, loanTransaction, accountTransferDTO.getTransactionDate(), transactionMonetaryAmount,
                accountTransferDTO.getDescription());
    }

}