/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.service;

import java.util.HashMap;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CollectionSheetWritePlatformServiceJpaRepositoryImpl implements CollectionSheetWritePlatformService {

    private final LoanWritePlatformService loanWritePlatformService;
    private final CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer bulkRepaymentCommandFromApiJsonDeserializer;
    private final CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer bulkDisbursalCommandFromApiJsonDeserializer;
    private final CollectionSheetTransactionDataValidator transactionDataValidator;
    private final MeetingWritePlatformService meetingWritePlatformService;

    @Autowired
    public CollectionSheetWritePlatformServiceJpaRepositoryImpl(LoanWritePlatformService loanWritePlatformService,
            CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer bulkRepaymentCommandFromApiJsonDeserializer,
            CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer bulkDisbursalCommandFromApiJsonDeserializer,
            final CollectionSheetTransactionDataValidator transactionDataValidator, final MeetingWritePlatformService meetingWritePlatformService) {
        this.loanWritePlatformService = loanWritePlatformService;
        this.bulkRepaymentCommandFromApiJsonDeserializer = bulkRepaymentCommandFromApiJsonDeserializer;
        this.bulkDisbursalCommandFromApiJsonDeserializer = bulkDisbursalCommandFromApiJsonDeserializer;
        this.transactionDataValidator = transactionDataValidator;
        this.meetingWritePlatformService = meetingWritePlatformService;
    }

    @Override
    public CommandProcessingResult updateCollectionSheet(JsonCommand command) {
        
        this.transactionDataValidator.validateTransaction(command);
        
        Map<String, Object> changes = new HashMap<String, Object>();
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());
        
        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }
        
        changes.putAll(updateBulkReapayments(command));

        changes.putAll(updateBulkDisbursals(command));
        
        this.meetingWritePlatformService.updateCollectionSheetAttendance(command);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(command.entityId()) //
                .withGroupId(command.entityId()) //
                .with(changes)
                .with(changes).build();
    }

    private Map<String, Object> updateBulkReapayments(JsonCommand command) {
        Map<String, Object> changes = new HashMap<String, Object>();
        final CollectionSheetBulkRepaymentCommand bulkRepaymentCommand = this.bulkRepaymentCommandFromApiJsonDeserializer.commandFromApiJson(command.json());
        changes.putAll(this.loanWritePlatformService.makeLoanBulkRepayment(bulkRepaymentCommand));
        return changes;
    }

    private Map<String, Object> updateBulkDisbursals(JsonCommand command) {
        Map<String, Object> changes = new HashMap<String, Object>();
        final CollectionSheetBulkDisbursalCommand bulkDisbursalCommand = this.bulkDisbursalCommandFromApiJsonDeserializer.commandFromApiJson(command.json());
        changes.putAll(this.loanWritePlatformService.bulkLoanDisbursal(command, bulkDisbursalCommand));
        return changes;
    }
}
