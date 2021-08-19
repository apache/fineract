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
package org.apache.fineract.portfolio.creditscorecard.domain;

import java.util.ArrayList;
import java.util.List;

public enum FeatureCategory {

    INDIVIDUAL(0, "featureCategory.individual"), ORGANISATION(1, "featureCategory.organisation"), COUNTRY(2,
            "featureCategory.country"), CREDIT_HISTORY(3,
                    "featureCategory.creditHistory"), LOAN(3, "featureCategory.loan"), INVALID(4, "featureCategory.invalid");

    private final Integer value;
    private final String code;

    FeatureCategory(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static FeatureCategory fromInt(final Integer selectedType) {
        FeatureCategory featureCategory = INVALID;
        if (selectedType != null) {
            switch (selectedType) {
                case 0:
                    featureCategory = INDIVIDUAL;
                break;
                case 1:
                    featureCategory = ORGANISATION;
                break;
                case 2:
                    featureCategory = COUNTRY;
                break;
                case 3:
                    featureCategory = CREDIT_HISTORY;
                break;
                case 4:
                    featureCategory = LOAN;
                break;
            }
        }
        return featureCategory;
    }

    public boolean isIndividual() {
        return this.value.equals(INDIVIDUAL.getValue());
    }

    public boolean isOrganisation() {
        return this.value.equals(ORGANISATION.getValue());
    }

    public boolean isCountry() {
        return this.value.equals(COUNTRY.getValue());
    }

    public boolean isCreditHistory() {
        return this.value.equals(CREDIT_HISTORY.getValue());
    }

    public boolean isLoan() {
        return this.value.equals(LOAN.getValue());
    }

    public boolean isInvalid() {
        return this.value.equals(INVALID.getValue());
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final FeatureCategory enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }
        return values.toArray();
    }

    public static Object[] validValues() {
        return new Integer[] { INDIVIDUAL.getValue(), ORGANISATION.getValue(), COUNTRY.getValue(), CREDIT_HISTORY.getValue(),
                LOAN.getValue() };
    }
}
