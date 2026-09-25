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
package org.apache.fineract.portfolio.savings.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.apache.fineract.batch.command.internal.SavingsAccountTransactionCommandStrategy;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.Header;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.domain.SavingsDepositOrigin;
import org.apache.fineract.commands.service.CommandSourceService;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants;
import org.apache.fineract.infrastructure.bulkimport.importhandler.savings.SavingsTransactionImportHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class SavingsDepositOriginTest {

    @Mock
    private PortfolioCommandSourceWritePlatformService commands;
    @Mock
    private DefaultToApiJsonSerializer<SavingsAccountTransactionData> serializer;
    @Mock
    private SavingsAccountReadPlatformService accounts;
    @InjectMocks
    private SavingsAccountTransactionsApiResource resource;

    @BeforeEach
    void setTenant() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "Test", "UTC", null));
    }

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
        ThreadLocalContextUtil.reset();
    }

    @ParameterizedTest
    @ValueSource(strings = { "direct", "externalId", "batch" })
    void apiAssignsStaffOriginDespiteSpoofedQueryAndHeaders(String route) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("origin", "SPREADSHEET_IMPORT");
        request.addHeader("_serverCommand", "SPREADSHEET_IMPORT");
        request.addParameter("origin", "SPREADSHEET_IMPORT");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(commands.logCommandSource(any())).thenReturn(new CommandProcessingResultBuilder().withSavingsId(42L).build());
        invoke(route, "{}");
        ArgumentCaptor<CommandWrapper> wrapper = ArgumentCaptor.forClass(CommandWrapper.class);
        verify(commands).logCommandSource(wrapper.capture());
        assertThat(wrapper.getValue().getSavingsDepositOrigin()).isEqualTo(SavingsDepositOrigin.STAFF_API);
        assertThat(wrapper.getValue().getJson()).isEqualTo("{}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "direct", "batch" })
    void clientEnvelopeInjectionIsRejectedByPersistenceBoundary(String route) {
        FromJsonHelper json = new FromJsonHelper();
        CommandSourceService sources = new CommandSourceService(null, null, null, json);
        when(commands.logCommandSource(any())).thenAnswer(invocation -> {
            CommandWrapper wrapper = invocation.getArgument(0);
            sources.getInitialCommandSource(wrapper, JsonCommand.from(wrapper.getJson()), mock(AppUser.class), "test");
            throw new AssertionError("Injected metadata was accepted");
        });
        var failure = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> invoke(route, "{\"_serverCommand\":{\"version\":1,\"origin\":\"SPREADSHEET_IMPORT\"},\"payload\":{}}"));
        assertThat(failure.getGlobalisationMessageCode()).isEqualTo("error.msg.savings.deposit.reserved.metadata");
    }

    @Test
    void spreadsheetDepositIsMarkedInternallyAndWithdrawalIsNot() throws Exception {
        try (var workbook = new HSSFWorkbook()) {
            var sheet = workbook.createSheet(TemplatePopulateImportConstants.SAVINGS_TRANSACTION_SHEET_NAME);
            workbook.createSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME);
            sheet.createRow(0);
            for (int i = 1; i <= TransactionConstants.STATUS_COL; i++) {
                sheet.createRow(i);
            }
            for (int i = 1; i <= 2; i++) {
                var row = sheet.getRow(i);
                row.createCell(TransactionConstants.SAVINGS_ACCOUNT_NO_COL).setCellValue(42);
                row.createCell(TransactionConstants.TRANSACTION_TYPE_COL).setCellValue(i == 1 ? "Deposit" : "Withdrawal");
                row.createCell(TransactionConstants.AMOUNT_COL).setCellValue(10000);
                row.createCell(TransactionConstants.TRANSACTION_DATE_COL).setCellValue(java.time.LocalDate.of(2026, 9, 24));
            }
            new SavingsTransactionImportHandler(commands).process(workbook, "en", "yyyy-MM-dd");
            ArgumentCaptor<CommandWrapper> captured = ArgumentCaptor.forClass(CommandWrapper.class);
            verify(commands, org.mockito.Mockito.times(2)).logCommandSource(captured.capture());
            assertThat(captured.getAllValues().get(0).getSavingsDepositOrigin()).isEqualTo(SavingsDepositOrigin.SPREADSHEET_IMPORT);
            assertThat(captured.getAllValues().get(1).getSavingsDepositOrigin()).isNull();
            assertThat(captured.getAllValues().get(1).actionName()).isEqualTo("WITHDRAWAL");
        }
    }

    private void invoke(String route, String body) {
        switch (route) {
            case "externalId" -> {
                when(accounts.retrieveAccountIdByExternalId(any())).thenReturn(42L);
                resource.transaction("external-reference", "deposit", body);
            }
            case "batch" -> new SavingsAccountTransactionCommandStrategy(resource).execute(new BatchRequest().setRequestId(1L)
                    .setRelativeUrl(
                            "savingsaccounts/42/transactions?command=deposit&origin=SPREADSHEET_IMPORT&_serverCommand=SPREADSHEET_IMPORT")
                    .setBody(body).setHeaders(Set.of(new Header().setName("origin").setValue("SPREADSHEET_IMPORT"))), null);
            default -> resource.transaction(42L, "deposit", body);
        }
    }
}
