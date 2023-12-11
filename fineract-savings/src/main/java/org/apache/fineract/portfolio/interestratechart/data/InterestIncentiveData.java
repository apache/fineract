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
package org.apache.fineract.portfolio.interestratechart.data;

import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public final class InterestIncentiveData {

    @SuppressWarnings("unused")
    private final Long id;
    private final EnumOptionData entityType;
    private final EnumOptionData attributeName;
    private final EnumOptionData conditionType;
    private final String attributeValue;
    private final String attributeValueDesc;
    private final EnumOptionData incentiveType;
    private final BigDecimal amount;

    public static InterestIncentiveData instance(final Long id, final EnumOptionData entityType, final EnumOptionData attributeName,
            final EnumOptionData conditionType, final String attributeValue, final String attributeValueDesc,
            final EnumOptionData incentiveType, final BigDecimal amount) {

        return new InterestIncentiveData(id, entityType, attributeName, conditionType, attributeValue, attributeValueDesc, incentiveType,
                amount);
    }

    private InterestIncentiveData(final Long id, final EnumOptionData entityType, final EnumOptionData attributeName,
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

    public EnumOptionData entityType() {
        return this.entityType;
    }

    public EnumOptionData attributeName() {
        return this.attributeName;
    }

    public EnumOptionData conditionType() {
        return this.conditionType;
    }

    public String attributeValue() {
        return this.attributeValue;
    }

    public EnumOptionData incentiveType() {
        return this.incentiveType;
    }

    public BigDecimal amount() {
        return this.amount;
    }

    public String attributeValueDesc() {
        return this.attributeValueDesc;
    }

}
