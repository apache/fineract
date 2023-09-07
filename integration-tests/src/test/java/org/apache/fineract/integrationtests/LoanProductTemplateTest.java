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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.fineract.client.models.GetLoanProductsTemplateResponse;
import org.apache.fineract.integrationtests.common.loans.LoanProductHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoanProductTemplateTest {

    private static LoanProductHelper loanProductHelper;

    @BeforeAll
    public static void setup() {
        loanProductHelper = new LoanProductHelper();
    }

    @Test
    public void testLoanProductTemplateForAdvancedPaymentAllocation() {
        GetLoanProductsTemplateResponse loanProductsTemplateResponse = loanProductHelper.getLoanProductTemplate(false);

        // assert payment allocation types
        assertEquals("PAST_DUE_PENALTY", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(0).getCode());
        assertEquals("Past due penalty", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(0).getValue());
        assertEquals("PAST_DUE_FEE", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(1).getCode());
        assertEquals("Past due fee", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(1).getValue());
        assertEquals("PAST_DUE_PRINCIPAL", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(2).getCode());
        assertEquals("Past due principal", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(2).getValue());
        assertEquals("PAST_DUE_INTEREST", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(3).getCode());
        assertEquals("Past due interest", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(3).getValue());
        assertEquals("DUE_PENALTY", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(4).getCode());
        assertEquals("Due penalty", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(4).getValue());
        assertEquals("DUE_FEE", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(5).getCode());
        assertEquals("Due fee", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(5).getValue());
        assertEquals("DUE_PRINCIPAL", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(6).getCode());
        assertEquals("Due principal", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(6).getValue());
        assertEquals("DUE_INTEREST", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(7).getCode());
        assertEquals("Due interest", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(7).getValue());
        assertEquals("IN_ADVANCE_PENALTY", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(8).getCode());
        assertEquals("In advance penalty", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(8).getValue());
        assertEquals("IN_ADVANCE_FEE", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(9).getCode());
        assertEquals("In advance fee", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(9).getValue());
        assertEquals("IN_ADVANCE_PRINCIPAL", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(10).getCode());
        assertEquals("In advance principal", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(10).getValue());
        assertEquals("IN_ADVANCE_INTEREST", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(11).getCode());
        assertEquals("In advanced interest", loanProductsTemplateResponse.getAdvancedPaymentAllocationTypes().get(11).getValue());
        // assert payment allocation transaction types
        assertEquals("DEFAULT", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(0).getCode());
        assertEquals("Default", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(0).getValue());
        assertEquals("REPAYMENT", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(1).getCode());
        assertEquals("Repayment", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(1).getValue());
        assertEquals("DOWN_PAYMENT", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(2).getCode());
        assertEquals("Down payment", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(2).getValue());
        assertEquals("MERCHANT_ISSUED_REFUND",
                loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(3).getCode());
        assertEquals("Merchant issued refund",
                loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(3).getValue());
        assertEquals("PAYOUT_REFUND", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(4).getCode());
        assertEquals("Payout refund", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(4).getValue());
        assertEquals("GOODWILL_CREDIT", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(5).getCode());
        assertEquals("Goodwill credit", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(5).getValue());
        assertEquals("CHARGE_REFUND", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(6).getCode());
        assertEquals("Charge refund", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(6).getValue());
        assertEquals("CHARGE_ADJUSTMENT", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(7).getCode());
        assertEquals("Charge adjustment", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(7).getValue());
        assertEquals("WAIVE_INTEREST", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(8).getCode());
        assertEquals("Waive interest", loanProductsTemplateResponse.getAdvancedPaymentAllocationTransactionTypes().get(8).getValue());
        // assert future installment rules
        assertEquals("NEXT_INSTALLMENT",
                loanProductsTemplateResponse.getAdvancedPaymentAllocationFutureInstallmentAllocationRules().get(0).getCode());
        assertEquals("Next installment",
                loanProductsTemplateResponse.getAdvancedPaymentAllocationFutureInstallmentAllocationRules().get(0).getValue());
        assertEquals("LAST_INSTALLMENT",
                loanProductsTemplateResponse.getAdvancedPaymentAllocationFutureInstallmentAllocationRules().get(1).getCode());
        assertEquals("Last installment",
                loanProductsTemplateResponse.getAdvancedPaymentAllocationFutureInstallmentAllocationRules().get(1).getValue());
        assertEquals("REAMORTIZATION",
                loanProductsTemplateResponse.getAdvancedPaymentAllocationFutureInstallmentAllocationRules().get(2).getCode());
        assertEquals("Reamortization",
                loanProductsTemplateResponse.getAdvancedPaymentAllocationFutureInstallmentAllocationRules().get(2).getValue());
    }
}
