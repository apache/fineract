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
package org.apache.fineract.portfolio.shareaccounts.domain;

/**
 * Enum representation of {@link ShareAccount} status states.
 */
public enum ShareAccountStatusType {

    INVALID(0, "shareAccountStatusType.invalid"), //
    SUBMITTED_AND_PENDING_APPROVAL(100, "shareAccountStatusType.submitted.and.pending.approval"), //
    APPROVED(200, "shareAccountStatusType.approved"), //
    ACTIVE(300, "shareAccountStatusType.active"), //
    REJECTED(500, "shareAccountStatusType.rejected"), //
    CLOSED(600, "shareAccountStatusType.closed");

    private final Integer value;
    private final String code;

    public static ShareAccountStatusType fromInt(final Integer type) {

        ShareAccountStatusType enumeration = ShareAccountStatusType.INVALID;
        switch (type) {
            case 100:
                enumeration = ShareAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL;
            break;
            case 200:
                enumeration = ShareAccountStatusType.APPROVED;
            break;
            case 300:
                enumeration = ShareAccountStatusType.ACTIVE;
            break;
            case 500:
                enumeration = ShareAccountStatusType.REJECTED;
            break;
            case 600:
                enumeration = ShareAccountStatusType.CLOSED;
            break;
        }
        return enumeration;
    }

    private ShareAccountStatusType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final ShareAccountStatusType state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isSubmittedAndPendingApproval() {
        return this.value.equals(ShareAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue());
    }

    public boolean isApproved() {
        return this.value.equals(ShareAccountStatusType.APPROVED.getValue());
    }

    public boolean isRejected() {
        return this.value.equals(ShareAccountStatusType.REJECTED.getValue());
    }

    public boolean isActive() {
        return this.value.equals(ShareAccountStatusType.ACTIVE.getValue());
    }

    public boolean isActiveOrAwaitingApprovalOrDisbursal() {
        return isApproved() || isSubmittedAndPendingApproval() || isActive();
    }

    public boolean isClosed() {
        return this.value.equals(ShareAccountStatusType.CLOSED.getValue()) || isRejected();
    }

}