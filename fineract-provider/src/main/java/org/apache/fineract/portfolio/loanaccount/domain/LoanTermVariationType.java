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
package org.apache.fineract.portfolio.loanaccount.domain;

public enum LoanTermVariationType {

    INVALID(0, "loanTermType.invalid"), //
    EMI_AMOUNT(1, "loanTermType.emiAmount"), //
    INTEREST_RATE(2, "loanTermType.interestRate"), //
    PRINCIPAL_AMOUNT(3, "loanTermType.principalAmount"), //
    DUE_DATE(4, "loanTermType.dueDate"), //
    INSERT_INSTALLMENT(5, "loanTermType.insertInstallment"), //
    DELETE_INSTALLMENT(6, "loanTermType.deleteInstallment"),
    GRACE_ON_INTEREST(7, "loanTermType.graceOnInterest"),
    GRACE_ON_PRINCIPAL(8, "loanTermType.graceOnPrincipal"),
    EXTEND_REPAYMENT_PERIOD(9, "loanTermType.extendRepaymentPeriod"),
    INTEREST_RATE_FROM_INSTALLMENT(10, "loanTermType.interestRateFromInstallment");

    private final Integer value;
    private final String code;

    private LoanTermVariationType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static LoanTermVariationType fromInt(final Integer value) {

        LoanTermVariationType enumeration = LoanTermVariationType.INVALID;
        switch (value) {
            case 1:
                enumeration = LoanTermVariationType.EMI_AMOUNT;
            break;
            case 2:
                enumeration = LoanTermVariationType.INTEREST_RATE;
            break;
            case 3:
                enumeration = LoanTermVariationType.PRINCIPAL_AMOUNT;
            break;
            case 4:
                enumeration = LoanTermVariationType.DUE_DATE;
            break;
            case 5:
                enumeration = LoanTermVariationType.INSERT_INSTALLMENT;
            break;
            case 6:
                enumeration = LoanTermVariationType.DELETE_INSTALLMENT;
            break;
            case 7:
                enumeration = LoanTermVariationType.GRACE_ON_INTEREST;
            break;
            case 8:
                enumeration = LoanTermVariationType.GRACE_ON_PRINCIPAL;
            break;
            case 9:
                enumeration = LoanTermVariationType.EXTEND_REPAYMENT_PERIOD;
            break;
            case 10:
                enumeration = LoanTermVariationType.INTEREST_RATE_FROM_INSTALLMENT;
            break;
        }
        return enumeration;
    }

    public boolean isEMIAmountVariation() {
        return this.value.equals(LoanTermVariationType.EMI_AMOUNT.getValue());
    }

    public boolean isInterestRateVariation() {
        return this.value.equals(LoanTermVariationType.INTEREST_RATE.getValue());
    }

    public boolean isPrincipalAmountVariation() {
        return this.value.equals(LoanTermVariationType.PRINCIPAL_AMOUNT.getValue());
    }

    public boolean isDueDateVariation() {
        return this.value.equals(LoanTermVariationType.DUE_DATE.getValue());
    }

    public boolean isInsertInstallment() {
        return this.value.equals(LoanTermVariationType.INSERT_INSTALLMENT.getValue());
    }

    public boolean isDeleteInstallment() {
        return this.value.equals(LoanTermVariationType.DELETE_INSTALLMENT.getValue());
    }
    
    public boolean isGraceOnInterest() {
        return this.value.equals(LoanTermVariationType.GRACE_ON_INTEREST.getValue());
    }
    
    public boolean isGraceOnPrincipal() {
        return this.value.equals(LoanTermVariationType.GRACE_ON_PRINCIPAL.getValue());
    }
    
    public boolean isExtendRepaymentPeriod() {
        return this.value.equals(LoanTermVariationType.EXTEND_REPAYMENT_PERIOD.getValue());
    }
    
    public boolean isInterestRateFromInstallment() {
        return this.value.equals(LoanTermVariationType.INTEREST_RATE_FROM_INSTALLMENT.getValue());
    }
}
