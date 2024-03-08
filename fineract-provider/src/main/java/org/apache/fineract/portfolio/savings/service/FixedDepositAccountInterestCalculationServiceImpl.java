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

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.annualInterestRateParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.interestCompoundingPeriodInMonthsParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.principalAmountParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.tenureInMonthsParamName;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.savings.data.DepositAccountDataValidator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FixedDepositAccountInterestCalculationServiceImpl implements FixedDepositAccountInterestCalculationService {

    private final DepositAccountDataValidator depositAccountDataValidator;
    private final FromJsonHelper fromApiJsonHelper;

    @Override
    public HashMap calculateInterest(JsonQuery query) {
        depositAccountDataValidator.validateFixedDepositForInterestCalculation(query.json());
        JsonElement element = query.parsedJson();
        BigDecimal principalAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(principalAmountParamName, element);
        BigDecimal annualInterestRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(annualInterestRateParamName, element);
        Long tenureInMonths = this.fromApiJsonHelper.extractLongNamed(tenureInMonthsParamName, element);
        Long interestCompoundingPeriodInMonths = this.fromApiJsonHelper.extractLongNamed(interestCompoundingPeriodInMonthsParamName,
                element);
        BigDecimal maturityAmount = this.calculateInterestInternal(principalAmount, annualInterestRate, tenureInMonths,
                interestCompoundingPeriodInMonths);
        String warning = "This is an approximate calculated amount - it may vary slightly when the account is created";

        HashMap result = new HashMap<>();
        result.put("maturityAmount", maturityAmount);
        result.put("warning", warning);

        return result;
    }

    public BigDecimal calculateInterestInternal(BigDecimal principalAmount, BigDecimal annualInterestRate, Long tenureInMonths,
            Long interestCompoundingPeriodInMonths) {
        BigDecimal numberOfCompoundingsPerAnnum = BigDecimal.valueOf(12).divide(BigDecimal.valueOf(interestCompoundingPeriodInMonths));
        Long totalNumberOfCompoundings = tenureInMonths / interestCompoundingPeriodInMonths;
        MathContext mc = MoneyHelper.getMathContext();
        BigDecimal exponentialTerm = annualInterestRate.divide(numberOfCompoundingsPerAnnum, mc).divide(BigDecimal.valueOf(100), mc)
                .add(BigDecimal.valueOf(1)).pow(Math.toIntExact(totalNumberOfCompoundings));
        return principalAmount.multiply(exponentialTerm);
    }
}
