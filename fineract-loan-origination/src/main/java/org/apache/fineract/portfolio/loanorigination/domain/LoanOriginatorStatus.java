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
package org.apache.fineract.portfolio.loanorigination.domain;

import java.util.HashSet;
import java.util.Set;

public enum LoanOriginatorStatus {

    ACTIVE("ACTIVE"), PENDING("PENDING"), INACTIVE("INACTIVE");

    private final String value;

    LoanOriginatorStatus(String value) {
        this.value = value;
    }

    private static final Set<String> values = new HashSet<>();

    static {
        for (final LoanOriginatorStatus type : LoanOriginatorStatus.values()) {
            values.add(type.value);
        }
    }

    public String getValue() {
        return value;
    }

    public static Set<String> getAllValues() {
        return values;
    }

    public static LoanOriginatorStatus fromString(String text) {
        for (LoanOriginatorStatus status : LoanOriginatorStatus.values()) {
            if (status.value.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown LoanOriginatorStatus: " + text);
    }
}
