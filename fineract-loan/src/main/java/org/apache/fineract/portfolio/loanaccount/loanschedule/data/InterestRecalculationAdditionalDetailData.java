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
package org.apache.fineract.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;

@Getter
@AllArgsConstructor
public final class InterestRecalculationAdditionalDetailData {

    private final LocalDate effectiveDate;
    private final BigDecimal amount;

    public static InterestRecalculationAdditionalDetailData of(LocalDate effectiveDate, BigDecimal amount) {
        return new InterestRecalculationAdditionalDetailData(effectiveDate, amount);
    }

    public LoanInterestRecalcualtionAdditionalDetails toEntity() {
        return new LoanInterestRecalcualtionAdditionalDetails(effectiveDate, amount);
    }

    public static Set<LoanInterestRecalcualtionAdditionalDetails> toEntities(Set<InterestRecalculationAdditionalDetailData> dataSet) {
        if (dataSet == null) {
            return null;
        }
        Set<LoanInterestRecalcualtionAdditionalDetails> entities = new HashSet<>();
        for (InterestRecalculationAdditionalDetailData data : dataSet) {
            entities.add(data.toEntity());
        }
        return entities;
    }
}
