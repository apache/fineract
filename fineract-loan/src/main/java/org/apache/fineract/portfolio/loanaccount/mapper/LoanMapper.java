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
package org.apache.fineract.portfolio.loanaccount.mapper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanMapper {

    private final LoanTermVariationsMapper loanTermVariationsMapper;

    public LoanScheduleModel regenerateScheduleModel(final ScheduleGeneratorDTO scheduleGeneratorDTO, final Loan loan) {
        final MathContext mc = MoneyHelper.getMathContext();

        final LoanApplicationTerms loanApplicationTerms = loanTermVariationsMapper.constructLoanApplicationTerms(scheduleGeneratorDTO,
                loan);
        LoanScheduleGenerator loanScheduleGenerator;
        if (loanApplicationTerms.isEqualAmortization()) {
            if (loanApplicationTerms.getInterestMethod().isDecliningBalance()) {
                final LoanScheduleGenerator decliningLoanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory()
                        .create(loanApplicationTerms.getLoanScheduleType(), InterestMethod.DECLINING_BALANCE);
                Set<LoanCharge> loanCharges = loan.getActiveCharges();
                LoanScheduleModel loanSchedule = decliningLoanScheduleGenerator.generate(mc, loanApplicationTerms, loanCharges,
                        scheduleGeneratorDTO.getHolidayDetailDTO());

                loanApplicationTerms
                        .updateTotalInterestDue(Money.of(loanApplicationTerms.getCurrency(), loanSchedule.getTotalInterestCharged()));

            }
            loanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory().create(loanApplicationTerms.getLoanScheduleType(),
                    InterestMethod.FLAT);
        } else {
            loanScheduleGenerator = scheduleGeneratorDTO.getLoanScheduleFactory().create(loanApplicationTerms.getLoanScheduleType(),
                    loanApplicationTerms.getInterestMethod());
        }

        return loanScheduleGenerator.generate(mc, loanApplicationTerms, loan.getActiveCharges(),
                scheduleGeneratorDTO.getHolidayDetailDTO());
    }

    public List<DisbursementData> getDisbursementData(final Loan loan) {
        Iterator<LoanDisbursementDetails> iterator = loan.getDisbursementDetails().iterator();
        List<DisbursementData> disbursementData = new ArrayList<>();

        while (iterator.hasNext()) {
            LoanDisbursementDetails loanDisbursementDetails = iterator.next();

            LocalDate expectedDisbursementDate = null;
            LocalDate actualDisbursementDate = null;

            if (loanDisbursementDetails.expectedDisbursementDate() != null) {
                expectedDisbursementDate = loanDisbursementDetails.expectedDisbursementDate();
            }

            if (loanDisbursementDetails.actualDisbursementDate() != null) {
                actualDisbursementDate = loanDisbursementDetails.actualDisbursementDate();
            }
            BigDecimal waivedChargeAmount = null;
            disbursementData.add(new DisbursementData(loanDisbursementDetails.getId(), expectedDisbursementDate, actualDisbursementDate,
                    loanDisbursementDetails.principal(), loan.getNetDisbursalAmount(), null, null, waivedChargeAmount));
        }

        return disbursementData;
    }

}
