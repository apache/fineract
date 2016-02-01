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
public enum StandingInstructionStatus {

    INVALID(0, "standingInstructionStatus.invalid"), //
    ACTIVE(1, "standingInstructionStatus.active"), //
    DISABLED(2, "standingInstructionStatus.disabled"), //
    DELETED(3, "standingInstructionStatus.deleted");

    private final Integer value;
    private final String code;

    public static StandingInstructionStatus fromInt(final Integer statusValue) {

        StandingInstructionStatus enumeration = StandingInstructionStatus.INVALID;
        switch (statusValue) {
            case 1:
                enumeration = StandingInstructionStatus.ACTIVE;
            break;
            case 2:
                enumeration = StandingInstructionStatus.DISABLED;
            break;
            case 3:
                enumeration = StandingInstructionStatus.DELETED;
            break;
        }
        return enumeration;
    }

    private StandingInstructionStatus(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final StandingInstructionStatus state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isDisabled() {
        return this.value.equals(StandingInstructionStatus.DISABLED.getValue());
    }

    public boolean isDeleted() {
        return this.value.equals(StandingInstructionStatus.DELETED.getValue());
    }

    public boolean isActive() {
        return this.value.equals(StandingInstructionStatus.ACTIVE.getValue());
    }

}