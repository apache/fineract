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
 * Enum representation of client status states.
 */
public enum ClientStatus {

    INVALID(0, "clientStatusType.invalid"), //
    PENDING(100, "clientStatusType.pending"), //
    ACTIVE(300, "clientStatusType.active"), //
    TRANSFER_IN_PROGRESS(303, "clientStatusType.transfer.in.progress"), //
    TRANSFER_ON_HOLD(304, "clientStatusType.transfer.on.hold"), //
    CLOSED(600, "clientStatusType.closed"),
    REJECTED(700,"clientStatusType.rejected"),
    WITHDRAWN(800,"clientStatusType.withdraw");
    

    private final Integer value;
    private final String code;

    public static ClientStatus fromInt(final Integer statusValue) {

        ClientStatus enumeration = ClientStatus.INVALID;
        switch (statusValue) {
            case 100:
                enumeration = ClientStatus.PENDING;
            break;
            case 300:
                enumeration = ClientStatus.ACTIVE;
            break;
            case 303:
                enumeration = ClientStatus.TRANSFER_IN_PROGRESS;
            break;
            case 304:
                enumeration = ClientStatus.TRANSFER_ON_HOLD;
            break;
            case 600:
                enumeration = ClientStatus.CLOSED;
            break;
            case 700:
            	enumeration = ClientStatus.REJECTED;
            break;
            case 800:
            	enumeration = ClientStatus.WITHDRAWN;
            break;
           
        }
        return enumeration;
    }

    private ClientStatus(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final ClientStatus state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isPending() {
        return this.value.equals(ClientStatus.PENDING.getValue());
    }

    public boolean isActive() {
        return this.value.equals(ClientStatus.ACTIVE.getValue());
    }
  
    public boolean isClosed() {
        return this.value.equals(ClientStatus.CLOSED.getValue());
    }
    public boolean isRejected(){
    	return this.value.equals(ClientStatus.REJECTED.getValue());
    }
    public boolean isWithdrawn(){
    	return this.value.equals(ClientStatus.WITHDRAWN.getValue());
    }
    public boolean isTransferInProgress() {
        return this.value.equals(ClientStatus.TRANSFER_IN_PROGRESS.getValue());
    }

    public boolean isTransferOnHold() {
        return this.value.equals(ClientStatus.TRANSFER_ON_HOLD.getValue());
    }

    public boolean isUnderTransfer() {
        return isTransferInProgress() || isTransferOnHold();
    }
}