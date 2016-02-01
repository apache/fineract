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
package org.apache.fineract.portfolio.interestratechart.incentive;

import java.math.BigDecimal;

import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.interestratechart.domain.InterestIncentivesFields;
import org.joda.time.LocalDate;
import org.joda.time.Years;

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
