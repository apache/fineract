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
package org.apache.fineract.portfolio.loanproduct.calc.data;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeInterestHandlingType;

@Getter
@Builder
public final class LoanReAgeParameterData {

    private final PeriodFrequencyType frequencyType;
    private final Integer frequencyNumber;
    private final LocalDate startDate;
    private final Integer numberOfInstallments;
    private final LoanReAgeInterestHandlingType interestHandlingType;

    public static LoanReAgeParameterData of(PeriodFrequencyType frequencyType, Integer frequencyNumber, LocalDate startDate,
            Integer numberOfInstallments, LoanReAgeInterestHandlingType interestHandlingType) {
        return LoanReAgeParameterData.builder().frequencyType(frequencyType).frequencyNumber(frequencyNumber).startDate(startDate)
                .numberOfInstallments(numberOfInstallments).interestHandlingType(interestHandlingType).build();
    }
}
