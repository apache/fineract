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

public enum FeatureDataType {

    NUMERIC(0, "featureDataType.numeric"), STRING(1, "featureDataType.string"), DATE(2, "featureDataType.date"), INVALID(3,
            "featureDataType.invalid");

    private final Integer value;
    private final String code;

    FeatureDataType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static FeatureDataType fromInt(final Integer selectedType) {
        FeatureDataType featureDataType = INVALID;
        if (selectedType != null) {
            switch (selectedType) {
                case 0:
                    featureDataType = NUMERIC;
                break;
                case 1:
                    featureDataType = STRING;
                break;
                case 2:
                    featureDataType = DATE;
                break;
            }
        }
        return featureDataType;
    }

    public boolean isNumeric() {
        return this.value.equals(NUMERIC.getValue());
    }

    public boolean isString() {
        return this.value.equals(STRING.getValue());
    }

    public boolean isDate() {
        return this.value.equals(DATE.getValue());
    }

    public boolean isInvalid() {
        return this.value.equals(INVALID.getValue());
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final FeatureDataType enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }
        return values.toArray();
    }

    public static Object[] validValues() {
        return new Integer[] { NUMERIC.getValue(), STRING.getValue(), DATE.getValue() };
    }
}
