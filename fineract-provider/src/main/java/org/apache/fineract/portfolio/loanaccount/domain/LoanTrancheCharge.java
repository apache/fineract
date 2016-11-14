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
package org.apache.fineract.portfolio.loanaccount.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.charge.domain.Charge;

@Entity
@Table(name = "m_loan_tranche_charges")
public class LoanTrancheCharge extends AbstractPersistableCustom<Long> {

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
