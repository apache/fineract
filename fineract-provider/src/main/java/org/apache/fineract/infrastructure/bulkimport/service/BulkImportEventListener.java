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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.bulkimport.data.BulkImportEvent;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocument;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentCommand;
import org.apache.fineract.infrastructure.documentmanagement.domain.Document;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentWritePlatformService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkImportEventListener implements ApplicationListener<BulkImportEvent> {

    private final ApplicationContext applicationContext;
    private final ImportDocumentRepository importRepository;
    private final DocumentWritePlatformService documentService;

    @Override
    public void onApplicationEvent(final BulkImportEvent event) {
        try {
            ThreadLocalContextUtil.init(event.getContext());
            final ImportDocument importDocument = this.importRepository.findById(event.getImportId()).orElse(null);
            final GlobalEntityType entityType = GlobalEntityType.fromInt(importDocument.getEntityType());

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

            final Workbook workbook = event.getWorkbook();
            final Count count = importHandler.process(workbook, event.getLocale(), event.getDateFormat());
            importDocument.update(DateUtils.getLocalDateTimeOfTenant(), count.getSuccessCount(), count.getErrorCount());
            this.importRepository.saveAndFlush(importDocument);

            final Set<String> modifiedParams = new HashSet<>();
            modifiedParams.add("fileName");
            modifiedParams.add("size");
            modifiedParams.add("type");
            modifiedParams.add("location");
            Document document = importDocument.getDocument();

            DocumentCommand documentCommand = new DocumentCommand(modifiedParams, document.getId(), entityType.name(), null,
                    document.getName(), document.getFileName(), document.getSize(),
                    URLConnection.guessContentTypeFromName(document.getFileName()), null, null);

            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                try {
                    workbook.write(bos);
                } finally {
                    bos.close();
                }
            } catch (IOException io) {
                log.error("Problem occurred in onApplicationEvent function", io);
            }
            byte[] bytes = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            this.documentService.updateDocument(documentCommand, bis);
        } finally {
            ThreadLocalContextUtil.reset();
        }
    }

}
