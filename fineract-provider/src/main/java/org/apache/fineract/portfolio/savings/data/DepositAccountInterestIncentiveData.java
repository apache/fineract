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
package org.apache.fineract.portfolio.savings.data;

import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.interestratechart.data.InterestIncentiveData;

public final class DepositAccountInterestIncentiveData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final EnumOptionData entityType;
    @SuppressWarnings("unused")
    private final EnumOptionData attributeName;
    @SuppressWarnings("unused")
    private final EnumOptionData conditionType;
    @SuppressWarnings("unused")
    private final String attributeValue;
    @SuppressWarnings("unused")
    private final String attributeValueDesc;
    @SuppressWarnings("unused")
    private final EnumOptionData incentiveType;
    @SuppressWarnings("unused")
    private final BigDecimal amount;

    public static DepositAccountInterestIncentiveData instance(final Long id, final EnumOptionData entityType,
            final EnumOptionData attributeName, final EnumOptionData conditionType, final String attributeValue,
            final String attributeValueDesc, final EnumOptionData incentiveType, final BigDecimal amount) {

        return new DepositAccountInterestIncentiveData(id, entityType, attributeName, conditionType, attributeValue, attributeValueDesc,
                incentiveType, amount);
    }

    public static DepositAccountInterestIncentiveData from(final InterestIncentiveData incentiveData) {
        final Long id = null;
        return new DepositAccountInterestIncentiveData(id, incentiveData.entityType(), incentiveData.attributeName(),
                incentiveData.conditionType(), incentiveData.attributeValue(), incentiveData.attributeValueDesc(),
                incentiveData.incentiveType(), incentiveData.amount());
    }

    private DepositAccountInterestIncentiveData(final Long id, final EnumOptionData entityType, final EnumOptionData attributeName,
            final EnumOptionData conditionType, final String attributeValue, final String attributeValueDesc,
            final EnumOptionData incentiveType, final BigDecimal amount) {

        this.id = id;
        this.entityType = entityType;
        this.attributeName = attributeName;
        this.conditionType = conditionType;
        this.attributeValue = attributeValue;
        this.attributeValueDesc = attributeValueDesc;
        this.incentiveType = incentiveType;
        this.amount = amount;

    }

}
