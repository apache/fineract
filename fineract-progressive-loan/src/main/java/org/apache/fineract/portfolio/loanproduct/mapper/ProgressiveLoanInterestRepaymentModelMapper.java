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
package org.apache.fineract.portfolio.loanproduct.mapper;

import java.util.List;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestRepaymentModel;
import org.mapstruct.Mapper;

@Mapper(config = MapstructMapperConfig.class)
public interface ProgressiveLoanInterestRepaymentModelMapper {

    List<ProgressiveLoanInterestRepaymentModel> map(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments);

    default ProgressiveLoanInterestRepaymentModel map(LoanRepaymentScheduleInstallment repaymentScheduleInstallment) {
        return new ProgressiveLoanInterestRepaymentModel(repaymentScheduleInstallment.getFromDate(),
                repaymentScheduleInstallment.getDueDate(), Money.zero(repaymentScheduleInstallment.getLoan().getCurrency()));
    }
}
