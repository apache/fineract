/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.data;

import java.util.List;

import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;

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
