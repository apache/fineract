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

public enum LoanProductParamType {

    INVALID(0, "LoanProductParamType.invalid"), //
    PRINCIPAL(1, "LoanProductParamType.principal"), //
    INTERESTRATE(2, "LoanProductParamType.interestrate"), //
    REPAYMENT(3, "LoanProductParamType.repayment"); //

    private final Integer value;
    private final String code;

    private LoanProductParamType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static LoanProductParamType fromInt(final Integer chargeTime) {
        LoanProductParamType loanProductParamType = LoanProductParamType.INVALID;
        if (chargeTime != null) {
            switch (chargeTime) {
                case 1:
                    loanProductParamType = PRINCIPAL;
                break;
                case 2:
                    loanProductParamType = INTERESTRATE;
                break;
                case 3:
                    loanProductParamType = REPAYMENT;
                break;
                default:
                    loanProductParamType = INVALID;
                break;
            }
        }
        return loanProductParamType;
    }

    public boolean isParamTypePrincipal() {
        return LoanProductParamType.PRINCIPAL.getValue().equals(this.value);
    }

    public boolean isParamTypeInterestTate() {
        return LoanProductParamType.INTERESTRATE.getValue().equals(this.value);
    }

    public boolean isParamTypeRepayment() {
        return LoanProductParamType.REPAYMENT.getValue().equals(this.value);
    }

}