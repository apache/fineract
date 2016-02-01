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
public enum StandingInstructionPriority {

    INVALID(0, "standingInstructionPriority.invalid"), //
    URGENT(1, "standingInstructionPriority.urgent"), //
    HIGH(2, "standingInstructionPriority.high"), //
    MEDIUM(3, "standingInstructionPriority.medium"), //
    LOW(4, "standingInstructionPriority.low");

    private final Integer value;
    private final String code;

    public static StandingInstructionPriority fromInt(final Integer statusValue) {

        StandingInstructionPriority enumeration = StandingInstructionPriority.INVALID;
        switch (statusValue) {
            case 1:
                enumeration = StandingInstructionPriority.URGENT;
            break;
            case 2:
                enumeration = StandingInstructionPriority.HIGH;
            break;
            case 3:
                enumeration = StandingInstructionPriority.MEDIUM;
            break;
            case 4:
                enumeration = StandingInstructionPriority.LOW;
            break;
        }
        return enumeration;
    }

    private StandingInstructionPriority(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final StandingInstructionPriority state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isHighPriority() {
        return this.value.equals(StandingInstructionPriority.HIGH.getValue());
    }

    public boolean isLowPriority() {
        return this.value.equals(StandingInstructionPriority.LOW.getValue());
    }

    public boolean isUrgentPriority() {
        return this.value.equals(StandingInstructionPriority.URGENT.getValue());
    }

    public boolean isMediumPriority() {
        return this.value.equals(StandingInstructionPriority.MEDIUM.getValue());
    }
}