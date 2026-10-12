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
package org.apache.fineract.portfolio;

import lombok.Getter;

@Getter
public enum PortfolioProductType {

    LOAN(1, "productType.loan"), //
    SAVING(2, "productType.saving"), //
    CLIENT(5, "productType.client"), //
    PROVISIONING(3, "productType.provisioning"), //
    SHARES(4, "productType.shares"), //
    WORKING_CAPITAL_LOAN(6, "productType.workingCapitalLoan"); //

    private final Integer value;
    private final String code;

    PortfolioProductType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    @Override
    public String toString() {
        return name().replace("_", " ");
    }

    public static PortfolioProductType fromInt(final Integer v) {
        if (v == null) {
            return null;
        }

        return switch (v) {
            case 1 -> LOAN;
            case 2 -> SAVING;
            case 3 -> CLIENT;
            case 4 -> PROVISIONING;
            case 5 -> SHARES;
            case 6 -> WORKING_CAPITAL_LOAN;
            default -> null;
        };
    }

    public boolean isSavingProduct() {
        return this.equals(SAVING);
    }

    public boolean isLoanProduct() {
        return this.equals(LOAN);
    }

    public boolean isClient() {
        return this.equals(CLIENT);
    }

    public boolean isShareProduct() {
        return this.equals(SHARES);
    }

    public boolean isWorkingCapitalLoanProduct() {
        return this.equals(WORKING_CAPITAL_LOAN);
    }

}
