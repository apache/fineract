/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.incentive;

import java.math.BigDecimal;

import org.mifosplatform.portfolio.common.domain.ConditionType;

public abstract class AttributeIncentiveCalculation {

    public abstract BigDecimal calculateIncentive(final IncentiveDTO incentiveDTO);

    public boolean applyIncentive(ConditionType conditionType, Long attributeValue, Long actualValue) {
        boolean applyIncentive = false;
        int compareVal = actualValue.compareTo(attributeValue);
        switch (conditionType) {
            case LESSTHAN:
                applyIncentive = compareVal < 0;
            break;
            case EQUAL:
                applyIncentive = compareVal == 0;
            break;
            case NOT_EQUAL:
                applyIncentive = compareVal != 0;
            break;
            case GRETERTHAN:
                applyIncentive = compareVal > 0;
            break;
            default:
                applyIncentive = false;
            break;
        }
        return applyIncentive;
    }
}
