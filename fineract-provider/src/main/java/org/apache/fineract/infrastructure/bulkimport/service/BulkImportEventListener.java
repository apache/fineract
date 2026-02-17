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
package org.apache.fineract.infrastructure.bulkimport.service;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.bulkimport.data.BulkImportEvent;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.contentstore.util.ContentPipe;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentUpdateCommand;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentCreateResponse;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentUpdateRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkImportEventListener implements ApplicationListener<BulkImportEvent> {

    private final ApplicationContext applicationContext;
    private final ImportDocumentRepository importRepository;
    private final ContentPipe pipe;
    private final CommandPipeline commandPipeline;

    @Override
    public void onApplicationEvent(final BulkImportEvent event) {
        try {
            ThreadLocalContextUtil.init(event.getContext());

            final GlobalEntityType entityType = GlobalEntityType.fromInt(event.getImportDocument().getEntityType());

            final ImportHandler importHandler = switch (entityType) {
                case OFFICES -> this.applicationContext.getBean("officeImportHandler", ImportHandler.class);
                case CENTERS -> this.applicationContext.getBean("centerImportHandler", ImportHandler.class);
                case CHART_OF_ACCOUNTS -> this.applicationContext.getBean("chartOfAccountsImportHandler", ImportHandler.class);
                case CLIENTS_ENTITY -> this.applicationContext.getBean("clientEntityImportHandler", ImportHandler.class);
                case CLIENTS_PERSON -> this.applicationContext.getBean("clientPersonImportHandler", ImportHandler.class);
                case FIXED_DEPOSIT_ACCOUNTS -> this.applicationContext.getBean("fixedDepositImportHandler", ImportHandler.class);
                case FIXED_DEPOSIT_TRANSACTIONS ->
                    this.applicationContext.getBean("fixedDepositTransactionImportHandler", ImportHandler.class);
                case GROUPS -> this.applicationContext.getBean("groupImportHandler", ImportHandler.class);
                case GUARANTORS -> this.applicationContext.getBean("guarantorImportHandler", ImportHandler.class);
                case GL_JOURNAL_ENTRIES -> this.applicationContext.getBean("journalEntriesImportHandler", ImportHandler.class);
                case LOANS -> this.applicationContext.getBean("loanImportHandler", ImportHandler.class);
                case LOAN_TRANSACTIONS -> this.applicationContext.getBean("loanRepaymentImportHandler", ImportHandler.class);
                case RECURRING_DEPOSIT_ACCOUNTS -> this.applicationContext.getBean("recurringDepositImportHandler", ImportHandler.class);
                case RECURRING_DEPOSIT_ACCOUNTS_TRANSACTIONS ->
                    this.applicationContext.getBean("recurringDepositTransactionImportHandler", ImportHandler.class);
                case SAVINGS_ACCOUNT -> this.applicationContext.getBean("savingsImportHandler", ImportHandler.class);
                case SAVINGS_TRANSACTIONS -> this.applicationContext.getBean("savingsTransactionImportHandler", ImportHandler.class);
                case SHARE_ACCOUNTS -> this.applicationContext.getBean("sharedAccountImportHandler", ImportHandler.class);
                case STAFF -> this.applicationContext.getBean("staffImportHandler", ImportHandler.class);
                case USERS -> this.applicationContext.getBean("userImportHandler", ImportHandler.class);
                default ->
                    throw new GeneralPlatformDomainRuleException("error.msg.unable.to.find.resource", "Unable to find requested resource");
            };

            final var count = importHandler.process(event.getWorkbook(), event.getLocale(), event.getDateFormat());

            event.getImportDocument().update(DateUtils.getLocalDateTimeOfTenant(), count.getSuccessCount(), count.getErrorCount());

            final var pipedInputStream = pipe.pipe(output -> {
                event.getWorkbook().write(output);
            });

            final var command = new DocumentUpdateCommand();

            command.setPayload(DocumentUpdateRequest.builder().id(event.getImportDocument().getDocumentId()).entityId(event.getEntityId())
                    .entityType("IMPORT").stream(pipedInputStream).build());

            final Supplier<DocumentCreateResponse> response = commandPipeline.send(command);

            response.get();

            importRepository.saveAndFlush(event.getImportDocument());
        } catch (Exception e) {
            log.error("Bulk import error:", e);
        } finally {
            ThreadLocalContextUtil.reset();
        }
    }
}
