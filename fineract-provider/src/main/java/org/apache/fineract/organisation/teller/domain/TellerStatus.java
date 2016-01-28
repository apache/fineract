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
package org.apache.fineract.organisation.teller.domain;

/**
 * Enum representation of teller status states.
 */

public enum TellerStatus {
	
    INVALID(0, "tellerStatusType.invalid"),
    PENDING(100, "tellerStatusType.pending"),
    ACTIVE(300, "tellerStatusType.active"),
    INACTIVE(400, "tellerStatusType.inactive"),
    CLOSED(600, "tellerStatusType.closed");

    private final Integer value;
    private final String code;

    public static TellerStatus fromInt(final Integer statusValue) {

        TellerStatus status = TellerStatus.INVALID;
        switch (statusValue) {
            case 100:
            	status = TellerStatus.PENDING;
            break;
            case 300:
            	status = TellerStatus.ACTIVE;
            break;
            case 400:
            	status = TellerStatus.INACTIVE;
            break;
            case 600:
            	status = TellerStatus.CLOSED;
            break;
        }
        return status;
    }

    private TellerStatus(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final TellerStatus state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isPending() {
        return this.value.equals(TellerStatus.PENDING.getValue());
    }

    public boolean isActive() {
        return this.value.equals(TellerStatus.ACTIVE.getValue());
    }

    public boolean isClosed() {
        return this.value.equals(TellerStatus.CLOSED.getValue());
    }

    public boolean isInactive() {
        return this.value.equals(TellerStatus.INACTIVE.getValue());
    }
    
}
