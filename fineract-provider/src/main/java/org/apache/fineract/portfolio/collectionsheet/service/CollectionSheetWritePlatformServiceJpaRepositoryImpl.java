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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.apache.fineract.portfolio.collectionsheet.data.CollectionSheetTransactionDataValidator;
import org.apache.fineract.portfolio.collectionsheet.data.SaveCollectionSheetRequest;
import org.apache.fineract.portfolio.collectionsheet.serialization.CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.collectionsheet.serialization.CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.meeting.service.MeetingWritePlatformService;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetailAssembler;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.domain.DepositAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.service.DepositAccountWritePlatformService;

@RequiredArgsConstructor
public class CollectionSheetWritePlatformServiceJpaRepositoryImpl implements CollectionSheetWritePlatformService {

    private final LoanWritePlatformService loanWritePlatformService;
    private final CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer bulkRepaymentCommandFromApiJsonDeserializer;
    private final CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer bulkDisbursalCommandFromApiJsonDeserializer;
    private final CollectionSheetTransactionDataValidator transactionDataValidator;
    private final MeetingWritePlatformService meetingWritePlatformService;
    private final DepositAccountAssembler accountAssembler;
    private final DepositAccountWritePlatformService accountWritePlatformService;
    private final PaymentDetailAssembler paymentDetailAssembler;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final ToApiJsonSerializer<Object> toApiJsonSerializer;
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

        this.meetingWritePlatformService.updateCollectionSheetAttendance(command);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(command.entityId()) //
                .withGroupId(command.entityId()) //
                .with(changes).with(changes).build();
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
                .with(changes).with(changes).build();
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
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        changes.put("SavingsTransactions", depositTransactionIds);
        return changes;
    }

    /**
     * Added with String as a parameter
     *
     **/
    private Map<String, Object> updateBulkRepayments(final String json, final PaymentDetail paymentDetail) {
        final Map<String, Object> changes = new HashMap<>();
        final CollectionSheetBulkRepaymentCommand bulkRepaymentCommand = this.bulkRepaymentCommandFromApiJsonDeserializer
                .commandFromApiJson(json, paymentDetail);
        changes.putAll(this.loanWritePlatformService.makeLoanBulkRepayment(bulkRepaymentCommand));
        return changes;
    }

    /**
     * Added with String as a parameter
     *
     **/
    private Map<String, Object> updateBulkMandatorySavingsDuePayments(final String json, final PaymentDetail paymentDetail) {
        final Map<String, Object> changes = new HashMap<>();
        final Collection<SavingsAccountTransactionDTO> savingsTransactions = this.accountAssembler
                .assembleBulkMandatorySavingsAccountTransactionDTOs(json, paymentDetail);
        List<Long> depositTransactionIds = new ArrayList<>();
        for (SavingsAccountTransactionDTO savingsAccountTransactionDTO : savingsTransactions) {
            try {
                SavingsAccountTransaction savingsAccountTransaction = this.accountWritePlatformService
                        .mandatorySavingsAccountDeposit(savingsAccountTransactionDTO);
                depositTransactionIds.add(savingsAccountTransaction.getId());
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        changes.put("SavingsTransactions", depositTransactionIds);
        return changes;
    }

    /**
     * Added overloaded method to accept new parameter data type
     *
     */
    @Override
    public CommandProcessingResult saveIndividualCollectionSheet(Command<SaveCollectionSheetRequest> command) {

        /**
         *
         * Comment No: 1 Added Gson to keep the changes in this ticket minimal There are major command changes that
         * needs to be done in the changes.putAll(updateBulkDisbursals(jsonCommand)); method
         *
         */
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String json = gson.toJson(command.getPayload().getRequest());

        this.transactionDataValidator.validateIndividualCollectionSheet(json);

        final Map<String, Object> changes = new HashMap<>();
        changes.put("locale", command.getPayload().getRequest().getLocale());
        changes.put("dateFormat", command.getPayload().getRequest().getDateFormat());

        /**
         *
         * Comment No: 2 Removed as the original logic also doesn't have the parameter from the API as note.
         *
         **/
        // final String noteText = command.stringValueOfParameterNamed("note");
        // if (StringUtils.isNotBlank(noteText)) {
        // changes.put("note", noteText);
        // }

        /**
         * Added below for compatibility with the existing logic
         *
         */
        ////////////////////// this block of code is not required if comment no. 1 above is addressed======
        final String payload = toApiJsonSerializer.serialize(command.getPayload().getRequest());
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(payload);
        final CommandWrapper wrapper = builder.saveIndividualCollectionSheet().build();
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);

        final JsonCommand jsonCommand = JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, wrapper.getEntityName(),
                wrapper.getEntityId(), wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(), wrapper.getLoanId(),
                wrapper.getSavingsId(), wrapper.getTransactionId(), wrapper.getHref(), wrapper.getProductId(), wrapper.getCreditBureauId(),
                wrapper.getOrganisationCreditBureauId(), wrapper.getJobName(), wrapper.getLoanExternalId());

        final PaymentDetail paymentDetail = null;
        ///////////////////// ================================================

        changes.putAll(updateBulkRepayments(json, paymentDetail));

        /**
         * Comment No: 3 refer above comments
         *
         * This method needs major changes which can be incremental and aligned with the existing effort.
         *
         **/
        changes.putAll(updateBulkDisbursals(jsonCommand));

        changes.putAll(updateBulkMandatorySavingsDuePayments(json, paymentDetail));

        return new CommandProcessingResultBuilder().withCommandId((long) command.getId().hashCode()).with(changes).with(changes).build();
    }

}
