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
package org.apache.fineract.portfolio.loanproduct.calc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestRepaymentModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelRepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;

public interface EMICalculator {

    ProgressiveLoanInterestScheduleModel generateInterestScheduleModel(List<LoanScheduleModelRepaymentPeriod> periods,
            LoanProductRelatedDetail loanProductRelatedDetail, Integer installmentAmountInMultiplesOf, MathContext mc);

    ProgressiveLoanInterestScheduleModel generateModel(LoanProductRelatedDetail loanProductRelatedDetail,
            Integer installmentAmountInMultiplesOf, List<LoanRepaymentScheduleInstallment> repaymentPeriods, MathContext mc);

    Optional<ProgressiveLoanInterestRepaymentModel> findInterestRepaymentPeriod(ProgressiveLoanInterestScheduleModel scheduleModel,
            LocalDate dueDate);

    void addDisbursement(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate disbursementDueDate, Money disbursedAmount);

    void changeInterestRate(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate newInterestEffectiveDate,
            BigDecimal newInterestRate);

    void addBalanceCorrection(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate balanceCorrectionDate,
            Money balanceCorrectionAmount);

    Optional<ProgressiveLoanInterestRepaymentModel> getPayableDetails(ProgressiveLoanInterestScheduleModel scheduleModel,
            LocalDate periodDueDate, LocalDate payDate);

    ProgressiveLoanInterestScheduleModel makeScheduleModelDeepCopy(ProgressiveLoanInterestScheduleModel scheduleModel);

    ProgressiveLoanInterestScheduleModel makeScheduleModelDeepCopy(ProgressiveLoanInterestScheduleModel scheduleModel,
            LoanProductRelatedDetail loanProductRelatedDetail, Integer installmentAmountInMultiplesOf, MathContext mc);
}
