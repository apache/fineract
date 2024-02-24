package org.apache.fineract.portfolio.savings.service;

import org.apache.fineract.infrastructure.core.api.JsonQuery;

public interface FixedDepositAccountInterestCalculationService {
    public double calculateInterest(JsonQuery query);
}
