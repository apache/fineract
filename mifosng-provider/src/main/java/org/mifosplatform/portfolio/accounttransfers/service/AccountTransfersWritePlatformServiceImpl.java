/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounttransfers.service;

import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.fromAccountIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.toAccountIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.transferAmountParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.transferDateParamName;

import java.math.BigDecimal;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.portfolio.accounttransfers.data.AccountTransfersDataValidator;
import org.mifosplatform.portfolio.accounttransfers.domain.AccountTransfer;
import org.mifosplatform.portfolio.accounttransfers.domain.AccountTransferAssembler;
import org.mifosplatform.portfolio.accounttransfers.domain.AccountTransferRepository;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountAssembler;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountDomainService;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountTransfersWritePlatformServiceImpl implements AccountTransfersWritePlatformService {

    private final AccountTransfersDataValidator accountTransfersDataValidator;
    private final AccountTransferAssembler accountTransferAssembler;
    private final AccountTransferRepository accountTransferRepository;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final SavingsAccountDomainService savingsAccountDomainService;

    @Autowired
    public AccountTransfersWritePlatformServiceImpl(final AccountTransfersDataValidator accountTransfersDataValidator,
            final AccountTransferAssembler accountTransferAssembler, final AccountTransferRepository accountTransferRepository,
            final SavingsAccountAssembler savingsAccountAssembler, final SavingsAccountDomainService savingsAccountDomainService) {
        this.accountTransfersDataValidator = accountTransfersDataValidator;
        this.accountTransferAssembler = accountTransferAssembler;
        this.accountTransferRepository = accountTransferRepository;
        this.savingsAccountAssembler = savingsAccountAssembler;
        this.savingsAccountDomainService = savingsAccountDomainService;
    }

    @Transactional
    @Override
    public CommandProcessingResult create(final JsonCommand command) {

        this.accountTransfersDataValidator.validate(command);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transferDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(transferAmountParamName);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);

        // check fromAccountType settings to see if its a savings account
        // withdrawal or a loan account transfer
        final Long fromSavingsId = command.longValueOfParameterNamed(fromAccountIdParamName);
        final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(fromSavingsId);

        final PaymentDetail paymentDetail = null;

        final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(fromSavingsAccount, fmt,
                transactionDate, transactionAmount, paymentDetail);

        final Long toSavingsId = command.longValueOfParameterNamed(toAccountIdParamName);
        final SavingsAccount toSavingsAccount = this.savingsAccountAssembler.assembleFrom(toSavingsId);

        final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount, fmt, transactionDate,
                transactionAmount, paymentDetail);

        AccountTransfer transferTransaction = this.accountTransferAssembler.assembleSavingsToSavingsTransfer(command, withdrawal, deposit);
        this.accountTransferRepository.saveAndFlush(transferTransaction);

        return new CommandProcessingResultBuilder() //
                .withSavingsId(fromSavingsId) //
                .withEntityId(transferTransaction.getId()) //
                .build();
    }
}