/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.incentive;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.interestratechart.domain.InterestIncentivesFields;

public class ClientAttributeIncentiveCalculation extends AttributeIncentiveCalculation {

    @Override
    public BigDecimal calculateIncentive(IncentiveDTO incentiveDTO) {
        final Client client = incentiveDTO.client();
        BigDecimal interest = incentiveDTO.interest();
        final InterestIncentivesFields incentivesFields = incentiveDTO.incentives();
        boolean applyIncentive = false;
        switch (incentivesFields.attributeName()) {
            case GENDER:
                if (client.genderId() != null) {
                    applyIncentive = applyIncentive(incentivesFields.conditionType(), Long.valueOf(incentivesFields.attributeValue()),
                            client.genderId());
                }
            break;
            case AGE:
                if (client.dateOfBirth() != null) {
                    final LocalDate dobLacalDate = LocalDate.fromDateFields(client.dateOfBirth());
                    final int age = Years.yearsBetween(dobLacalDate, LocalDate.now()).getYears();
                    applyIncentive = applyIncentive(incentivesFields.conditionType(), Long.valueOf(incentivesFields.attributeValue()),
                            Long.valueOf(age));
                }
            break;
            case CLIENT_TYPE:
                if (client.clientTypeId() != null) {
                    applyIncentive = applyIncentive(incentivesFields.conditionType(), Long.valueOf(incentivesFields.attributeValue()),
                            client.clientTypeId());
                }
            break;
            case CLIENT_CLASSIFICATION:
                if (client.clientClassificationId() != null) {
                    applyIncentive = applyIncentive(incentivesFields.conditionType(), Long.valueOf(incentivesFields.attributeValue()),
                            client.clientClassificationId());
                }
            break;

            default:
            break;

        }
        if (applyIncentive) {
            switch (incentivesFields.incentiveType()) {
                case FIXED:
                    interest = incentivesFields.amount();
                break;
                case INCENTIVE:
                    interest = interest.add(incentivesFields.amount());
                break;
                default:
                break;

            }
        }

        return interest;
    }

}
