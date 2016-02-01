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
package org.apache.fineract.portfolio.savings.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsWithdrawalFeesType;
import org.springframework.stereotype.Service;

@Service
public class SavingsDropdownReadPlatformServiceImpl implements SavingsDropdownReadPlatformService {

    @Override
    public Collection<EnumOptionData> retrievewithdrawalFeeTypeOptions() {
        final List<EnumOptionData> allowedOptions = Arrays.asList( //
                SavingsEnumerations.withdrawalFeeType(SavingsWithdrawalFeesType.FLAT), //
                SavingsEnumerations.withdrawalFeeType(SavingsWithdrawalFeesType.PERCENT_OF_AMOUNT) //
                );

        return allowedOptions;
    }

    @Override
    public List<EnumOptionData> retrieveLockinPeriodFrequencyTypeOptions() {
        final List<EnumOptionData> allowedLockinPeriodFrequencyTypeOptions = Arrays.asList( //
                SavingsEnumerations.lockinPeriodFrequencyType(SavingsPeriodFrequencyType.DAYS), //
                SavingsEnumerations.lockinPeriodFrequencyType(SavingsPeriodFrequencyType.WEEKS), //
                SavingsEnumerations.lockinPeriodFrequencyType(SavingsPeriodFrequencyType.MONTHS), //
                SavingsEnumerations.lockinPeriodFrequencyType(SavingsPeriodFrequencyType.YEARS) //
                );

        return allowedLockinPeriodFrequencyTypeOptions;
    }

    @Override
    public Collection<EnumOptionData> retrieveCompoundingInterestPeriodTypeOptions() {
        final List<EnumOptionData> allowedOptions = Arrays.asList(
                //
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.DAILY), //
                // SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.WEEKLY),
                // //
                // SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.BIWEEKLY),
                // //
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.MONTHLY),
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.QUATERLY),
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.BI_ANNUAL),
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.ANNUAL)
        // //
        // SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.NO_COMPOUNDING_SIMPLE_INTEREST)
        // //
                );

        return allowedOptions;
    }

    @Override
    public Collection<EnumOptionData> retrieveInterestPostingPeriodTypeOptions() {
        final List<EnumOptionData> allowedOptions = Arrays.asList( //
                SavingsEnumerations.interestPostingPeriodType(SavingsPostingInterestPeriodType.MONTHLY), //
                SavingsEnumerations.interestPostingPeriodType(SavingsPostingInterestPeriodType.QUATERLY), //
                SavingsEnumerations.interestPostingPeriodType(SavingsPostingInterestPeriodType.BIANNUAL), //
                SavingsEnumerations.interestPostingPeriodType(SavingsPostingInterestPeriodType.ANNUAL) //
                );

        return allowedOptions;
    }

    @Override
    public Collection<EnumOptionData> retrieveInterestCalculationTypeOptions() {
        final List<EnumOptionData> allowedOptions = Arrays.asList( //
                SavingsEnumerations.interestCalculationType(SavingsInterestCalculationType.DAILY_BALANCE), //
                SavingsEnumerations.interestCalculationType(SavingsInterestCalculationType.AVERAGE_DAILY_BALANCE) //
                );

        return allowedOptions;
    }

    @Override
    public Collection<EnumOptionData> retrieveInterestCalculationDaysInYearTypeOptions() {
        final List<EnumOptionData> allowedOptions = Arrays.asList( //
                SavingsEnumerations.interestCalculationDaysInYearType(SavingsInterestCalculationDaysInYearType.DAYS_360), //
                SavingsEnumerations.interestCalculationDaysInYearType(SavingsInterestCalculationDaysInYearType.DAYS_365) //
                );

        return allowedOptions;
    }
}