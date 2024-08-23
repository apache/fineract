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
package org.apache.fineract.portfolio;

import lombok.Getter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;

@Getter
public enum TransactionEntryType {

    CREDIT(1, "transactionEntryType.credit", "Credit transaction"), //
    DEBIT(2, "transactionEntryType.debit", "Debit transaction"), //
    ;

    private final Integer value;
    private final String code;
    private final String description;

    TransactionEntryType(final Integer value, final String code, final String description) {
        this.value = value;
        this.code = code;
        this.description = description;
    }

    public boolean isCredit() {
        return this == CREDIT;
    }

    public boolean isDebit() {
        return this == DEBIT;
    }

    public TransactionEntryType getReversal() {
        return this == CREDIT ? DEBIT : CREDIT;
    }

    public EnumOptionData toEnumOptionData() {
        return new EnumOptionData((long) getValue(), getCode(), getDescription());
    }

    public StringEnumOptionData toStringEnumOptionData() {
        return new StringEnumOptionData(name(), getCode(), getDescription());
    }
}
