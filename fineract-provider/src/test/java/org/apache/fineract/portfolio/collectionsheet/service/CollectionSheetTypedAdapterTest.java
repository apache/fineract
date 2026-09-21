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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.group.data.CollectionSheetLoanTransactionItem;
import org.apache.fineract.portfolio.group.data.CollectionSheetSavingsTransactionItem;
import org.apache.fineract.portfolio.group.data.GroupSaveCollectionSheetRequest;
import org.junit.jupiter.api.Test;

class CollectionSheetTypedAdapterTest {

    @Test
    void bridgeSerialisesOnlyLegacyKeysAndDropsId() {
        GroupSaveCollectionSheetRequest request = GroupSaveCollectionSheetRequest.builder().id(5L).calendarId(7L)
                .transactionDate("01 January 2024").dateFormat("dd MMMM yyyy").locale("en")
                .bulkRepaymentTransactions(
                        List.of(CollectionSheetLoanTransactionItem.builder().loanId(1L).transactionAmount(new BigDecimal("10")).build()))
                .isTransactionDateOnNonMeetingDate(true).build();

        JsonObject json = CollectionSheetWritePlatformServiceJpaRepositoryImpl.toLegacyJson(request, new FromJsonHelper())
                .getAsJsonObject();

        assertFalse(json.has("id"));
        assertEquals(7L, json.get("calendarId").getAsLong());
        assertEquals("01 January 2024", json.get("transactionDate").getAsString());
        assertTrue(json.get("isTransactionDateOnNonMeetingDate").getAsBoolean());
        assertEquals(1L, json.getAsJsonArray("bulkRepaymentTransactions").get(0).getAsJsonObject().get("loanId").getAsLong());
        assertFalse(json.has("note"));
    }

    @Test
    void bridgeKeepsPerTransactionExternalIdAndPaymentDetail() {
        GroupSaveCollectionSheetRequest request = GroupSaveCollectionSheetRequest.builder().id(5L).transactionDate("01 January 2024")
                .dateFormat("dd MMMM yyyy").locale("en")
                .bulkRepaymentTransactions(List.of(CollectionSheetLoanTransactionItem.builder().loanId(1L)
                        .transactionAmount(new BigDecimal("10")).externalId("ext-1").paymentTypeId(3L).accountNumber("acc")
                        .checkNumber("chk").routingCode("rt").receiptNumber("rcpt").bankNumber("bank").build()))
                .bulkSavingsDueTransactions(List.of(CollectionSheetSavingsTransactionItem.builder().savingsId(2L)
                        .transactionAmount(new BigDecimal("5")).paymentTypeId(4).accountNumber("sacc").checkNumber("schk")
                        .routingCode("srt").receiptNumber("srcpt").bankNumber("sbank").build()))
                .build();

        JsonObject json = CollectionSheetWritePlatformServiceJpaRepositoryImpl.toLegacyJson(request, new FromJsonHelper())
                .getAsJsonObject();

        JsonObject repayment = json.getAsJsonArray("bulkRepaymentTransactions").get(0).getAsJsonObject();
        assertEquals("ext-1", repayment.get("externalId").getAsString());
        assertEquals(3L, repayment.get("paymentTypeId").getAsLong());
        assertEquals("acc", repayment.get("accountNumber").getAsString());
        assertEquals("chk", repayment.get("checkNumber").getAsString());
        assertEquals("rt", repayment.get("routingCode").getAsString());
        assertEquals("rcpt", repayment.get("receiptNumber").getAsString());
        assertEquals("bank", repayment.get("bankNumber").getAsString());
        JsonObject savings = json.getAsJsonArray("bulkSavingsDueTransactions").get(0).getAsJsonObject();
        assertEquals(4, savings.get("paymentTypeId").getAsInt());
        assertEquals("sacc", savings.get("accountNumber").getAsString());
        assertEquals("schk", savings.get("checkNumber").getAsString());
        assertEquals("srt", savings.get("routingCode").getAsString());
        assertEquals("srcpt", savings.get("receiptNumber").getAsString());
        assertEquals("sbank", savings.get("bankNumber").getAsString());
    }
}
