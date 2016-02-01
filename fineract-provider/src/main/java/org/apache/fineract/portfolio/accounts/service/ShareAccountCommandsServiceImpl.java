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

import java.util.Set;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.accounts.constants.ShareAccountApiConstants;
import org.apache.fineract.portfolio.accounts.domain.PurchasedShares;
import org.apache.fineract.portfolio.accounts.domain.ShareAccount;
import org.apache.fineract.portfolio.accounts.domain.ShareAccountTempRepository;
import org.apache.fineract.portfolio.accounts.serialization.ShareAccountDataSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service(value = "SHAREACCOUNT_COMMANDSERVICE")
public class ShareAccountCommandsServiceImpl implements AccountsCommandsService {

    private final FromJsonHelper fromApiJsonHelper;
    
    private final ShareAccountDataSerializer shareAccountDataSerializer ;
    
    @Autowired
    public ShareAccountCommandsServiceImpl(final FromJsonHelper fromApiJsonHelper,
            final ShareAccountDataSerializer shareAccountDataSerializer) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.shareAccountDataSerializer = shareAccountDataSerializer ;
    }

    @Override
    public Object handleCommand(Long accountId, String command, String jsonBody) {
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(jsonBody);
        final JsonCommand jsonCommand = JsonCommand.from(jsonBody, parsedCommand, this.fromApiJsonHelper, null, null, null, null, null,
                null, null, null, null, null);
        if(ShareAccountApiConstants.APPROVE_COMMAND.equals(command)){
            return approveShareAccount(accountId, jsonCommand) ;
        }if(ShareAccountApiConstants.REJECT_COMMAND.equals(command)){
            return rejectShareAccount(accountId, jsonCommand) ;
        }else if(ShareAccountApiConstants.APPLY_ADDITIONALSHARES_COMMAND.equals(command)) {
            return applyAdditionalShares(accountId, jsonCommand) ;
        }else if(ShareAccountApiConstants.APPROVE_ADDITIONSHARES_COMMAND.equals(command)) {
            return approveAdditionalShares(accountId, jsonCommand) ;
        }else if(ShareAccountApiConstants.REJECT_ADDITIONSHARES_COMMAND.equals(command)) {
            return rejectAdditionalShares(accountId, jsonCommand) ;
        }
        
        return CommandProcessingResult.empty();
    }

    public Object approveShareAccount(Long accountId, JsonCommand jsonCommand) {
        //We need to add approval date also
        ShareAccount account = ShareAccountTempRepository.getInstance().findOne(accountId);
        account.setStatus("Approved");
        Set<PurchasedShares> purchasedShares = account.getPurchasedShares() ;
        for(PurchasedShares pur: purchasedShares) {
            pur.setStatus("Approved") ;    
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(jsonCommand.commandId()) //
                .withEntityId(account.getId()) //
                .build();
    }

    public Object rejectShareAccount(Long accountId, JsonCommand jsonCommand) {
        ShareAccount account = ShareAccountTempRepository.getInstance().findOne(accountId);
        account.setStatus("Rejected");
        //rejection date we need to capture
        return new CommandProcessingResultBuilder() //
                .withCommandId(jsonCommand.commandId()) //
                .withEntityId(account.getId()) //
                .build();
    }

    public Object applyAdditionalShares(Long accountId, JsonCommand jsonCommand) {
        ShareAccount account = ShareAccountTempRepository.getInstance().findOne(accountId);
        Set<PurchasedShares> additionalShares = this.shareAccountDataSerializer.asembleAdditionalShares(jsonCommand.parsedJson()) ;
        account.addAddtionalShares(additionalShares) ;
        return new CommandProcessingResultBuilder() //
                .withCommandId(jsonCommand.commandId()) //
                .withEntityId(account.getId()) //
                .build();
    }

    public Object approveAdditionalShares(Long accountId, JsonCommand jsonCommand) {
        //user might have requested for different dates.
        //we need to capture either purchase date or ids []
        ShareAccount account = ShareAccountTempRepository.getInstance().findOne(accountId);
        Set<PurchasedShares> purchasedShares = account.getPurchasedShares() ;
        for(PurchasedShares pur: purchasedShares) {
            if(pur.getStatus().equals("Submitted") && !pur.getStatus().equals("Rejected")) {
                pur.setStatus("Approved") ;    
            }
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(jsonCommand.commandId()) //
                .withEntityId(account.getId()) //
                .build();
    }

    public Object rejectAdditionalShares(Long accountId, JsonCommand jsonCommand) {
        //user might have requested for different dates.
        //we need to capture either purchase date or ids []
        ShareAccount account = ShareAccountTempRepository.getInstance().findOne(accountId);
        Set<PurchasedShares> purchasedShares = account.getPurchasedShares() ;
        for(PurchasedShares pur: purchasedShares) {
            if(pur.getStatus().equals("Submitted")) {
                pur.setStatus("Rejected") ;    
            }
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(jsonCommand.commandId()) //
                .withEntityId(account.getId()) //
                .build();
    }
}
