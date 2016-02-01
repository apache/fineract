/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounts.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.portfolio.accounts.domain.ShareAccount;
import org.mifosplatform.portfolio.accounts.domain.ShareAccountTempRepository;
import org.mifosplatform.portfolio.accounts.exceptions.ShareAccountNotFoundException;
import org.mifosplatform.portfolio.accounts.serialization.ShareAccountDataSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class ShareAccountWritePlatformServiceJpaRepositoryImpl implements ShareAccountWritePlatformService {

    private final ShareAccountDataSerializer accountDataSerializer;

    @Autowired
    public ShareAccountWritePlatformServiceJpaRepositoryImpl(final ShareAccountDataSerializer accountDataSerializer) {
        this.accountDataSerializer = accountDataSerializer;
    }

    @Override
    public CommandProcessingResult createShareAccount(JsonCommand jsonCommand) {
        try {
            ShareAccount account = this.accountDataSerializer.validateAndCreate(jsonCommand);
            ShareAccountTempRepository.getInstance().save(account);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(account.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult updateShareAccount(Long accountId, JsonCommand jsonCommand) {
        try {
            ShareAccount account = ShareAccountTempRepository.getInstance().findOne(accountId);
            if (account == null) {
                throw new ShareAccountNotFoundException(accountId) ;
            }
            Map<String, Object> changes = this.accountDataSerializer.validateAndUpdate(jsonCommand, account);
            if (!changes.isEmpty()) {
                // Save the data here
            }
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(accountId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

    }

}
