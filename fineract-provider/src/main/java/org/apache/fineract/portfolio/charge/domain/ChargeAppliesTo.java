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

public enum ChargeAppliesTo {

    INVALID(0, "chargeAppliesTo.invalid"), //
    LOAN(1, "chargeAppliesTo.loan"), //
    SAVINGS(2, "chargeAppliesTo.savings"), //
    CLIENT(3, "chargeAppliesTo.client"),
    SHARES(4, "chargeAppliesTo.shares");
    
    private final Integer value;
    private final String code;

    private ChargeAppliesTo(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static ChargeAppliesTo fromInt(final Integer chargeAppliesTo) {
        ChargeAppliesTo chargeAppliesToType = ChargeAppliesTo.INVALID;

        if (chargeAppliesTo != null) {
            switch (chargeAppliesTo) {
                case 1:
                    chargeAppliesToType = LOAN;
                break;
                case 2:
                    chargeAppliesToType = SAVINGS;
                break;
                case 3:
                    chargeAppliesToType = CLIENT;
                break;
                case 4:
                	chargeAppliesToType = SHARES ;
                	break ;
                default:
                    chargeAppliesToType = INVALID;
                break;
            }
        }

        return chargeAppliesToType;
    }

    public boolean isLoanCharge() {
        return this.value.equals(ChargeAppliesTo.LOAN.getValue());
    }

    public boolean isSavingsCharge() {
        return this.value.equals(ChargeAppliesTo.SAVINGS.getValue());
    }

    public boolean isClientCharge() {
        return this.value.equals(ChargeAppliesTo.CLIENT.getValue());
    }

    public boolean isSharesCharge() {
    	return this.value.equals(SHARES.getValue()) ;
    }
    public static Object[] validValues() {
        return new Object[] { ChargeAppliesTo.LOAN.getValue(), ChargeAppliesTo.SAVINGS.getValue(), ChargeAppliesTo.CLIENT.getValue(), ChargeAppliesTo.SHARES.getValue() };
    }
}