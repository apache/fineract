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
package org.apache.fineract.accounting.provisioning.domain;

public enum ProvisioningEntryStatus {

    INVALID(0, "provisioningEntryStatus.invalid"), //
    DRAFT(100, "provisioningEntryStatus.draft"), //
    APPROVED(200, "provisioningEntryStatus.approved"), //
    REJECTED(300, "provisioningEntryStatus.rejected"); //

    private final Integer value;
    private final String code;

    ProvisioningEntryStatus(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public static ProvisioningEntryStatus fromInt(final Integer value) {
        if (value == null) {
            return INVALID;
        }
        for (final ProvisioningEntryStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return INVALID;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isApproved() {
        return this == APPROVED;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }
}
