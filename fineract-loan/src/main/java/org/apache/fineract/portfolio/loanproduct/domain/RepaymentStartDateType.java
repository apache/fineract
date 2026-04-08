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

public enum RepaymentStartDateType {

    INVALID(0, "repaymentStartDateType.invalid"), //
    DISBURSEMENT_DATE(1, "repaymentStartDateType.disbursementDate"), //
    SUBMITTED_ON_DATE(2, "repaymentStartDateType.submittedOnDate");

    private final Integer value;
    private final String code;

    RepaymentStartDateType(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public static RepaymentStartDateType fromInt(final Integer repaymentStartDateType) {
        if (repaymentStartDateType == null) {
            return RepaymentStartDateType.DISBURSEMENT_DATE;
        }
        return switch (repaymentStartDateType) {
            case 1 -> RepaymentStartDateType.DISBURSEMENT_DATE;
            case 2 -> RepaymentStartDateType.SUBMITTED_ON_DATE;
            default -> RepaymentStartDateType.INVALID;
        };
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isDisbursementDate() {
        return RepaymentStartDateType.DISBURSEMENT_DATE.equals(this);
    }

    public boolean isSubmittedOnDate() {
        return RepaymentStartDateType.SUBMITTED_ON_DATE.equals(this);
    }

}
