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
package org.apache.fineract.portfolio.client.domain;

/**
 * Enum representation of client identifier status states.
 */
public enum ClientIdentifierStatus {

    INACTIVE(100, "clientIdentifierStatusType.inactive"), //
    ACTIVE(200, "clientIdentifierStatusType.active"), //
    INVALID(0,"clientIdentifierStatusType.invalid");


    private final Integer value;
    private final String code;

    public static ClientIdentifierStatus fromInt(final Integer statusValue) {

        ClientIdentifierStatus enumeration = ClientIdentifierStatus.INVALID;
        switch (statusValue) {
            case 100:
                enumeration = ClientIdentifierStatus.INACTIVE;
            break;
            case 200:
                enumeration = ClientIdentifierStatus.ACTIVE;
            break;
        }
        return enumeration;
    }

    private ClientIdentifierStatus(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

//    public boolean hasStateOf(final ClientIdentifierStatus state) {
//        return this.value.equals(state.getValue());
//    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isInactive() {
        return this.value.equals(ClientIdentifierStatus.INACTIVE.getValue());
    }

    public boolean isActive() {
        return this.value.equals(ClientIdentifierStatus.ACTIVE.getValue());
    }
}