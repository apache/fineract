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
package org.apache.fineract.accounting.producttoaccountmapping.domain;

import java.util.HashMap;
import java.util.Map;

public enum PortfolioProductType {
    LOAN(1, "productType.loan"), SAVING(2, "productType.saving"), CLIENT(5, "productType.client"), PROVISIONING(3,
            "productType.provisioning"), SHARES(4, "productType.shares");

    private final Integer value;
    private final String code;

    private PortfolioProductType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    @Override
    public String toString() {
        return name().toString().replaceAll("_", " ");
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    private static final Map<Integer, PortfolioProductType> intToEnumMap = new HashMap<>();

    static {
        for (final PortfolioProductType type : PortfolioProductType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static PortfolioProductType fromInt(final int i) {
        final PortfolioProductType type = intToEnumMap.get(Integer.valueOf(i));
        return type;
    }

    public boolean isSavingProduct() {
        return this.value.equals(PortfolioProductType.SAVING.getValue());
    }

    public boolean isLoanProduct() {
        return this.value.equals(PortfolioProductType.LOAN.getValue());
    }

    public boolean isClient() {
        return this.value.equals(PortfolioProductType.CLIENT.getValue());
    }

    public boolean isShareProduct() {
        return this.value.equals(PortfolioProductType.SHARES.getValue());
    }

}
