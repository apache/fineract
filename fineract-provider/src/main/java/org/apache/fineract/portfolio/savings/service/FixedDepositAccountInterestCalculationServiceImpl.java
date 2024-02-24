package org.apache.fineract.portfolio.savings.service;

import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.savings.data.DepositAccountDataValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FixedDepositAccountInterestCalculationServiceImpl implements FixedDepositAccountInterestCalculationService{

    private final DepositAccountDataValidator depositAccountDataValidator;
    private final FromJsonHelper fromApiJsonHelper;
    @Override
    public double calculateInterest(JsonQuery query) {
        depositAccountDataValidator.validateFixedDepositForInterestCalculation(query.json());
        JsonElement element = query.parsedJson();
        Long principalAmount = this.fromApiJsonHelper.extractLongNamed("principalAmount", element);
        BigDecimal annualInterestRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("annualInterestRate", element);
        Long tenureInMonths = this.fromApiJsonHelper.extractLongNamed("tenureInMonths", element);
        Long interestPostingPeriodInMonths = this.fromApiJsonHelper.extractLongNamed("interestPostingPeriodInMonths", element);
        Long interestCompoundingPeriodInMonths = this.fromApiJsonHelper.extractLongNamed("interestCompoundingPeriodInMonths", element);

        double n = (12/(double)interestCompoundingPeriodInMonths);
        double r = annualInterestRate.doubleValue();
        double maturityAmount =principalAmount * (Math.pow((1+(r/(n*100))),((tenureInMonths*n)/12)));
        return maturityAmount;
    }
}
