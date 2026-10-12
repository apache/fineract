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
package org.apache.fineract.infrastructure.jobs.service.retainedearning.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.apache.fineract.accounting.retainedearning.domain.AccountGLJournalEntryAnnualSummary;
import org.apache.fineract.accounting.retainedearning.domain.AccountGLJournalEntryAnnualSummaryRepository;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningConfigurationService;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.data.AccountGLJournalEntryAnnualSummaryData;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.helper.DataParser;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.model.AccountGLJournalEntryAnnualSummaryRecord;
import org.apache.fineract.infrastructure.report.service.ReportingProcessService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetainedEarningDataServiceImplTest {

    @Mock
    private ReportingProcessService reportingProcessService;

    @Mock
    private DataParser dataParser;

    @Mock
    private AccountGLJournalEntryAnnualSummaryRepository retainedEarningSummaryRepository;

    @Mock
    private LoanProductRepository loanProductRepository;

    @Mock
    private RetainedEarningConfigurationService retainedEarningConfigurationService;

    @InjectMocks
    private RetainedEarningDataServiceImpl retainedEarningDataService;

    @Test
    void shouldInsertBatchAndMapAllFieldsCorrectly() {
        LocalDate yearEndDate = LocalDate.of(2024, 12, 31);
        List<AccountGLJournalEntryAnnualSummaryData> summaries = List.of(AccountGLJournalEntryAnnualSummaryData.builder()
                .glAccountCode("400001").productId(10L).officeId(1L).ownerExternalId(ExternalIdFactory.produce("OWNER1"))
                .openingBalanceAmount(new BigDecimal("1000.50")).yearEndDate(yearEndDate).currencyCode("USD").manualEntry(false).build());

        retainedEarningDataService.insertRetainedEarningSummaryBatch(summaries);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AccountGLJournalEntryAnnualSummary>> captor = ArgumentCaptor.forClass(List.class);
        verify(retainedEarningSummaryRepository).saveAll(captor.capture());

        List<AccountGLJournalEntryAnnualSummary> savedEntities = captor.getValue();
        assertEquals(1, savedEntities.size());

        AccountGLJournalEntryAnnualSummary entity = savedEntities.get(0);
        assertEquals("400001", entity.getGlCode());
        assertEquals(10L, entity.getProductId());
        assertEquals(1L, entity.getOfficeId());
        assertEquals(ExternalIdFactory.produce("OWNER1"), entity.getOwnerExternalId());
        assertEquals(new BigDecimal("1000.50"), entity.getOpeningBalanceAmount());
        assertEquals(yearEndDate, entity.getYearEndDate());
        assertEquals("USD", entity.getCurrencyCode());
    }

    @Test
    void shouldInsertMultipleRecordsInBatch() {
        LocalDate yearEndDate = LocalDate.of(2024, 12, 31);
        List<AccountGLJournalEntryAnnualSummaryData> summaries = Arrays.asList(
                AccountGLJournalEntryAnnualSummaryData.builder().glAccountCode("400001").productId(10L).officeId(1L)
                        .ownerExternalId(ExternalIdFactory.produce("OWNER1")).openingBalanceAmount(BigDecimal.valueOf(1000))
                        .yearEndDate(yearEndDate).currencyCode("USD").build(),
                AccountGLJournalEntryAnnualSummaryData.builder().glAccountCode("500001").productId(10L).officeId(1L)
                        .ownerExternalId(ExternalIdFactory.produce("OWNER2")).openingBalanceAmount(BigDecimal.valueOf(2000))
                        .yearEndDate(yearEndDate).currencyCode("EUR").build());

        retainedEarningDataService.insertRetainedEarningSummaryBatch(summaries);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AccountGLJournalEntryAnnualSummary>> captor = ArgumentCaptor.forClass(List.class);
        verify(retainedEarningSummaryRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void shouldSkipSaveWhenEmptyBatch() {
        retainedEarningDataService.insertRetainedEarningSummaryBatch(List.of());
        verifyNoInteractions(retainedEarningSummaryRepository);
    }

    @Test
    void shouldSkipSaveWhenNullBatch() {
        retainedEarningDataService.insertRetainedEarningSummaryBatch(null);
        verifyNoInteractions(retainedEarningSummaryRepository);
    }

    @Test
    void shouldFetchTrialBalanceDataAndParseResponse() throws Exception {
        String reportName = "Trial Balance Summary Report with Asset Owner";
        LocalDate fiscalYearEnd = LocalDate.of(2024, 12, 31);

        Response mockResponse = mockOkResponse("{\"columnHeaders\":[], \"data\":[]}");
        when(reportingProcessService.processRequest(eq(reportName), any())).thenReturn(mockResponse);
        when(dataParser.parse(anyString())).thenReturn(List.of());

        List<AccountGLJournalEntryAnnualSummaryData> result = retainedEarningDataService.fetchTrialBalanceData(reportName, fiscalYearEnd);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reportingProcessService).processRequest(eq(reportName), any());
    }

    @Test
    void shouldFetchTrialBalanceDataAndMapRecords() throws Exception {
        String reportName = "Test Report";
        LocalDate fiscalYearEnd = LocalDate.of(2024, 12, 31);

        Response mockResponse = mockOkResponse("json-content");
        when(reportingProcessService.processRequest(eq(reportName), any())).thenReturn(mockResponse);

        List<AccountGLJournalEntryAnnualSummaryRecord> parsedRecords = List
                .of(AccountGLJournalEntryAnnualSummaryRecord.builder().postingDate("2024-12-31").product("TestProduct").glAcct("400001")
                        .assetOwner(ExternalIdFactory.produce("OWNER1")).endingBalance(BigDecimal.valueOf(1200)).build());

        when(retainedEarningConfigurationService.getOfficeId()).thenReturn(1L);
        when(dataParser.parse(anyString())).thenReturn(parsedRecords);

        List<AccountGLJournalEntryAnnualSummaryData> result = retainedEarningDataService.fetchTrialBalanceData(reportName, fiscalYearEnd);

        assertEquals(1, result.size());
        AccountGLJournalEntryAnnualSummaryData data = result.getFirst();
        assertEquals("400001", data.getGlAccountCode());
        assertEquals("TestProduct", data.getProductName());
        assertEquals(1L, data.getOfficeId());
        assertEquals(ExternalIdFactory.produce("OWNER1"), data.getOwnerExternalId());
        assertEquals(new BigDecimal("1200").negate(), data.getOpeningBalanceAmount());
        assertEquals(new BigDecimal("1200"), data.getEndingBalanceAmount());
        assertEquals(LocalDate.of(2024, 12, 31), data.getYearEndDate());
        assertFalse(data.getManualEntry());
    }

    @Test
    void shouldThrowExceptionWhenParsingFails() throws Exception {
        String reportName = "Test Report";
        LocalDate fiscalYearEnd = LocalDate.of(2024, 12, 31);

        Response mockResponse = mockOkResponse("invalid-json");
        when(reportingProcessService.processRequest(eq(reportName), any())).thenReturn(mockResponse);
        when(dataParser.parse(anyString())).thenThrow(new RuntimeException("Parse error"));

        assertThrows(IllegalArgumentException.class, () -> retainedEarningDataService.fetchTrialBalanceData(reportName, fiscalYearEnd));
    }

    @Test
    void shouldThrowExceptionWhenResponseIsNotOk() {
        String reportName = "Test Report";
        LocalDate fiscalYearEnd = LocalDate.of(2024, 12, 31);

        Response mockResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        when(reportingProcessService.processRequest(eq(reportName), any())).thenReturn(mockResponse);

        assertThrows(IllegalStateException.class, () -> retainedEarningDataService.fetchTrialBalanceData(reportName, fiscalYearEnd));
    }

    @Test
    void shouldMapNullCurrencyCodeWithoutError() {
        List<AccountGLJournalEntryAnnualSummaryData> summaries = List.of(AccountGLJournalEntryAnnualSummaryData.builder()
                .glAccountCode("400001").productId(10L).officeId(1L).ownerExternalId(ExternalIdFactory.produce("OWNER1"))
                .openingBalanceAmount(BigDecimal.ZERO).yearEndDate(LocalDate.of(2024, 12, 31)).currencyCode(null).build());

        retainedEarningDataService.insertRetainedEarningSummaryBatch(summaries);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AccountGLJournalEntryAnnualSummary>> captor = ArgumentCaptor.forClass(List.class);
        verify(retainedEarningSummaryRepository).saveAll(captor.capture());

        AccountGLJournalEntryAnnualSummary entity = captor.getValue().get(0);
        assertEquals(null, entity.getCurrencyCode());
    }

    private Response mockOkResponse(String body) {
        Response response = org.mockito.Mockito.mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.getEntity()).thenReturn(body);
        return response;
    }
}
