/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.domain;

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