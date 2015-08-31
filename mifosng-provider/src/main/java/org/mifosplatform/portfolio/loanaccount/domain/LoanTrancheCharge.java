/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mifosplatform.portfolio.loanaccount.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.portfolio.charge.domain.Charge;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loan_tranche_charges")
public class LoanTrancheCharge extends AbstractPersistable<Long> {

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
    
    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "charge_id", nullable = false)
    private Charge charge;
    
    LoanTrancheCharge() {

    }
    
    LoanTrancheCharge(Charge chargeDefinition) {
        this.charge = chargeDefinition ;
    }
    public LoanTrancheCharge(Charge charge, Loan loan) {
        this.charge = charge;
        this.loan = loan ;
    }
    
    public static LoanTrancheCharge createLoanTrancheCharge(Charge chargeDefinition) {
        return new LoanTrancheCharge(chargeDefinition) ;
    }
    public static LoanTrancheCharge createLoanTrancheChargeWithLoan(Charge chargeDefinition, Loan loan) {
        return new LoanTrancheCharge(chargeDefinition, loan) ;
    }
    
    public Charge getCharge() {
        return charge ;
    }
}
