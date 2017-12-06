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

import org.apache.fineract.infrastructure.bulkimport.data.BulkImportEvent;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocument;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentCommand;
import org.apache.fineract.infrastructure.documentmanagement.domain.Document;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentWritePlatformService;
import org.apache.fineract.infrastructure.security.service.TenantDetailsService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class BulkImportEventListener implements ApplicationListener<BulkImportEvent> {


    private final TenantDetailsService tenantDetailsService;
    private final ApplicationContext applicationContext;
    private final ImportDocumentRepository importRepository;
    private final DocumentWritePlatformService documentService;

    @Autowired
    public BulkImportEventListener(
            final TenantDetailsService tenantDetailsService,
            final ApplicationContext context,
            final ImportDocumentRepository importRepository,
            final DocumentWritePlatformService documentService) {
        this.tenantDetailsService = tenantDetailsService;
        this.applicationContext = context;
        this.importRepository = importRepository;
        this.documentService = documentService;
    }

    @Override
    public void onApplicationEvent(final BulkImportEvent event) {

        final String tenantIdentifier = event.getTenantIdentifier();
        final FineractPlatformTenant tenant = this.tenantDetailsService
                .loadTenantById(tenantIdentifier);
        ThreadLocalContextUtil.setTenant(tenant);
        ImportHandler importHandler = null;
        final ImportDocument importDocument = this.importRepository.findOne(event.getImportId());
        final GlobalEntityType entityType = GlobalEntityType.fromInt(importDocument.getEntityType());

        switch(entityType) {
            case OFFICES :
                importHandler = this.applicationContext.getBean("officeImportHandler", ImportHandler.class);
                break;
            case CENTERS:
                importHandler=this.applicationContext.getBean("centerImportHandler",ImportHandler.class);
                break;
            case CHART_OF_ACCOUNTS:
                importHandler=this.applicationContext.getBean("chartOfAccountsImportHandler",ImportHandler.class);
                break;
            case CLIENTS_ENTTTY:
                importHandler=this.applicationContext.getBean("clientEntityImportHandler",ImportHandler.class);
                break;
            case CLIENTS_PERSON:
                importHandler=this.applicationContext.getBean("clientPersonImportHandler",ImportHandler.class);
                break;
            case FIXED_DEPOSIT_ACCOUNTS:
                importHandler=this.applicationContext.getBean("fixedDepositImportHandler",ImportHandler.class);
                break;
            case FIXED_DEPOSIT_TRANSACTIONS:
                importHandler=this.applicationContext.getBean("fixedDepositTransactionImportHandler",ImportHandler.class);
                break;
            case GROUPS:
                importHandler=this.applicationContext.getBean("groupImportHandler",ImportHandler.class);
                break;
            case GUARANTORS:
                importHandler=this.applicationContext.getBean("guarantorImportHandler",ImportHandler.class);
                break;
            case GL_JOURNAL_ENTRIES:
                importHandler=this.applicationContext.getBean("journalEntriesImportHandler",ImportHandler.class);
                break;
            case LOANS:
                importHandler=this.applicationContext.getBean("loanImportHandler",ImportHandler.class);
                break;
            case LOAN_TRANSACTIONS:
                importHandler=this.applicationContext.getBean("loanRepaymentImportHandler",ImportHandler.class);
                break;
            case RECURRING_DEPOSIT_ACCOUNTS:
                importHandler=this.applicationContext.getBean("recurringDepositImportHandler",ImportHandler.class);
                break;
            case RECURRING_DEPOSIT_ACCOUNTS_TRANSACTIONS:
                importHandler=this.applicationContext.getBean("recurringDepositTransactionImportHandler",ImportHandler.class);
                break;
            case SAVINGS_ACCOUNT:
                importHandler=this.applicationContext.getBean("savingsImportHandler",ImportHandler.class);
                break;
            case SAVINGS_TRANSACTIONS:
                importHandler=this.applicationContext.getBean("savingsTransactionImportHandler",ImportHandler.class);
                break;
            case SHARE_ACCOUNTS:
                importHandler=this.applicationContext.getBean("sharedAccountImportHandler",ImportHandler.class);
                break;
            case STAFF:
                importHandler=this.applicationContext.getBean("staffImportHandler",ImportHandler.class);
                break;
            case USERS:
                importHandler=this.applicationContext.getBean("userImportHandler",ImportHandler.class);
                break;
            default : throw new GeneralPlatformDomainRuleException("error.msg.unable.to.find.resource",
                    "Unable to find requested resource");

        }

        final Workbook workbook = event.getWorkbook();
        final Count count = importHandler.process(workbook, event.getLocale(), event.getDateFormat());
        importDocument.update(DateUtils.getLocalDateTimeOfTenant(), count.getSuccessCount(), count.getErrorCount());
        this.importRepository.save(importDocument);

        final Set<String> modifiedParams = new HashSet<>();
        modifiedParams.add("fileName");
        modifiedParams.add("size");
        modifiedParams.add("type");
        modifiedParams.add("location");
        Document document = importDocument.getDocument();

        DocumentCommand documentCommand = new DocumentCommand(modifiedParams, document.getId(), entityType.name(), null,
                document.getName(), document.getFileName(), document.getSize(), URLConnection.guessContentTypeFromName(document.getFileName()),
                null, null);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            try {
                workbook.write(bos);
            } finally {
                bos.close();
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
        byte[] bytes = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        this.documentService.updateDocument(documentCommand, bis);
    }

}