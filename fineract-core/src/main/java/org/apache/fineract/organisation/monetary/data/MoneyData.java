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
package org.apache.fineract.organisation.monetary.data;

import java.math.BigDecimal;

/**
 * Immutable data object representing currency.
 */
public class MoneyData {

    private final String code;
    private final BigDecimal amount;
    private final int decimalPlaces;

    public MoneyData(final String code, final BigDecimal amount, final int decimalPlaces) {
        this.code = code;
        this.amount = amount;
        this.decimalPlaces = decimalPlaces;
    }

    public String getCode() {
        return this.code;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public int getDecimalPlaces() {
        return this.decimalPlaces;
    }

}
