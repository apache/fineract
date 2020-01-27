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

package org.apache.fineract.portfolio.self.pockets.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.self.pockets.api.PocketApiConstants;
import org.apache.fineract.portfolio.self.pockets.data.PocketDataValidator;
import org.apache.fineract.portfolio.self.pockets.domain.Pocket;
import org.apache.fineract.portfolio.self.pockets.domain.PocketAccountMapping;
import org.apache.fineract.portfolio.self.pockets.domain.PocketAccountMappingRepositoryWrapper;
import org.apache.fineract.portfolio.self.pockets.domain.PocketRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PocketWritePlatformServiceImpl implements PocketWritePlatformService {

    private final PlatformSecurityContext context;
    private final PocketDataValidator pocketDataValidator;
    private final AccountEntityServiceFactory accountEntityServiceFactory;
    private final PocketRepositoryWrapper pocketRepositoryWrapper;
    private final PocketAccountMappingRepositoryWrapper pocketAccountMappingRepositoryWrapper;
    private final PocketAccountMappingReadPlatformService pocketAccountMappingReadPlatformService;

    @Autowired
    public PocketWritePlatformServiceImpl(final PlatformSecurityContext context,
            PocketDataValidator pocketDataValidator, final AccountEntityServiceFactory accountEntityServiceFactory,
            final PocketRepositoryWrapper pocketRepositoryWrapper,
            final PocketAccountMappingRepositoryWrapper pocketAccountMappingRepositoryWrapper,
            final PocketAccountMappingReadPlatformService pocketAccountMappingReadPlatformService) {
        this.context = context;
        this.pocketDataValidator = pocketDataValidator;
        this.accountEntityServiceFactory = accountEntityServiceFactory;
        this.pocketRepositoryWrapper = pocketRepositoryWrapper;
        this.pocketAccountMappingRepositoryWrapper = pocketAccountMappingRepositoryWrapper;
        this.pocketAccountMappingReadPlatformService = pocketAccountMappingReadPlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult linkAccounts(JsonCommand command) {

        this.pocketDataValidator.validateForLinkingAccounts(command.json());
        JsonArray accountsDetail = command.arrayOfParameterNamed(PocketApiConstants.accountsDetail);

        Long pocketId = this.pocketRepositoryWrapper.findByAppUserId(this.context.authenticatedUser().getId());

        if (pocketId == null) {
            final Pocket pocket = Pocket.instance(this.context.authenticatedUser().getId());
            this.pocketRepositoryWrapper.saveAndFlush(pocket);
            pocketId = pocket.getId();
        }

        final List<PocketAccountMapping> pocketAccounts = new ArrayList<>();

        for (int i = 0; i < accountsDetail.size(); i++) {
            final JsonObject element = accountsDetail.get(i).getAsJsonObject();
            final Long accountId = element.get(PocketApiConstants.accountIdParamName).getAsLong();
            final String accountType = element.get(PocketApiConstants.accountTypeParamName).getAsString();

            final AccountEntityService accountEntityService = this.accountEntityServiceFactory
                    .getAccountEntityService(accountType);
            accountEntityService.validateSelfUserAccountMapping(accountId);
            Integer accountTypeValue = EntityAccountType.valueOf(accountType).getValue();
            if (this.pocketAccountMappingReadPlatformService.validatePocketAndAccountMapping(pocketId, accountId,
                    accountTypeValue)) {
                throw new PlatformDataIntegrityException(PocketApiConstants.duplicateMappingException,
                        PocketApiConstants.duplicateMappingExceptionMessage, accountId, accountType);
            }

            final String accountNumber = accountEntityService.retrieveAccountNumberByAccountId(accountId);

            pocketAccounts.add(PocketAccountMapping.instance(pocketId, accountId, accountTypeValue, accountNumber));

        }
        this.pocketAccountMappingRepositoryWrapper.save(pocketAccounts);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(pocketId).build();

    }

    @Override
    public CommandProcessingResult delinkAccounts(JsonCommand command) {
        this.pocketDataValidator.validateForDeLinkingAccounts(command.json());
        JsonArray pocketAccountMappingList = command.arrayOfParameterNamed(PocketApiConstants.pocketAccountMappingList);

        Long pocketId = this.pocketRepositoryWrapper
                .findByAppUserIdWithNotFoundDetection(this.context.authenticatedUser().getId());

        final List<PocketAccountMapping> pocketAccounts = new ArrayList<>();

        for (JsonElement mapping : pocketAccountMappingList) {

            final Long mappingId = mapping.getAsLong();

            PocketAccountMapping pocketAccountMapping = this.pocketAccountMappingRepositoryWrapper
                    .findByIdAndPocketIdWithNotFoundException(mappingId, pocketId);

            if (pocketAccountMapping != null) {
                pocketAccounts.add(pocketAccountMapping);
            }
        }

        this.pocketAccountMappingRepositoryWrapper.delete(pocketAccounts);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(pocketId).build();

    }

}
