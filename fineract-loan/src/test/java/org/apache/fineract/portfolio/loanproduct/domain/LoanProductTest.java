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
package org.apache.fineract.portfolio.loanproduct.domain;

import org.apache.fineract.portfolio.common.domain.DaysInYearCustomStrategyType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductGeneralRuleException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LoanProductTest {

    @Test
    void testCanCreateLoanProductWithDaysInYearCustomStrategy() {
        LoanProduct loanProduct = new LoanProduct();
        loanProduct.setLoanProductRelatedDetail(new LoanProductRelatedDetail());
        loanProduct.getLoanProductRelatedDetail().setLoanScheduleType(LoanScheduleType.PROGRESSIVE);
        loanProduct.getLoanProductRelatedDetail().setDaysInYearType(1);
        loanProduct.getLoanProductRelatedDetail().setDaysInYearCustomStrategy(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY);
        loanProduct.validateLoanProductPreSave();
    }

    @Test
    void testValidationExceptionForCreateCumulativeLoanProductWithDaysInYearCustomStrategy() {
        LoanProduct loanProduct = new LoanProduct();
        loanProduct.setLoanProductRelatedDetail(new LoanProductRelatedDetail());
        loanProduct.getLoanProductRelatedDetail().setLoanScheduleType(LoanScheduleType.CUMULATIVE);
        loanProduct.getLoanProductRelatedDetail().setDaysInYearType(1);
        loanProduct.getLoanProductRelatedDetail().setDaysInYearCustomStrategy(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY);
        Assertions.assertThrowsExactly(LoanProductGeneralRuleException.class, loanProduct::validateLoanProductPreSave);
    }

    @Test
    void testValidationExceptionForCreate360LoanProductWithDaysInYearCustomStrategy() {
        LoanProduct loanProduct = new LoanProduct();
        loanProduct.setLoanProductRelatedDetail(new LoanProductRelatedDetail());
        loanProduct.getLoanProductRelatedDetail().setLoanScheduleType(LoanScheduleType.PROGRESSIVE);
        loanProduct.getLoanProductRelatedDetail().setDaysInYearType(360);
        loanProduct.getLoanProductRelatedDetail().setDaysInYearCustomStrategy(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY);
        Assertions.assertThrowsExactly(LoanProductGeneralRuleException.class, loanProduct::validateLoanProductPreSave);
    }

    @Test
    void testCanCreate360LoanProductWithoutDaysInYearCustomStrategy() {
        LoanProduct loanProduct = new LoanProduct();
        loanProduct.setLoanProductRelatedDetail(new LoanProductRelatedDetail());
        loanProduct.getLoanProductRelatedDetail().setLoanScheduleType(LoanScheduleType.PROGRESSIVE);
        loanProduct.getLoanProductRelatedDetail().setDaysInYearType(360);
        loanProduct.validateLoanProductPreSave();
    }
}
