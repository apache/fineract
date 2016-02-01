/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.service;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanproduct.data.TransactionProcessingStrategyData;

public interface LoanDropdownReadPlatformService {

    List<EnumOptionData> retrieveLoanAmortizationTypeOptions();

    List<EnumOptionData> retrieveLoanInterestTypeOptions();

    List<EnumOptionData> retrieveLoanInterestRateCalculatedInPeriodOptions();

    List<EnumOptionData> retrieveLoanTermFrequencyTypeOptions();

    List<EnumOptionData> retrieveRepaymentFrequencyTypeOptions();
    
    List<EnumOptionData> retrieveRepaymentFrequencyOptionsForNthDayOfMonth();
    
    List<EnumOptionData> retrieveRepaymentFrequencyOptionsForDaysOfWeek();

    List<EnumOptionData> retrieveInterestRateFrequencyTypeOptions();

    Collection<TransactionProcessingStrategyData> retreiveTransactionProcessingStrategies();

    List<EnumOptionData> retrieveLoanCycleValueConditionTypeOptions();

    List<EnumOptionData> retrieveInterestRecalculationCompoundingTypeOptions();

    List<EnumOptionData> retrieveRescheduleStrategyTypeOptions();
    
    List<EnumOptionData> retrieveInterestRecalculationFrequencyTypeOptions();
    
    List<EnumOptionData> retrivePreCloseInterestCalculationStrategyOptions();

}