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
package org.apache.fineract.portfolio.note.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public enum NoteType {

    CLIENT(100, "noteType.client", "clients", "Client note"), //
    LOAN(200, "noteType.loan", "loans", "Loan note"), //
    LOAN_TRANSACTION(300, "noteType.loan.transaction", "loanTransactions", "Loan transaction note"), //
    SAVING_ACCOUNT(500, "noteType.saving", "savings", " account note"), //
    GROUP(600, "noteType.group", "groups", "Group note"), //
    SHARE_ACCOUNT(700, "noteType.shares", "accounts/share", "Share account note"), //
    SAVINGS_TRANSACTION(800, "noteType.savings.transaction", "savingsTransactions", "Savings transaction note"), //
    ;

    public static final NoteType[] VALUES = values();

    private static final Map<Integer, NoteType> BY_ID = Arrays.stream(VALUES).collect(Collectors.toMap(NoteType::getValue, v -> v));
    private static final Map<String, NoteType> BY_API = Arrays.stream(VALUES).collect(Collectors.toMap(NoteType::getApiUrl, v -> v));

    private final Integer value;
    private final String code;
    private final String apiUrl;
    private final String description;

    NoteType(final Integer value, final String code, final String apiUrl, String description) {
        this.value = value;
        this.code = code;
        this.apiUrl = apiUrl;
        this.description = description;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public String getApiUrl() {
        return this.apiUrl;
    }

    public String getDescription() {
        return description;
    }

    public static NoteType fromInt(int i) {
        return BY_ID.get(i);
    }

    public static NoteType fromApiUrl(final String url) {
        return BY_API.get(url);
    }

    public EnumOptionData toEnumOptionData() {
        return new EnumOptionData(getValue().longValue(), getCode(), getDescription());
    }
}
