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

public enum MatchType {

    AUTO_EXACT(1, "matchType.autoExact"), //
    AUTO_FUZZY(2, "matchType.autoFuzzy"), //
    MANUAL(3, "matchType.manual"), //
    SUGGESTED(4, "matchType.suggested");

    private final Integer value;
    private final String code;

    MatchType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static MatchType fromInt(final Integer value) {
        if (value == null) {
            return null;
        }
        for (MatchType type : MatchType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    public static MatchType fromString(final String typeString) {
        if (typeString == null) {
            return null;
        }
        for (MatchType type : MatchType.values()) {
            if (type.name().equalsIgnoreCase(typeString)) {
                return type;
            }
        }
        return null;
    }

    public boolean isAuto() {
        return this.equals(AUTO_EXACT) || this.equals(AUTO_FUZZY);
    }

    public boolean isManual() {
        return this.equals(MANUAL);
    }
}
