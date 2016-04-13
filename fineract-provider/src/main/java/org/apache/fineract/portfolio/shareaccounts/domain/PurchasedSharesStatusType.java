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
package org.apache.fineract.portfolio.shareaccounts.domain;

public enum PurchasedSharesStatusType {
    INVALID(0, "purchasedSharesStatusType.invalid"), APPLIED(100, "purchasedSharesStatusType.applied"), APPROVED(300,
            "purchasedSharesStatusType.approved"), REJECTED(400, "purchasedSharesStatusType.rejected"), PURCHASED(500,
            "purchasedSharesType.purchased"), REDEEMED(600, "purchasedSharesType.redeemed"),
            CHARGE_PAYMENT(700, "charge.payment");

    private final Integer value;
    private final String code;

    private PurchasedSharesStatusType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public static PurchasedSharesStatusType fromInt(final Integer type) {

        PurchasedSharesStatusType enumeration = PurchasedSharesStatusType.INVALID;
        switch (type) {
            case 100:
                enumeration = PurchasedSharesStatusType.APPLIED;
            break;
            case 300:
                enumeration = PurchasedSharesStatusType.APPROVED;
            break;
            case 400:
                enumeration = PurchasedSharesStatusType.REJECTED;
            break;
            case 500:
                enumeration = PurchasedSharesStatusType.PURCHASED;
            break;
            case 600:
                enumeration = PurchasedSharesStatusType.REDEEMED;
            break;
            case 700:
                enumeration = PurchasedSharesStatusType.CHARGE_PAYMENT;
                break ;
        }
        return enumeration;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isApproved() {
        return this.value.equals(PurchasedSharesStatusType.APPROVED.getValue());
    }

    public boolean isPurchased() {
        return this.value.equals(PurchasedSharesStatusType.PURCHASED.getValue());
    }
    
    public boolean isChargePayment() {
        return this.value.equals(PurchasedSharesStatusType.CHARGE_PAYMENT.getValue());
    }
}
