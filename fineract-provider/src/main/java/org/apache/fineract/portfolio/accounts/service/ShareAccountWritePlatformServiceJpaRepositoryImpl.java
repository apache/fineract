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
package org.apache.fineract.portfolio.accounts.service;

import java.util.Map;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.portfolio.accounts.domain.ShareAccount;
import org.apache.fineract.portfolio.accounts.domain.ShareAccountTempRepository;
import org.apache.fineract.portfolio.accounts.exceptions.ShareAccountNotFoundException;
import org.apache.fineract.portfolio.accounts.serialization.ShareAccountDataSerializer;
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
