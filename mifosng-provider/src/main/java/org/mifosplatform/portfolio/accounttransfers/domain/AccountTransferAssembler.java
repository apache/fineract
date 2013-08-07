/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounttransfers.domain;

import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.transferDescriptionParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.fromAccountIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.fromClientIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.fromOfficeIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.toAccountIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.toClientIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.toOfficeIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.transferAmountParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.transferDateParamName;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
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

        final Long fromOfficeId = fromApiJsonHelper.extractLongNamed(fromOfficeIdParamName, element);
        final Office fromOffice = this.officeRepository.findOne(fromOfficeId);

        final Long fromClientId = fromApiJsonHelper.extractLongNamed(fromClientIdParamName, element);
        final Client fromClient = this.clientRepository.findOneWithNotFoundDetection(fromClientId);

        final Long fromSavingsId = command.longValueOfParameterNamed(fromAccountIdParamName);
        final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(fromSavingsId);

        final Long toOfficeId = fromApiJsonHelper.extractLongNamed(toOfficeIdParamName, element);
        final Office toOffice = this.officeRepository.findOne(toOfficeId);

        final Long toClientId = fromApiJsonHelper.extractLongNamed(toClientIdParamName, element);
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
}