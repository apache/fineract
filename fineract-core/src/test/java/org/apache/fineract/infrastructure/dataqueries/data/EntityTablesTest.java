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
package org.apache.fineract.infrastructure.dataqueries.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.junit.jupiter.api.Test;

class EntityTablesTest {

    @Test
    void testEntityTablesEnumOptionData() {
        StringEnumOptionData clientOption = EntityTables.CLIENT.toEnumOptionData();
        assertNotNull(clientOption);
        assertEquals("CLIENT", clientOption.getId());
        assertEquals("m_client", clientOption.getCode());
        assertEquals("Client", clientOption.getValue());

        StringEnumOptionData loanOption = EntityTables.LOAN.toEnumOptionData();
        assertNotNull(loanOption);
        assertEquals("LOAN", loanOption.getId());
        assertEquals("m_loan", loanOption.getCode());
        assertEquals("Loan", loanOption.getValue());

        StringEnumOptionData savingsOption = EntityTables.SAVINGS.toEnumOptionData();
        assertNotNull(savingsOption);
        assertEquals("SAVINGS", savingsOption.getId());
        assertEquals("m_savings_account", savingsOption.getCode());
        assertEquals("Savings Account", savingsOption.getValue());

        StringEnumOptionData groupOption = EntityTables.GROUP.toEnumOptionData();
        assertNotNull(groupOption);
        assertEquals("GROUP", groupOption.getId());
        assertEquals("m_group", groupOption.getCode());
        assertEquals("Group", groupOption.getValue());

        StringEnumOptionData wcLoanOption = EntityTables.WC_LOAN.toEnumOptionData();
        assertNotNull(wcLoanOption);
        assertEquals("WC_LOAN", wcLoanOption.getId());
        assertEquals("m_wc_loan", wcLoanOption.getCode());
        assertEquals("Working Capital Loan", wcLoanOption.getValue());

        StringEnumOptionData wcProductOption = EntityTables.WC_LOAN_PRODUCT.toEnumOptionData();
        assertNotNull(wcProductOption);
        assertEquals("WC_LOAN_PRODUCT", wcProductOption.getId());
        assertEquals("m_wc_loan_product", wcProductOption.getCode());
        assertEquals("Working Capital Loan Product", wcProductOption.getValue());
    }

    @Test
    void testFromEntityName() {
        assertEquals(EntityTables.CLIENT, EntityTables.fromEntityName("m_client"));
        assertEquals(EntityTables.LOAN, EntityTables.fromEntityName("m_loan"));
        assertEquals(EntityTables.SAVINGS, EntityTables.fromEntityName("m_savings_account"));
        assertEquals(EntityTables.GROUP, EntityTables.fromEntityName("m_group"));
    }
}
