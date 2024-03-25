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
package org.apache.fineract.test.data;

public enum TransactionType {

    DISBURSEMENT("disbursement"), REPAYMENT("repayment"), DOWN_PAYMENT("downPayment"), GOODWILL_CREDIT("goodwillCredit"), PAYOUT_REFUND(
            "payoutRefund"), REFUND_BY_CASH("refundByCash"), MERCHANT_ISSUED_REFUND("merchantIssuedRefund"), CREDIT_BALANCE_REFUND(
                    "creditBalanceRefund"), CHARGEBACK("chargeback"), ACCRUAL("accrual"), CHARGE_OFF("chargeOff");

    public final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
