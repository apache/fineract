/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.mifosplatform.portfolio.collectionsheet.data.CollectionSheetTransactionDataValidator;
import org.mifosplatform.portfolio.collectionsheet.serialization.CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.collectionsheet.serialization.CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanaccount.service.LoanWritePlatformService;
import org.mifosplatform.portfolio.meeting.service.MeetingWritePlatformService;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetailAssembler;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.mifosplatform.portfolio.savings.domain.DepositAccountAssembler;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransaction;
import org.mifosplatform.portfolio.savings.service.DepositAccountWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CollectionSheetWritePlatformServiceJpaRepositoryImpl implements CollectionSheetWritePlatformService {

    private final LoanWritePlatformService loanWritePlatformService;
    private final CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer bulkRepaymentCommandFromApiJsonDeserializer;
    private final CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer bulkDisbursalCommandFromApiJsonDeserializer;
    private final CollectionSheetTransactionDataValidator transactionDataValidator;
    private final MeetingWritePlatformService meetingWritePlatformService;
    private final DepositAccountAssembler accountAssembler;
    private final DepositAccountWritePlatformService accountWritePlatformService;
    private final PaymentDetailAssembler paymentDetailAssembler;

    @Autowired
    public CollectionSheetWritePlatformServiceJpaRepositoryImpl(final LoanWritePlatformService loanWritePlatformService,
            final CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer bulkRepaymentCommandFromApiJsonDeserializer,
            final CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer bulkDisbursalCommandFromApiJsonDeserializer,
            final CollectionSheetTransactionDataValidator transactionDataValidator,
            final MeetingWritePlatformService meetingWritePlatformService, final DepositAccountAssembler accountAssembler,
            final DepositAccountWritePlatformService accountWritePlatformService, final PaymentDetailAssembler paymentDetailAssembler) {
        this.loanWritePlatformService = loanWritePlatformService;
        this.bulkRepaymentCommandFromApiJsonDeserializer = bulkRepaymentCommandFromApiJsonDeserializer;
        this.bulkDisbursalCommandFromApiJsonDeserializer = bulkDisbursalCommandFromApiJsonDeserializer;
        this.transactionDataValidator = transactionDataValidator;
        this.meetingWritePlatformService = meetingWritePlatformService;
        this.accountAssembler = accountAssembler;
        this.accountWritePlatformService = accountWritePlatformService;
        this.paymentDetailAssembler = paymentDetailAssembler;
    }

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

        final PaymentDetail paymentDetail = this.paymentDetailAssembler.fetchPaymentDetail(command.parsedJson().getAsJsonObject());
        changes.putAll(updateBulkReapayments(command, paymentDetail));

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

        changes.putAll(updateBulkReapayments(command, paymentDetail));

        changes.putAll(updateBulkDisbursals(command));

        changes.putAll(updateBulkMandatorySavingsDuePayments(command, paymentDetail));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(command.entityId()) //
                .withGroupId(command.entityId()) //
                .with(changes).with(changes).build();
    }

    private Map<String, Object> updateBulkReapayments(final JsonCommand command, final PaymentDetail paymentDetail) {
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
                SavingsAccountTransaction savingsAccountTransaction =  this.accountWritePlatformService.mandatorySavingsAccountDeposit(savingsAccountTransactionDTO);
                depositTransactionIds.add(savingsAccountTransaction.getId());
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        changes.put("SavingsTransactions", depositTransactionIds);
        return changes;
    }

}
