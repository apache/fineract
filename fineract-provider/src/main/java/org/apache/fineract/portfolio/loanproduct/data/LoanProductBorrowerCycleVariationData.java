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
package org.apache.fineract.portfolio.loanproduct.data;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductParamType;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductValueConditionType;

public class LoanProductBorrowerCycleVariationData {

    @SuppressWarnings("unused")
    private final Long id;
    private final Integer borrowerCycleNumber;
    private final EnumOptionData paramType;
    private final EnumOptionData valueConditionType;
    @SuppressWarnings("unused")
    private final BigDecimal minValue;
    @SuppressWarnings("unused")
    private final BigDecimal maxValue;
    private final BigDecimal defaultValue;

    public LoanProductBorrowerCycleVariationData(final Long id, final Integer borrowerCycleNumber, final EnumOptionData paramType,
            final EnumOptionData valueConditionType, final BigDecimal defaultValue, final BigDecimal minValue, final BigDecimal maxValue) {
        this.id = id;
        this.borrowerCycleNumber = borrowerCycleNumber;
        this.paramType = paramType;
        this.valueConditionType = valueConditionType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.defaultValue = defaultValue;
    }

    public LoanProductParamType getParamType() {
        return LoanProductParamType.fromInt(this.paramType.getId().intValue());
    }

    public Integer getBorrowerCycleNumber() {
        return this.borrowerCycleNumber;
    }

    public LoanProductValueConditionType getValueConditionType() {
        return LoanProductValueConditionType.fromInt(this.valueConditionType.getId().intValue());
    }

    public BigDecimal getDefaultValue() {
        return this.defaultValue;
    }

}
