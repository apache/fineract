/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.interestratechart.data.InterestIncentiveData;

public class DepositAccountInterestIncentiveData {

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
