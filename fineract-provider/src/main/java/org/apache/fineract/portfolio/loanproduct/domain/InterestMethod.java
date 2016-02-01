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
package org.apache.fineract.portfolio.loanproduct.domain;


public enum InterestMethod {
    DECLINING_BALANCE(0, "interestType.declining.balance"), FLAT(1, "interestType.flat"), INVALID(2, "interestType.invalid");

    private final Integer value;
    private final String code;

    private InterestMethod(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static InterestMethod fromInt(final Integer selectedMethod) {

        InterestMethod repaymentMethod = null;
        switch (selectedMethod) {
            case 0:
                repaymentMethod = InterestMethod.DECLINING_BALANCE;
            break;
            case 1:
                repaymentMethod = InterestMethod.FLAT;
            break;
            default:
                repaymentMethod = InterestMethod.INVALID;
            break;
        }
        return repaymentMethod;
    }

    public boolean isDecliningBalnce() {
        return this.value.equals(InterestMethod.DECLINING_BALANCE.getValue());
    }
}