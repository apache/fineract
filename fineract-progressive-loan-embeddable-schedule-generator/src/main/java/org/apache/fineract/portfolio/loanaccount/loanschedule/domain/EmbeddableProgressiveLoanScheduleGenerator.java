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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import java.math.MathContext;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.ProgressiveLoanModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlan;
import org.apache.fineract.portfolio.loanaccount.service.InterestScheduleModelRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.calc.ProgressiveEMICalculator;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;

@SuppressWarnings("unused")
public class EmbeddableProgressiveLoanScheduleGenerator {

    private final ProgressiveLoanScheduleGenerator scheduleGenerator;
    private final ScheduledDateGenerator scheduledDateGenerator;
    private final EMICalculator emiCalculator;

    public EmbeddableProgressiveLoanScheduleGenerator() {
        this.emiCalculator = new ProgressiveEMICalculator();
        this.scheduledDateGenerator = new DefaultScheduledDateGenerator();
        this.scheduleGenerator = new ProgressiveLoanScheduleGenerator(scheduledDateGenerator, emiCalculator,
                new NoopInterestScheduleModelRepositoryWrapper());
    }

    public LoanSchedulePlan generate(final MathContext mc, final LoanRepaymentScheduleModelData modelData) {
        return scheduleGenerator.generate(mc, modelData);
    }

    private static final class NoopInterestScheduleModelRepositoryWrapper implements InterestScheduleModelRepositoryWrapper {

        @Override
        public Optional<ProgressiveLoanModel> findOneByLoanId(Long loanId) {
            return Optional.empty();
        }

        @Override
        public Optional<ProgressiveLoanModel> findOneByLoan(Loan loan) {
            return Optional.empty();
        }

        @Override
        public Optional<ProgressiveLoanInterestScheduleModel> extractModel(Optional<ProgressiveLoanModel> progressiveLoanModel) {
            return Optional.empty();
        }

        @Override
        public ProgressiveLoanInterestScheduleModel writeInterestScheduleModel(Loan loan, ProgressiveLoanInterestScheduleModel model) {
            return null;
        }

        @Override
        public Optional<ProgressiveLoanInterestScheduleModel> readProgressiveLoanInterestScheduleModel(Long loanId,
                LoanProductMinimumRepaymentScheduleRelatedDetail detail, Integer installmentAmountInMultipliesOf) {
            return Optional.empty();
        }

        @Override
        public boolean hasValidModelForDate(Long loanId, LocalDate targetDate) {
            return false;
        }

        @Override
        public Optional<ProgressiveLoanInterestScheduleModel> getSavedModel(Loan loan, LocalDate businessDate) {
            return Optional.empty();
        }
    }
}
