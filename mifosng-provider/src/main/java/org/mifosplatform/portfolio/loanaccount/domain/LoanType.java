/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

/**
 * Enum representation of loan types .
 */
public enum LoanType {

    INVALID(0, "loanType.invalid"), //
    INDIVIDUAL(1, "loanType.individual"), //
    GROUP(2, "loanType.group"), //
    JLG(3, "loanType.jlg");//JLG loans given in group context

    private final Integer value;
    private final String code;

    private LoanType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public static LoanType fromInt(final Integer loanTypeValue) {

        LoanType enumeration = LoanType.INVALID;
        switch (loanTypeValue) {
            case 1:
                enumeration = LoanType.INDIVIDUAL;
            break;
            case 2:
                enumeration = LoanType.GROUP;
            break;
            case 3:
                enumeration = LoanType.JLG;
            break;
        }
        return enumeration;
    }

    public static LoanType fromName(final String name){
        LoanType loanType = LoanType.INVALID;
        for (LoanType type : LoanType.values()) {
            if(type.getName().equals(name)) {
                loanType = type;
                break;
            }
        }
        return loanType;
    }
    
    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return code;
    }
    
    public String getName(){
        return name().toLowerCase();
    }
    
    public boolean isInvalid(){
        return this.value.equals(LoanType.INVALID.getValue());
    }
    
    public boolean isIndividualLoan(){
        return this.value.equals(LoanType.INDIVIDUAL.getValue());
    }
    
    public boolean isGroupLoan(){
        return this.value.equals(LoanType.GROUP.getValue());
    }
    
    public boolean isJLGLoan(){
        return this.value.equals(LoanType.JLG.getValue());
    }
}