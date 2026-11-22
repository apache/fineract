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
package org.apache.fineract.portfolio.collectionsheet.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.apache.fineract.portfolio.collectionsheet.data.CollectionSheetTransactionDataValidator;
import org.apache.fineract.portfolio.collectionsheet.serialization.CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.collectionsheet.serialization.CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.group.data.GroupCommandResponse;
import org.apache.fineract.portfolio.group.data.GroupSaveCollectionSheetRequest;
import org.apache.fineract.portfolio.group.domain.GroupTypes;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.domain.DepositAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.service.DepositAccountWritePlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnMissingBean(value = CollectionSheetWritePlatformService.class, ignored = CollectionSheetWritePlatformServiceJpaRepositoryImpl.class)
public class CollectionSheetWritePlatformServiceJpaRepositoryImpl implements CollectionSheetWritePlatformService {

    private final LoanWritePlatformService loanWritePlatformService;
    private final CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer bulkRepaymentCommandFromApiJsonDeserializer;
    private final CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer bulkDisbursalCommandFromApiJsonDeserializer;
    private final CollectionSheetTransactionDataValidator transactionDataValidator;
    private final DepositAccountAssembler accountAssembler;
    private final DepositAccountWritePlatformService accountWritePlatformService;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final TransactionBoundApplicationEventPublisher eventPublisher;
    private final FromJsonHelper fromApiJsonHelper;

    @Override
    public CommandProcessingResult updateCollectionSheet(final JsonCommand command) {

        this.transactionDataValidator.validateTransaction(command);

        final Map<String, Object> changes = new HashMap<>();
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }

        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        changes.putAll(updateBulkRepayments(command, paymentDetail));

        changes.putAll(updateBulkDisbursals(command));

        changes.putAll(updateBulkMandatorySavingsDuePayments(command, paymentDetail));

        // TODO: send type safe event one day when we refactor this module
        eventPublisher.publishEvent(command);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(command.entityId()) //
                .withGroupId(command.entityId()) //
                .with(changes) //
                .with(changes) //
                .build();
    }

    @Override
    public GroupCommandResponse saveCollectionSheet(final GroupSaveCollectionSheetRequest request) {
        final Long groupId = request.getGroupId();
        final CommandProcessingResult result = updateCollectionSheet(toLegacyCommand(request));
        return GroupCommandResponse.builder().resourceId(groupId).groupId(groupId).changes(result.getChanges()).build();
    }

    /**
     * Bridges the typed request to the {@link JsonCommand} that {@link #updateCollectionSheet(JsonCommand)} and its
     * validator and deserializers still read. The entity name and URL match what the legacy GROUP / CENTER
     * SAVECOLLECTIONSHEET commands carried, so event listeners see the same command as before.
     *
     * TODO: remove this bridge once the {@code /collectionsheet} endpoint and
     * {@link #updateCollectionSheet(JsonCommand)} are migrated to typed requests.
     */
    private JsonCommand toLegacyCommand(final GroupSaveCollectionSheetRequest request) {
        final Long groupId = request.getGroupId();
        final boolean center = request.getGroupingType() == GroupTypes.CENTER;
        final String json = fromApiJsonHelper.toJson(request);
        return JsonCommand.from(json, fromApiJsonHelper.parse(json), fromApiJsonHelper, center ? "CENTER" : "GROUP", groupId, null, groupId,
                null, null, null, null, (center ? "/centers/" : "/groups/") + groupId, null, null, null, null, null);
    }

    @Override
    public CommandProcessingResult saveIndividualCollectionSheet(final JsonCommand command) {

        this.transactionDataValidator.validateIndividualCollectionSheet(command);

        final Map<String, Object> changes = new HashMap<>();
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }

        final PaymentDetail paymentDetail = null;

        changes.putAll(updateBulkRepayments(command, paymentDetail));

        changes.putAll(updateBulkDisbursals(command));

        changes.putAll(updateBulkMandatorySavingsDuePayments(command, paymentDetail));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(command.entityId()) //
                .withGroupId(command.entityId()) //
                .with(changes) //
                .with(changes) //
                .build();
    }

    private Map<String, Object> updateBulkRepayments(final JsonCommand command, final PaymentDetail paymentDetail) {
        final Map<String, Object> changes = new HashMap<>();
        final CollectionSheetBulkRepaymentCommand bulkRepaymentCommand = this.bulkRepaymentCommandFromApiJsonDeserializer
                .commandFromApiJson(command.json(), paymentDetail);
        changes.putAll(this.loanWritePlatformService.makeLoanBulkRepayment(bulkRepaymentCommand));
        return changes;
    }

    private Map<String, Object> updateBulkDisbursals(final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>();
        final CollectionSheetBulkDisbursalCommand bulkDisbursalCommand = this.bulkDisbursalCommandFromApiJsonDeserializer
                .commandFromApiJson(command.json());
        changes.putAll(this.loanWritePlatformService.bulkLoanDisbursal(command, bulkDisbursalCommand, false));
        return changes;
    }

    private Map<String, Object> updateBulkMandatorySavingsDuePayments(final JsonCommand command, final PaymentDetail paymentDetail) {
        final Map<String, Object> changes = new HashMap<>();
        final Collection<SavingsAccountTransactionDTO> savingsTransactions = this.accountAssembler
                .assembleBulkMandatorySavingsAccountTransactionDTOs(command, paymentDetail);
        List<Long> depositTransactionIds = new ArrayList<>();
        for (SavingsAccountTransactionDTO savingsAccountTransactionDTO : savingsTransactions) {
            try {
                SavingsAccountTransaction savingsAccountTransaction = this.accountWritePlatformService
                        .mandatorySavingsAccountDeposit(savingsAccountTransactionDTO);
                depositTransactionIds.add(savingsAccountTransaction.getId());
            } catch (NoAuthorizationException e) {
                throw e;
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        changes.put("SavingsTransactions", depositTransactionIds);
        return changes;
    }
}
