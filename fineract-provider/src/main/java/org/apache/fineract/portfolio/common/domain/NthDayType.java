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
package org.apache.fineract.portfolio.common.domain;

public enum NthDayType {
	
	ONE(1,"nthDayType.one"),
	TWO(2,"nthDayType.two"),
	THREE(3,"nthDayType.three"),
	FOUR(4,"nthDayType.four"),
	FIVE(5,"nthDayType.five"),
	LAST(-1,"nthDayType.last"),
	ONDAY(-2,"nthDayType.onday"),
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
                case -1:
                    repaymentFrequencyNthDayType = NthDayType.LAST;
                break;
                case -2:
                    repaymentFrequencyNthDayType = NthDayType.ONDAY;
                break;
                default:
                break;
            }
        }
        return repaymentFrequencyNthDayType;
    }
   
    public boolean isInvalid() {
        return this.value.equals(NthDayType.INVALID.value);
    }
    public boolean isLastDay() {
        return this.value.equals(NthDayType.LAST.value);
    }
    public boolean isOnDay() {
        return this.value.equals(NthDayType.ONDAY.value);
    }
}
