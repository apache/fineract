/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.common.domain.ConditionType;
import org.mifosplatform.portfolio.common.service.CommonEnumerations;
import org.mifosplatform.portfolio.interestratechart.incentive.InterestIncentiveAttributeName;
import org.mifosplatform.portfolio.interestratechart.incentive.InterestIncentiveEntityType;
import org.mifosplatform.portfolio.interestratechart.incentive.InterestIncentiveType;
import org.springframework.stereotype.Service;

@Service
public class InterestIncentivesDropdownReadPlatformServiceImpl implements InterestIncentiveDropdownReadPlatformService {

    @Override
    public Collection<EnumOptionData> retrieveEntityTypeOptions() {
        return InterestIncentivesEnumerations.entityType(InterestIncentiveEntityType.values());
    }

    @Override
    public Collection<EnumOptionData> retrieveAttributeNameOptions() {
        return InterestIncentivesEnumerations.attributeName(InterestIncentiveAttributeName.values());
    }

    @Override
    public Collection<EnumOptionData> retrieveConditionTypeOptions() {
        return CommonEnumerations.conditionType(ConditionType.values(), "incentive");
    }

    @Override
    public Collection<EnumOptionData> retrieveIncentiveTypeOptions() {
        return InterestIncentivesEnumerations.incentiveType(InterestIncentiveType.values());
    }

}
