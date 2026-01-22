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
package org.apache.fineract.accounting.reconciliation.domain;

public enum ReconciliationStatus {

    DRAFT(0, "reconciliationStatus.draft"), //
    IN_PROGRESS(100, "reconciliationStatus.inProgress"), //
    COMPLETED(200, "reconciliationStatus.completed"), //
    APPROVED(300, "reconciliationStatus.approved"), //
    CANCELLED(400, "reconciliationStatus.cancelled");

    private final Integer value;
    private final String code;

    ReconciliationStatus(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static ReconciliationStatus fromInt(final Integer value) {
        if (value == null) {
            return null;
        }
        for (ReconciliationStatus status : ReconciliationStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

    public static ReconciliationStatus fromString(final String statusString) {
        if (statusString == null) {
            return null;
        }
        for (ReconciliationStatus status : ReconciliationStatus.values()) {
            if (status.name().equalsIgnoreCase(statusString)) {
                return status;
            }
        }
        return null;
    }

    public boolean isDraft() {
        return this.equals(DRAFT);
    }

    public boolean isCompleted() {
        return this.equals(COMPLETED);
    }

    public boolean isApproved() {
        return this.equals(APPROVED);
    }

    public boolean isCancelled() {
        return this.equals(CANCELLED);
    }
}
