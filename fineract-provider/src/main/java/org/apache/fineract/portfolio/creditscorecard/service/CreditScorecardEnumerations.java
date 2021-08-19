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
package org.apache.fineract.portfolio.creditscorecard.service;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.creditscorecard.domain.FeatureCategory;
import org.apache.fineract.portfolio.creditscorecard.domain.FeatureDataType;
import org.apache.fineract.portfolio.creditscorecard.domain.FeatureValueType;

public final class CreditScorecardEnumerations {

    public static final String FEATURE_VALUE_TYPE = "valueType";
    public static final String FEATURE_DATA_TYPE = "dataType";
    public static final String FEATURE_CATEGORY = "category";

    private CreditScorecardEnumerations() {
        //
    }

    public static EnumOptionData scorecardEnumueration(final String typeName, final int id) {
        switch (typeName) {
            case FEATURE_VALUE_TYPE:
                return featureValueType(id);
            case FEATURE_DATA_TYPE:
                return featureDataType(id);
            case FEATURE_CATEGORY:
                return featureCategory(id);
        }
        return null;
    }

    public static EnumOptionData featureValueType(final Integer id) {
        return featureValueType(FeatureValueType.fromInt(id));
    }

    public static EnumOptionData featureValueType(final FeatureValueType valueType) {
        EnumOptionData optionData = null;
        switch (valueType) {
            case BINARY:
                optionData = new EnumOptionData(FeatureValueType.BINARY.getValue().longValue(), FeatureValueType.BINARY.getCode(),
                        "Binary");
            break;
            case NOMINAL:
                optionData = new EnumOptionData(FeatureValueType.NOMINAL.getValue().longValue(), FeatureValueType.NOMINAL.getCode(),
                        "Nominal");
            break;
            case INTERVAL:
                optionData = new EnumOptionData(FeatureValueType.INTERVAL.getValue().longValue(), FeatureValueType.INTERVAL.getCode(),
                        "Interval");
            break;
            case RATIO:
                optionData = new EnumOptionData(FeatureValueType.RATIO.getValue().longValue(), FeatureValueType.RATIO.getCode(), "Ratio");
            break;
            default:
                optionData = new EnumOptionData(FeatureValueType.INVALID.getValue().longValue(), FeatureValueType.INVALID.getCode(),
                        "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData featureDataType(final Integer id) {
        return featureDataType(FeatureDataType.fromInt(id));
    }

    public static EnumOptionData featureDataType(final FeatureDataType valueType) {
        EnumOptionData optionData = null;
        switch (valueType) {
            case NUMERIC:
                optionData = new EnumOptionData(FeatureDataType.NUMERIC.getValue().longValue(), FeatureDataType.NUMERIC.getCode(),
                        "Numeric");
            break;
            case STRING:
                optionData = new EnumOptionData(FeatureDataType.STRING.getValue().longValue(), FeatureDataType.STRING.getCode(), "String");
            break;
            case DATE:
                optionData = new EnumOptionData(FeatureDataType.DATE.getValue().longValue(), FeatureDataType.DATE.getCode(), "Date");
            break;
            default:
                optionData = new EnumOptionData(FeatureValueType.INVALID.getValue().longValue(), FeatureValueType.INVALID.getCode(),
                        "Invalid");
            break;
        }
        return optionData;
    }

    public static EnumOptionData featureCategory(final Integer id) {
        return featureCategory(FeatureCategory.fromInt(id));
    }

    public static EnumOptionData featureCategory(final FeatureCategory category) {
        EnumOptionData optionData = null;
        switch (category) {
            case INDIVIDUAL:
                optionData = new EnumOptionData(FeatureCategory.INDIVIDUAL.getValue().longValue(), FeatureCategory.INDIVIDUAL.getCode(),
                        "Individual");
            break;
            case ORGANISATION:
                optionData = new EnumOptionData(FeatureCategory.ORGANISATION.getValue().longValue(), FeatureCategory.ORGANISATION.getCode(),
                        "Organisation");
            break;
            case COUNTRY:
                optionData = new EnumOptionData(FeatureCategory.COUNTRY.getValue().longValue(), FeatureCategory.COUNTRY.getCode(),
                        "Country");
            break;
            case CREDIT_HISTORY:
                optionData = new EnumOptionData(FeatureCategory.CREDIT_HISTORY.getValue().longValue(),
                        FeatureCategory.CREDIT_HISTORY.getCode(), "Credit History");
            break;
            case LOAN:
                optionData = new EnumOptionData(FeatureCategory.LOAN.getValue().longValue(), FeatureCategory.LOAN.getCode(), "Loan");
            break;
            default:
                optionData = new EnumOptionData(FeatureValueType.INVALID.getValue().longValue(), FeatureValueType.INVALID.getCode(),
                        "Invalid");
            break;
        }
        return optionData;
    }

}
