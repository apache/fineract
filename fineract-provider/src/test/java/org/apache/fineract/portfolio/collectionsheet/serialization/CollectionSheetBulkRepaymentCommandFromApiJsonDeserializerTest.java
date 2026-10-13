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
package org.apache.fineract.portfolio.collectionsheet.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.collectionsheet.command.SingleRepaymentCommand;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetailAssembler;
import org.junit.jupiter.api.Test;

class CollectionSheetBulkRepaymentCommandFromApiJsonDeserializerTest {

    private final CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer deserializer = new CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer(
            new FromJsonHelper(), mock(PaymentDetailAssembler.class));

    @Test
    void keepsRepaymentsBelowOne() {
        final String json = """
                {"dateFormat": "dd MMMM yyyy", "locale": "en", "transactionDate": "20 October 2011",
                 "bulkRepaymentTransactions": [{"loanId": 1, "transactionAmount": 1000}, {"loanId": 2, "transactionAmount": 0.5},
                 {"loanId": 3, "transactionAmount": 0}]}
                """;

        final SingleRepaymentCommand[] repayments = deserializer.commandFromApiJson(json, null).getLoanTransactions();

        assertEquals(3, repayments.length);
        assertEquals(new BigDecimal("1000"), repayments[0].getTransactionAmount());
        assertEquals(new BigDecimal("0.5"), repayments[1].getTransactionAmount());
        assertEquals(2, Arrays.stream(repayments).filter(Objects::nonNull).count(), "A zero amount is not a repayment");
    }
}
