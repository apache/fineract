/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.domain.AccountNumberGenerator;
import org.mifosplatform.portfolio.client.domain.AccountNumberGeneratorFactory;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepository;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.mifosplatform.portfolio.savings.api.SavingsApiConstants;
import org.mifosplatform.portfolio.savings.data.SavingsAccountDataValidator;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountAssembler;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountRepository;
import org.mifosplatform.portfolio.savings.domain.SavingsProduct;
import org.mifosplatform.portfolio.savings.domain.SavingsProductRepository;
import org.mifosplatform.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.mifosplatform.portfolio.savings.exception.SavingsProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavingsAccountWritePlatformServiceJpaRepositoryImpl implements SavingsAccountWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(SavingsAccountWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final SavingsAccountRepository savingAccountRepository;
    private final SavingsAccountAssembler savingAccountAssembler;
    private final SavingsAccountDataValidator savingsAccountDataValidator;
    private final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;
    private final ClientRepository clientRepository;
    private final GroupRepository groupRepository;
    private final SavingsProductRepository savingsProductRepository;

    @Autowired
    public SavingsAccountWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final SavingsAccountRepository savingAccountRepository, final SavingsAccountAssembler savingAccountAssembler,
            final SavingsAccountDataValidator savingsAccountDataValidator,
            final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory, final ClientRepository clientRepository,
            final GroupRepository groupRepository, final SavingsProductRepository savingsProductRepository) {
        this.context = context;
        this.savingAccountRepository = savingAccountRepository;
        this.savingAccountAssembler = savingAccountAssembler;
        this.accountIdentifierGeneratorFactory = accountIdentifierGeneratorFactory;
        this.savingsAccountDataValidator = savingsAccountDataValidator;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.savingsProductRepository = savingsProductRepository;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataAccessException dve) {

        final StringBuilder errorCodeBuilder = new StringBuilder("error.msg.").append(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("sa_account_no_UNIQUE")) {
            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            errorCodeBuilder.append(".duplicate.accountNo");
            throw new PlatformDataIntegrityException(errorCodeBuilder.toString(), "Savings account with accountNo " + accountNo
                    + " already exists", "accountNo", accountNo);

        } else if (realCause.getMessage().contains("sa_external_id_UNIQUE")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            errorCodeBuilder.append(".duplicate.externalId");
            throw new PlatformDataIntegrityException(errorCodeBuilder.toString(), "Savings account with externalId " + externalId
                    + " already exists", "externalId", externalId);
        }

        errorCodeBuilder.append(".unknown.data.integrity.issue");
        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException(errorCodeBuilder.toString(), "Unknown data integrity issue with savings account.");
    }

    @Transactional
    @Override
    public CommandProcessingResult createSavingAccount(final JsonCommand command) {
        try {
            this.context.authenticatedUser();
            this.savingsAccountDataValidator.validateForCreate(command.json());

            final SavingsAccount account = this.savingAccountAssembler.assembleFrom(command);
            this.savingAccountRepository.save(account);

            if (account.isAccountNumberRequiresAutoGeneration()) {
                final AccountNumberGenerator accountNoGenerator = this.accountIdentifierGeneratorFactory
                        .determineLoanAccountNoGenerator(account.getId());
                account.updateAccountNo(accountNoGenerator.generate());

                this.savingAccountRepository.save(account);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(account.getId()) //
                    .withOfficeId(account.officeId()) //
                    .withClientId(account.clientId()) //
                    .withGroupId(account.groupId()) //
                    .build();
        } catch (DataAccessException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }

    }

    @Transactional
    @Override
    public CommandProcessingResult updateSavingAccount(final Long accountId, final JsonCommand command) {
        try {
            this.context.authenticatedUser();
            this.savingsAccountDataValidator.validateForUpdate(command.json());

            final Map<String, Object> changes = new LinkedHashMap<String, Object>(20);

            final SavingsAccount account = this.savingAccountRepository.findOne(accountId);
            if (account == null) { throw new SavingsAccountNotFoundException(accountId); }

            account.update(command, changes);

            if (!changes.isEmpty()) {

                if (changes.containsKey(SavingsApiConstants.clientIdParamName)) {
                    final Long clientId = command.longValueOfParameterNamed(SavingsApiConstants.clientIdParamName);
                    if (clientId != null) {
                        final Client client = this.clientRepository.findOne(clientId);
                        if (client == null || client.isDeleted()) { throw new ClientNotFoundException(clientId); }
                        account.update(client);
                    } else {
                        final Client client = null;
                        account.update(client);
                    }
                }

                if (changes.containsKey(SavingsApiConstants.groupIdParamName)) {
                    final Long groupId = command.longValueOfParameterNamed(SavingsApiConstants.groupIdParamName);
                    if (groupId != null) {
                        final Group group = this.groupRepository.findOne(groupId);
                        if (group == null || group.isDeleted()) { throw new GroupNotFoundException(groupId); }
                        account.update(group);
                    } else {
                        final Group group = null;
                        account.update(group);
                    }
                }

                if (changes.containsKey(SavingsApiConstants.productIdParamName)) {
                    final Long productId = command.longValueOfParameterNamed(SavingsApiConstants.productIdParamName);
                    final SavingsProduct product = this.savingsProductRepository.findOne(productId);
                    if (product == null) { throw new SavingsProductNotFoundException(productId); }

                    account.update(product);
                }

                this.savingAccountRepository.saveAndFlush(account);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(account.getId()) //
                    .withOfficeId(account.officeId()) //
                    .withClientId(account.clientId()) //
                    .withGroupId(account.groupId()) //
                    .with(changes) //
                    .build();
        } catch (DataAccessException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deposit(final JsonCommand command) {

        this.context.authenticatedUser();

        SavingsAccount account = this.savingAccountRepository.findOne(command.entityId());
        if (account == null) { throw new SavingsAccountNotFoundException(command.entityId()); }

        // FIXME - add basic support for deposits

        return new CommandProcessingResultBuilder() //
                .withEntityId(account.getId()) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult withdraw(final JsonCommand command) {

        this.context.authenticatedUser();

        final SavingsAccount account = this.savingAccountRepository.findOne(command.entityId());
        if (account == null) { throw new SavingsAccountNotFoundException(command.entityId()); }

        // FIXME - add basic support for withdrawals

        this.savingAccountRepository.save(account);

        return new CommandProcessingResultBuilder() //
                .withEntityId(account.getId()) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteSavingAccount(final Long accountId) {

        this.context.authenticatedUser();

        final SavingsAccount account = this.savingAccountRepository.findOne(accountId);
        if (account == null) { throw new SavingsAccountNotFoundException(accountId); }

        this.savingAccountRepository.delete(account);

        return new CommandProcessingResultBuilder() //
                .withEntityId(accountId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .build();
    }
}