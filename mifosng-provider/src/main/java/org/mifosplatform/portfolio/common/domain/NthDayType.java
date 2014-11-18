/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.common.domain;

public enum NthDayType {
	
	ONE(1,"nthDayType.one"),
	TWO(2,"nthDayType.two"),
	THREE(3,"nthDayType.three"),
	FOUR(4,"nthDayType.four"),
	FIVE(5,"nthDayType.five"),
	INVALID(0,"nthDayType.invalid");
	
	private final Integer value;
    private final String code;
	
    private NthDayType(Integer value, String code) {
		this.value = value;
		this.code = code;
	}

	public Integer getValue() {
		return this.value;
	}

	public String getCode() {
		return this.code;
	}
	
	public static NthDayType fromInt(final Integer frequency) {
		NthDayType repaymentFrequencyNthDayType = NthDayType.INVALID;
        if (frequency != null) {
            switch (frequency) {
                case 1:
                    repaymentFrequencyNthDayType = NthDayType.ONE;
                break;
                case 2:
                    repaymentFrequencyNthDayType = NthDayType.TWO;
                break;
                case 3:
                    repaymentFrequencyNthDayType = NthDayType.THREE;
                break;
                case 4:
                    repaymentFrequencyNthDayType = NthDayType.FOUR;
                break;
                case 5:
                    repaymentFrequencyNthDayType = NthDayType.FIVE;
                break;
                default:
                break;
            }
        }
        return repaymentFrequencyNthDayType;
    }
   
}
