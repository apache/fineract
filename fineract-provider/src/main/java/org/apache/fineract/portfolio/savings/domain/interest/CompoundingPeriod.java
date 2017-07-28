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
package org.apache.fineract.portfolio.savings.domain.interest;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;

public interface CompoundingPeriod {

    BigDecimal calculateInterest(SavingsCompoundingInterestPeriodType compoundingInterestPeriodType,
            SavingsInterestCalculationType interestCalculationType, BigDecimal interestFromPreviousPostingPeriod,
            BigDecimal interestRateAsFraction, long daysInYear, BigDecimal minBalanceForInterestCalculation, 
            BigDecimal overdraftInterestRateAsFraction, BigDecimal minOverdraftForInterestCalculation);
    LocalDateInterval getPeriodInterval();
}