/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.accountnumberformat.service;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.transaction.Transactional;

import org.mifosplatform.infrastructure.accountnumberformat.data.AccountNumberFormatDataValidator;
import org.mifosplatform.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.mifosplatform.infrastructure.accountnumberformat.domain.AccountNumberFormatEnumerations.AccountNumberPrefixType;
import org.mifosplatform.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.mifosplatform.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class AccountNumberFormatWritePlatformServiceJpaRepositoryImpl implements AccountNumberFormatWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(AccountNumberFormatWritePlatformServiceJpaRepositoryImpl.class);
    private final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;
    private final AccountNumberFormatDataValidator accountNumberFormatDataValidator;

    @Autowired
    AccountNumberFormatWritePlatformServiceJpaRepositoryImpl(final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository,
            final AccountNumberFormatDataValidator accountNumberFormatDataValidator) {
        this.accountNumberFormatRepository = accountNumberFormatRepository;
        this.accountNumberFormatDataValidator = accountNumberFormatDataValidator;
    }

    @Override
    @Transactional
    public CommandProcessingResult createAccountNumberFormat(JsonCommand command) {
        try {
            this.accountNumberFormatDataValidator.validateForCreate(command.json());

            final Integer accountTypeId = command.integerValueSansLocaleOfParameterNamed(AccountNumberFormatConstants.accountTypeParamName);
            final EntityAccountType entityAccountType = EntityAccountType.fromInt(accountTypeId);

            final Integer prefixTypeId = command.integerValueSansLocaleOfParameterNamed(AccountNumberFormatConstants.prefixTypeParamName);
            AccountNumberPrefixType accountNumberPrefixType = null;
            if (prefixTypeId != null) {
                accountNumberPrefixType = AccountNumberPrefixType.fromInt(prefixTypeId);
            }

            AccountNumberFormat accountNumberFormat = new AccountNumberFormat(entityAccountType, accountNumberPrefixType);

            this.accountNumberFormatRepository.save(accountNumberFormat);

            return new CommandProcessingResultBuilder() //
                    .withEntityId(accountNumberFormat.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateAccountNumberFormat(Long accountNumberFormatId, JsonCommand command) {
        try {

            final AccountNumberFormat accountNumberFormatForUpdate = this.accountNumberFormatRepository
                    .findOneWithNotFoundDetection(accountNumberFormatId);
            EntityAccountType accountType = accountNumberFormatForUpdate.getAccountType();

            this.accountNumberFormatDataValidator.validateForUpdate(command.json(), accountType);

            final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

            if (command.isChangeInIntegerSansLocaleParameterNamed(AccountNumberFormatConstants.prefixTypeParamName,
                    accountNumberFormatForUpdate.getPrefixEnum())) {
                final Integer newValue = command.integerValueSansLocaleOfParameterNamed(AccountNumberFormatConstants.prefixTypeParamName);
                final AccountNumberPrefixType accountNumberPrefixType = AccountNumberPrefixType.fromInt(newValue);
                actualChanges.put(AccountNumberFormatConstants.prefixTypeParamName, accountNumberPrefixType);
                accountNumberFormatForUpdate.setPrefix(accountNumberPrefixType);
            }

            if (!actualChanges.isEmpty()) {
                this.accountNumberFormatRepository.saveAndFlush(accountNumberFormatForUpdate);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(accountNumberFormatId) //
                    .with(actualChanges) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteAccountNumberFormat(Long accountNumberFormatId) {
        AccountNumberFormat accountNumberFormat = this.accountNumberFormatRepository.findOneWithNotFoundDetection(accountNumberFormatId);
        this.accountNumberFormatRepository.delete(accountNumberFormat);

        return new CommandProcessingResultBuilder() //
                .withEntityId(accountNumberFormatId) //
                .build();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains(AccountNumberFormatConstants.ACCOUNT_TYPE_UNIQUE_CONSTRAINT_NAME)) {

            final Integer accountTypeId = command.integerValueSansLocaleOfParameterNamed(AccountNumberFormatConstants.accountTypeParamName);
            final EntityAccountType entityAccountType = EntityAccountType.fromInt(accountTypeId);
            throw new PlatformDataIntegrityException(AccountNumberFormatConstants.EXCEPTION_DUPLICATE_ACCOUNT_TYPE,
                    "Account Format preferences for Account type `" + entityAccountType.getCode() + "` already exists", "externalId",
                    entityAccountType.getValue(), entityAccountType.getCode());
        }
        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.account.number.format.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }
}
