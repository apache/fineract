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
package org.apache.fineract.cob.loan;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.service.LoanBuyDownFeeAmortizationProcessingService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BuyDownFeeAmortizationBusinessStep implements LoanCOBBusinessStep {

    private final LoanBuyDownFeeAmortizationProcessingService loanBuyDownFeeAmortizationProcessingService;

    @Transactional
    @Override
    public Loan execute(Loan loan) {
        if (!loan.getLoanProductRelatedDetail().isEnableBuyDownFee()) {
            return loan;
        }

        LocalDate businessDate = DateUtils.getBusinessLocalDate();

        loanBuyDownFeeAmortizationProcessingService.processBuyDownFeeAmortizationTillDate(loan, businessDate, true);

        return loan;
    }

    @Override
    public String getEnumStyledName() {
        return "BUY_DOWN_FEE_AMORTIZATION";
    }

    @Override
    public String getHumanReadableName() {
        return "Buy Down Fee amortization";
    }
}
