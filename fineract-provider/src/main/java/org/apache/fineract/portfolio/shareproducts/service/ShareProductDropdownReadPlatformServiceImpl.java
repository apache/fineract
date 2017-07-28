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
package org.apache.fineract.portfolio.shareproducts.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.shareaccounts.service.SharesEnumerations;
import org.apache.fineract.portfolio.shareproducts.SharePeriodFrequencyType;
import org.springframework.stereotype.Service;

@Service
public class ShareProductDropdownReadPlatformServiceImpl implements ShareProductDropdownReadPlatformService {

    @Override
    public Collection<EnumOptionData> retrieveLockinPeriodFrequencyTypeOptions() {
        final List<EnumOptionData> allowedLockinPeriodFrequencyTypeOptions = Arrays.asList( //
                SharesEnumerations.lockinPeriodFrequencyType(SharePeriodFrequencyType.DAYS), //
                SharesEnumerations.lockinPeriodFrequencyType(SharePeriodFrequencyType.WEEKS), //
                SharesEnumerations.lockinPeriodFrequencyType(SharePeriodFrequencyType.MONTHS), //
                SharesEnumerations.lockinPeriodFrequencyType(SharePeriodFrequencyType.YEARS) //
                );

        return allowedLockinPeriodFrequencyTypeOptions;
    }

    @Override
    public Collection<EnumOptionData> retrieveMinimumActivePeriodFrequencyTypeOptions() {
        final List<EnumOptionData> minimumActivePeriodFrequencyTypeOptions = Arrays.asList( //
                SharesEnumerations.lockinPeriodFrequencyType(SharePeriodFrequencyType.DAYS) //
                );

        return minimumActivePeriodFrequencyTypeOptions;
    }
}
