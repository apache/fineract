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
package org.apache.fineract.portfolio.charge.domain;

public enum ChargePaymentMode {

    REGULAR(0, "chargepaymentmode.regular"), //
    ACCOUNT_TRANSFER(1, "chargepaymentmode.accounttransfer");

    private final Integer value;
    private final String code;

    private ChargePaymentMode(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static Object[] validValues() {
        return new Integer[] { ChargePaymentMode.REGULAR.getValue(), ChargePaymentMode.ACCOUNT_TRANSFER.getValue() };
    }

    public static ChargePaymentMode fromInt(final Integer paymentMode) {
        ChargePaymentMode chargeAppliesToType = ChargePaymentMode.REGULAR;
        switch (paymentMode) {
            case 1:
                chargeAppliesToType = ACCOUNT_TRANSFER;
            break;
            default:
                chargeAppliesToType = REGULAR;
            break;
        }
        return chargeAppliesToType;
    }

    public boolean isPaymentModeAccountTransfer() {
        return this.value.equals(ChargePaymentMode.ACCOUNT_TRANSFER.getValue());
    }
}