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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;

/**
 * Multi value attribute holding the loan transaction types that must be kept out of external asset owner accounting and
 * reporting. The value is a comma separated list of {@link LoanTransactionType} names.
 * <p>
 * This cannot be modelled as an enum the way {@link SettlementModelExternalAssetOwnerLoanProductAttribute} is, because
 * the allowed values are the constants of another enum and any combination of them is a valid attribute value.
 */
public class ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute implements ExternalAssetOwnerLoanProductAttribute {

    public static final String ATTRIBUTE_KEY = "EXCLUDED_TRANSACTION_TYPES";

    private static final String SEPARATOR = ",";

    @Override
    public String getAttributeKey() {
        return ATTRIBUTE_KEY;
    }

    @Override
    public String getAttributeValue() {
        return null;
    }

    @Override
    public List<String> getAttributeValues() {
        return Arrays.stream(LoanTransactionType.values()).filter(type -> !LoanTransactionType.INVALID.equals(type)).map(Enum::name)
                .toList();
    }

    @Override
    public boolean validate(String attributeValue) {
        if (attributeValue == null || attributeValue.isBlank()) {
            return false;
        }
        List<String> allowedValues = getAttributeValues();
        Set<String> seenValues = new LinkedHashSet<>();
        for (String token : attributeValue.split(SEPARATOR, -1)) {
            String normalizedToken = normalizeToken(token);
            if (!allowedValues.contains(normalizedToken) || !seenValues.add(normalizedToken)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isMultiValue() {
        return true;
    }

    @Override
    public String normalize(String attributeValue) {
        List<String> normalizedTokens = new ArrayList<>();
        for (String token : attributeValue.split(SEPARATOR, -1)) {
            normalizedTokens.add(normalizeToken(token));
        }
        return String.join(SEPARATOR, normalizedTokens);
    }

    private String normalizeToken(String token) {
        return token.trim().toUpperCase(Locale.ROOT);
    }
}
