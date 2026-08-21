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
package org.apache.fineract.infrastructure.event.external.service.serialization.mapper.workingcapitalloan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanJournalEntryDataV1;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.AvroDateTimeMapper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class WorkingCapitalLoanJournalEntryDataMapperTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2024, 2, 1);
    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2024, 1, 15);

    private final WorkingCapitalLoanJournalEntryDataMapper mapper = new WorkingCapitalLoanJournalEntryDataMapperImpl(
            new AvroDateTimeMapper());

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Europe/Budapest", null));
        final HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        businessDates.put(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE);
        businessDates.put(BusinessDateType.COB_DATE, BUSINESS_DATE);
        ThreadLocalContextUtil.setBusinessDates(businessDates);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void map_nullSource_returnsNull() {
        assertNull(mapper.map((JournalEntry) null));
    }

    @Test
    void map_journalEntry_coversAllFields() {
        final JournalEntry source = workingCapitalJournalEntry();

        final WorkingCapitalLoanJournalEntryDataV1 result = mapper.map(source);

        assertNotNull(result);
        assertEquals(500L, result.getId());
        assertEquals(101L, result.getLoanId());
        assertEquals("WC777", result.getTransactionId());
        assertEquals(777L, result.getWcLoanTransactionId());
        assertEquals(new BigDecimal("125.50"), result.getAmount());
        assertEquals("EUR", result.getCurrencyCode());
        assertEquals(9L, result.getOfficeId());
        assertEquals("2024-01-15", result.getTransactionDate());
        assertEquals("2024-02-01", result.getSubmittedOnDate());
        assertEquals(Boolean.FALSE, result.getReversed());
        assertEquals(600L, result.getReversalId());

        assertNotNull(result.getType());
        assertEquals(JournalEntryType.DEBIT.name(), result.getType().getId());
        assertEquals(JournalEntryType.DEBIT.getCode(), result.getType().getCode());
        assertEquals(JournalEntryType.DEBIT.name(), result.getType().getValue());

        assertNotNull(result.getGlAccount());
        assertEquals(30L, result.getGlAccount().getId());
        assertEquals("Loans Receivable", result.getGlAccount().getName());
        assertEquals("112601", result.getGlAccount().getGlCode());
        assertEquals(31L, result.getGlAccount().getParentId());
        assertEquals(Boolean.FALSE, result.getGlAccount().getDisabled());
        assertEquals(Boolean.TRUE, result.getGlAccount().getManualEntriesAllowed());
        assertEquals("receivables", result.getGlAccount().getDescription());
        assertEquals(GLAccountType.ASSET.getValue(), result.getGlAccount().getType().getId());
        assertEquals(GLAccountUsage.DETAIL.getValue(), result.getGlAccount().getUsage().getId());

        assertNotNull(result.getPaymentDetailData());
        assertEquals("acc-1", result.getPaymentDetailData().getAccountNumber());
        assertEquals("chk-1", result.getPaymentDetailData().getCheckNumber());
        assertEquals("rt-1", result.getPaymentDetailData().getRoutingCode());
        assertEquals("rcp-1", result.getPaymentDetailData().getReceiptNumber());
        assertEquals("bank-1", result.getPaymentDetailData().getBankNumber());
    }

    @Test
    void map_legacyTransactionId_leavesWorkingCapitalTransactionIdNull() {
        final JournalEntry source = workingCapitalJournalEntry();
        ReflectionTestUtils.setField(source, "transactionId", "12345");

        assertNull(mapper.map(source).getWcLoanTransactionId());
    }

    private JournalEntry workingCapitalJournalEntry() {
        final Office office = mock(Office.class);
        when(office.getId()).thenReturn(9L);

        final PaymentDetail paymentDetail = PaymentDetail.instance(null, "acc-1", "chk-1", "rt-1", "rcp-1", "bank-1");

        final JournalEntry entry = JournalEntry.createNew(office, paymentDetail, glAccount(), "EUR", "WC777", false, TRANSACTION_DATE,
                JournalEntryType.DEBIT, new BigDecimal("125.50"), null, PortfolioProductType.WORKING_CAPITAL_LOAN.getValue(), 101L, null,
                null, null, null, null);
        ReflectionTestUtils.setField(entry, "id", 500L);

        final JournalEntry reversal = JournalEntry.createNew(office, null, glAccount(), "EUR", "WC777", false, TRANSACTION_DATE,
                JournalEntryType.CREDIT, new BigDecimal("125.50"), null, PortfolioProductType.WORKING_CAPITAL_LOAN.getValue(), 101L, null,
                null, null, null, null);
        ReflectionTestUtils.setField(reversal, "id", 600L);
        entry.setReversalJournalEntry(reversal);

        return entry;
    }

    private GLAccount glAccount() {
        final GLAccount parent = mock(GLAccount.class);
        when(parent.getId()).thenReturn(31L);

        final GLAccount account = mock(GLAccount.class);
        when(account.getId()).thenReturn(30L);
        when(account.getName()).thenReturn("Loans Receivable");
        when(account.getGlCode()).thenReturn("112601");
        when(account.getParent()).thenReturn(parent);
        when(account.isDisabled()).thenReturn(false);
        when(account.isManualEntriesAllowed()).thenReturn(true);
        when(account.getDescription()).thenReturn("receivables");
        when(account.getType()).thenReturn(GLAccountType.ASSET.getValue());
        when(account.getUsage()).thenReturn(GLAccountUsage.DETAIL.getValue());
        return account;
    }
}
