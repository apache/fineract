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
package org.apache.fineract.infrastructure.jobs.service.retainedearning.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.model.AccountGLJournalEntryAnnualSummaryRecord;
import org.junit.jupiter.api.Test;

class DataParserTest {

    private final DataParser parser = new DataParser();

    @Test
    void shouldParseValidJsonWithMultipleRows() throws Exception {
        // use class-level parser instance
        String json = """
                {
                  "columnHeaders": [
                    {"columnName": "postingdate"},
                    {"columnName": "product"},
                    {"columnName": "glacct"},
                    {"columnName": "description"},
                    {"columnName": "assetowner"},
                    {"columnName": "beginningbalance"},
                    {"columnName": "endingbalance"}
                  ],
                  "data": [
                    {"row": ["2024-12-31", "DE PAYIN30", "400001", "Fee Income", "OWNER1", "1000.50", "1200.75"]},
                    {"row": ["2024-12-31", "DE PAYIN30", "500001", "Interest Expense", "OWNER2", "-500.00", "-300.25"]}
                  ]
                }
                """;

        List<AccountGLJournalEntryAnnualSummaryRecord> records = parser.parse(json);

        assertEquals(2, records.size());

        AccountGLJournalEntryAnnualSummaryRecord first = records.get(0);
        assertEquals("2024-12-31", first.getPostingDate());
        assertEquals("DE PAYIN30", first.getProduct());
        assertEquals("400001", first.getGlAcct());
        assertEquals(ExternalIdFactory.produce("OWNER1"), first.getAssetOwner());
        assertEquals(new BigDecimal("1200.75"), first.getEndingBalance());

        AccountGLJournalEntryAnnualSummaryRecord second = records.get(1);
        assertEquals(ExternalIdFactory.produce("OWNER2"), second.getAssetOwner());
        assertEquals(new BigDecimal("-300.25"), second.getEndingBalance());
    }

    @Test
    void shouldReturnEmptyListForEmptyDataArray() throws Exception {
        // use class-level parser instance
        String json = """
                {
                  "columnHeaders": [
                    {"columnName": "postingdate"},
                    {"columnName": "glacct"}
                  ],
                  "data": []
                }
                """;

        List<AccountGLJournalEntryAnnualSummaryRecord> records = parser.parse(json);

        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    void shouldHandleMissingOptionalColumns() throws Exception {
        // use class-level parser instance
        String json = """
                {
                  "columnHeaders": [
                    {"columnName": "postingdate"},
                    {"columnName": "product"},
                    {"columnName": "glacct"},
                    {"columnName": "description"},
                    {"columnName": "assetowner"}
                  ],
                  "data": [
                    {"row": ["2024-12-31", "TestProduct", "400001", "Fee Income", "OWNER1"]}
                  ]
                }
                """;

        List<AccountGLJournalEntryAnnualSummaryRecord> records = parser.parse(json);

        assertEquals(1, records.size());
        assertEquals(new BigDecimal("0"), records.get(0).getEndingBalance());
    }

    @Test
    void shouldThrowExceptionForMalformedJson() throws Exception {
        // use class-level parser instance
        String malformedJson = "{ this is not valid json }";

        assertThrows(Exception.class, () -> parser.parse(malformedJson));
    }

    @Test
    void shouldThrowExceptionForNullInput() throws Exception {
        // use class-level parser instance

        assertThrows(Exception.class, () -> parser.parse(null));
    }

    @Test
    void shouldHandleRowWithFewerColumnsThanHeaders() throws Exception {
        // use class-level parser instance
        String json = """
                {
                  "columnHeaders": [
                    {"columnName": "postingdate"},
                    {"columnName": "product"},
                    {"columnName": "glacct"},
                    {"columnName": "description"},
                    {"columnName": "assetowner"},
                    {"columnName": "beginningbalance"},
                    {"columnName": "endingbalance"}
                  ],
                  "data": [
                    {"row": ["2024-12-31", "TestProduct", "400001"]}
                  ]
                }
                """;

        List<AccountGLJournalEntryAnnualSummaryRecord> records = parser.parse(json);

        assertEquals(1, records.size());
        assertEquals("2024-12-31", records.get(0).getPostingDate());
        assertEquals("TestProduct", records.get(0).getProduct());
        assertEquals("400001", records.get(0).getGlAcct());
    }

    @Test
    void shouldHandleMissingColumnHeadersAndDataPaths() throws Exception {
        // use class-level parser instance
        String json = """
                {
                  "someOtherField": "value"
                }
                """;

        List<AccountGLJournalEntryAnnualSummaryRecord> records = parser.parse(json);

        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    void shouldHandleNegativeAndZeroBalances() throws Exception {
        // use class-level parser instance
        String json = """
                {
                  "columnHeaders": [
                    {"columnName": "postingdate"},
                    {"columnName": "product"},
                    {"columnName": "glacct"},
                    {"columnName": "description"},
                    {"columnName": "assetowner"},
                    {"columnName": "beginningbalance"},
                    {"columnName": "endingbalance"}
                  ],
                  "data": [
                    {"row": ["2024-12-31", "TestProduct", "400001", "Fee Income", "OWNER1", "0", "0"]},
                    {"row": ["2024-12-31", "TestProduct", "400002", "Interest", "OWNER1", "-100.50", "-200.75"]}
                  ]
                }
                """;

        List<AccountGLJournalEntryAnnualSummaryRecord> records = parser.parse(json);

        assertEquals(2, records.size());
        assertEquals(BigDecimal.ZERO, records.get(0).getEndingBalance());
        assertEquals(new BigDecimal("-200.75"), records.get(1).getEndingBalance());
    }
}
