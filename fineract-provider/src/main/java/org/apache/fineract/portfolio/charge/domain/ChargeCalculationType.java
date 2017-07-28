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

public enum ChargeCalculationType {

    INVALID(0, "chargeCalculationType.invalid"), //
    FLAT(1, "chargeCalculationType.flat"), //
    PERCENT_OF_AMOUNT(2, "chargeCalculationType.percent.of.amount"), //
    PERCENT_OF_AMOUNT_AND_INTEREST(3, "chargeCalculationType.percent.of.amount.and.interest"), //
    PERCENT_OF_INTEREST(4, "chargeCalculationType.percent.of.interest"),
    PERCENT_OF_DISBURSEMENT_AMOUNT(5,"chargeCalculationType.percent.of.disbursement.amount");

    private final Integer value;
    private final String code;

    private ChargeCalculationType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static Object[] validValuesForLoan() {
        return new Integer[] { ChargeCalculationType.FLAT.getValue(), ChargeCalculationType.PERCENT_OF_AMOUNT.getValue(),
                ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue(), ChargeCalculationType.PERCENT_OF_INTEREST.getValue(),
                ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getValue()};
    }

    public static Object[] validValuesForSavings() {
        return new Integer[] { ChargeCalculationType.FLAT.getValue(), ChargeCalculationType.PERCENT_OF_AMOUNT.getValue() };
    }

    public static Object[] validValuesForShares() {
        return new Integer[] { ChargeCalculationType.FLAT.getValue(), ChargeCalculationType.PERCENT_OF_AMOUNT.getValue() };
    }
    
    public static Object[] validValuesForClients() {
        return new Integer[] { ChargeCalculationType.FLAT.getValue() };
    }
    
    public static Object[] validValuesForShareAccountActivation() {
        return new Integer[] { ChargeCalculationType.FLAT.getValue() };
    }
    
    public static Object[] validValuesForTrancheDisbursement(){
    	return new Integer[] { ChargeCalculationType.FLAT.getValue(), ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getValue()};
    }

    public static ChargeCalculationType fromInt(final Integer chargeCalculation) {
        ChargeCalculationType chargeCalculationType = ChargeCalculationType.INVALID;
        switch (chargeCalculation) {
            case 1:
                chargeCalculationType = FLAT;
            break;
            case 2:
                chargeCalculationType = PERCENT_OF_AMOUNT;
            break;
            case 3:
                chargeCalculationType = PERCENT_OF_AMOUNT_AND_INTEREST;
            break;
            case 4:
                chargeCalculationType = PERCENT_OF_INTEREST;
            break;
            case 5:
            	chargeCalculationType = PERCENT_OF_DISBURSEMENT_AMOUNT;
            break;
        }
        return chargeCalculationType;
    }

    public boolean isPercentageOfAmount() {
        return this.value.equals(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue());
    }

    public boolean isPercentageOfAmountAndInterest() {
        return this.value.equals(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue());
    }

    public boolean isPercentageOfInterest() {
        return this.value.equals(ChargeCalculationType.PERCENT_OF_INTEREST.getValue());
    }

    public boolean isFlat() {
        return this.value.equals(ChargeCalculationType.FLAT.getValue());
    }

    public boolean isAllowedSavingsChargeCalculationType() {
        return isFlat() || isPercentageOfAmount();
    }

    public boolean isAllowedClientChargeCalculationType() {
        return isFlat();
    }

    public boolean isPercentageBased() {
        return isPercentageOfAmount() || isPercentageOfAmountAndInterest() || isPercentageOfInterest() || isPercentageOfDisbursementAmount();
    }
    
    public boolean isPercentageOfDisbursementAmount(){
    	return this.value.equals(ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getValue());
    }
}