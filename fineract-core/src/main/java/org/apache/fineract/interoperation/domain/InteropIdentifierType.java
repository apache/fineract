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
package org.apache.fineract.interoperation.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum InteropIdentifierType {

    MSISDN("interopIdentifierType.msisdn"), //
    EMAIL("interopIdentifierType.email"), //
    PERSONAL_ID("interopIdentifierType.personalId", "PERSONALID", "PERSONALID"), //
    BUSINESS("interopIdentifierType.business"), //
    DEVICE("interopIdentifierType.device"), //
    ACCOUNT_ID("interopIdentifierType.accountId", "ACCOUNTID", "ACCOUNTID"), //
    IBAN("interopIdentifierType.iban"), //
    ALIAS("interopIdentifierType.alias"), //
    BBAN("interopIdentifierType.bban"), //
    ; //

    public static final InteropIdentifierType[] VALUES = values();

    private static final Map<String, InteropIdentifierType> BY_ALIAS = Arrays.stream(VALUES)
            .collect(Collectors.toMap(InteropIdentifierType::getAlias, v -> v));
    private static final Map<String, InteropIdentifierType> BY_NAME = Arrays.stream(VALUES)
            .collect(Collectors.toMap(InteropIdentifierType::name, v -> v));

    private final String code;
    private final String description;
    private final String alias;

    InteropIdentifierType(String code, String description, String alias) {
        this.code = code;
        this.description = description == null ? name() : description;
        this.alias = alias == null ? name() : alias;
    }

    InteropIdentifierType(String code) {
        this(code, null, null);
    }

    public static InteropIdentifierType resolveName(String name) {
        if (name == null) {
            return null;
        }
        InteropIdentifierType idType = BY_ALIAS.get(name);
        return idType == null ? BY_NAME.get(name) : idType;
    }
}
