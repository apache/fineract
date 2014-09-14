/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.savings.PreClosurePenalInterestOnType;
import org.springframework.stereotype.Service;

@Service
public class DepositsDropdownReadPlatformServiceImpl implements DepositsDropdownReadPlatformService {

    @Override
    public Collection<EnumOptionData> retrievePreClosurePenalInterestOnTypeOptions() {
        return SavingsEnumerations.preClosurePenaltyInterestOnType(PreClosurePenalInterestOnType.values());
    }

    /*
     * @Override public Collection<EnumOptionData>
     * retrieveDepositTermTypeOptions() { return
     * SavingsEnumerations.recurringDepositFrequencyType
     * (SavingsPeriodFrequencyType.values()); }
     * 
     * @Override public Collection<EnumOptionData>
     * retrieveDepositPeriodFrequencyOptions() { return
     * SavingsEnumerations.depositPeriodFrequency
     * (SavingsPeriodFrequencyType.values()); }
     * 
     * @Override public Collection<EnumOptionData>
     * retrieveInMultiplesOfDepositTermTypeOptions() { return
     * SavingsEnumerations
     * .recurringDepositFrequencyType(SavingsPeriodFrequencyType.values()); }
     */

}