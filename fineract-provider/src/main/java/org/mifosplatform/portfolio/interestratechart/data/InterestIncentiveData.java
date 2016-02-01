/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.data;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class InterestIncentiveData {

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
