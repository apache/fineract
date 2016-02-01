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
package org.apache.fineract.portfolio.account.domain;

/**
 * Enum representation of loan status states.
 */
public enum StandingInstructionType {

    INVALID(0, "standingInstructionType.invalid"), //
    FIXED(1, "standingInstructionType.fixed"), //
    DUES(2, "standingInstructionType.dues"); //

    private final Integer value;
    private final String code;

    public static StandingInstructionType fromInt(final Integer statusValue) {

        StandingInstructionType enumeration = StandingInstructionType.INVALID;
        switch (statusValue) {
            case 1:
                enumeration = StandingInstructionType.FIXED;
            break;
            case 2:
                enumeration = StandingInstructionType.DUES;
            break;
        }
        return enumeration;
    }

    private StandingInstructionType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final StandingInstructionType state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isFixedAmoutTransfer() {
        return this.value.equals(StandingInstructionType.FIXED.getValue());
    }

    public boolean isDuesAmoutTransfer() {
        return this.value.equals(StandingInstructionType.DUES.getValue());
    }

}