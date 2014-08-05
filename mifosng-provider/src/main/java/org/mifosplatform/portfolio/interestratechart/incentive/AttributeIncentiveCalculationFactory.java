/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.incentive;

public class AttributeIncentiveCalculationFactory {

    public static AttributeIncentiveCalculation findAttributeIncentiveCalculation(InterestIncentiveEntityType entityType) {
        AttributeIncentiveCalculation attributeIncentiveCalculation = null;
        switch (entityType) {
            case CUSTOMER:
                attributeIncentiveCalculation = new ClientAttributeIncentiveCalculation();
            break;
            default:
            break;
        }
        return attributeIncentiveCalculation;
    }

}
