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

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public enum LoanSubStatus {
    INVALID(0, "loanSubStatusType.invalid"), //
    FORECLOSED(100, "loanSubStatusType.foreclosed");

    private final Integer value;
    private final String code;

    public static LoanSubStatus fromInt(final Integer statusValue) {

        LoanSubStatus enumeration = LoanSubStatus.INVALID;
        switch (statusValue) {
            case 100:
                enumeration = LoanSubStatus.FORECLOSED;
            break;
        }
        return enumeration;
    }

    private LoanSubStatus(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final LoanSubStatus state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isForeclosed() {
        return this.value.equals(LoanSubStatus.FORECLOSED.getValue());
    }

    public static EnumOptionData loanSubStatus(final int id) {
        return loanSubStatusEnum(LoanSubStatus.fromInt(id));
    }

    public static EnumOptionData loanSubStatusEnum(final LoanSubStatus type) {
        final String codePrefix = "loanSubStatus.";
        EnumOptionData optionData = null;
        switch (type) {
            case FORECLOSED:
                optionData = new EnumOptionData(LoanSubStatus.FORECLOSED.getValue().longValue(), codePrefix
                        + LoanSubStatus.FORECLOSED.getCode(), "Foreclosed");
            break;
            default:
                optionData = new EnumOptionData(LoanSubStatus.INVALID.getValue().longValue(), LoanSubStatus.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }
}
