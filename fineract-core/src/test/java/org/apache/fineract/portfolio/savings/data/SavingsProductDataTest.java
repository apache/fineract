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
package org.apache.fineract.portfolio.savings.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class SavingsProductDataTest {

    @Test
    void lookupInstanceWithoutAccountingRuleReportsNoAccounting() {
        SavingsProductData product = SavingsProductData.lookup(1L, "Savings product");

        assertFalse(product.hasAccountingEnabled());
        assertEquals(AccountingRuleType.NONE.getValue().intValue(), product.accountingRuleTypeId());
        assertFalse(product.isCashBasedAccountingEnabled());
        assertFalse(product.isAccrualBasedAccountingEnabled());
        assertFalse(product.isUpfrontAccrualAccounting());
        assertFalse(product.isPeriodicAccrualAccounting());
    }

    @Test
    void lookupInstanceWithoutAccountingRuleCanBeSerialized() {
        SavingsProductData product = SavingsProductData.lookup(1L, "Savings product");

        String json = assertDoesNotThrow(() -> JsonMapper.builder().build().writeValueAsString(product));

        assertTrue(json.contains("\"cashBasedAccountingEnabled\":false"), json);
    }

    @Test
    void accountingRuleIsStillEvaluatedWhenPresent() {
        AccountingRuleType cashBased = AccountingRuleType.CASH_BASED;
        EnumOptionData accountingRule = new EnumOptionData(cashBased.getValue().longValue(), cashBased.getCode(),
                cashBased.getValue().toString());
        SavingsProductData product = SavingsProductData.createForInterestPosting(1L, accountingRule);

        assertTrue(product.hasAccountingEnabled());
        assertEquals(AccountingRuleType.CASH_BASED.getValue().intValue(), product.accountingRuleTypeId());
        assertTrue(product.isCashBasedAccountingEnabled());
        assertFalse(product.isAccrualBasedAccountingEnabled());
    }
}
