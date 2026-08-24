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
package org.apache.fineract.investor.data.attribute;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum SettlementModelExternalAssetOwnerLoanProductAttribute implements ExternalAssetOwnerLoanProductAttribute {

    DEFAULT_SETTLEMENT, //
    DELAYED_SETTLEMENT; //

    private final String attributeKey;

    SettlementModelExternalAssetOwnerLoanProductAttribute() {
        this.attributeKey = "SETTLEMENT_MODEL";
    }

    @Override
    public String getAttributeKey() {
        return attributeKey;
    }

    @Override
    public String getAttributeValue() {
        return name();
    }

    @Override
    public List<String> getAttributeValues() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }

    @Override
    public boolean validate(String attributeValue) {
        return this.getAttributeValue().equals(attributeValue.toUpperCase(Locale.ROOT));
    }

    @Override
    public boolean isMultiValue() {
        return false;
    }
}
