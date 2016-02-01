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

import java.util.List;

import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;

/**
 * Transfer object to return the schedule after generation of schedule
 */
public class LoanScheduleDTO {

    private final List<LoanRepaymentScheduleInstallment> installments;
    private final LoanScheduleModel loanScheduleModel;

    private LoanScheduleDTO(final List<LoanRepaymentScheduleInstallment> installments, final LoanScheduleModel loanScheduleModel) {
        this.installments = installments;
        this.loanScheduleModel = loanScheduleModel;
    }
    
    public static LoanScheduleDTO from(final List<LoanRepaymentScheduleInstallment> installments, final LoanScheduleModel loanScheduleModel){
        return new LoanScheduleDTO(installments, loanScheduleModel);
    }
    
    public List<LoanRepaymentScheduleInstallment> getInstallments() {
        return this.installments;
    }

    public LoanScheduleModel getLoanScheduleModel() {
        return this.loanScheduleModel;
    }

}
