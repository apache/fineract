/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.mifosplatform.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "ref_loan_transaction_processing_strategy")
public class LoanTransactionProcessingStrategy extends AbstractPersistable<Long> {

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "name")
    private String name;

    protected LoanTransactionProcessingStrategy() {
        //
    }

    public TransactionProcessingStrategyData toData() {
        return new TransactionProcessingStrategyData(getId(), this.code, this.name);
    }

    public boolean isStandardMifosStrategy() {
        return "mifos-standard-strategy".equalsIgnoreCase(this.code);
    }

    public boolean isHeavensfamilyStrategy() {
        return "heavensfamily-strategy".equalsIgnoreCase(this.code);
    }

    public boolean isEarlyPaymentStrategy() {
        return "early-repayment-strategy".equalsIgnoreCase(this.code);
    }

    public boolean isCreocoreStrategy() {
        return "creocore-strategy".equalsIgnoreCase(this.code);
    }

    public boolean isIndianRBIStrategy() {
        return "rbi-india-strategy".equalsIgnoreCase(this.code);
    }

    public boolean isPrincipalInterestPenaltiesFeesOrderStrategy() {
        return "principal-interest-penalties-fees-order-strategy".equalsIgnoreCase(this.code);
    }

    public boolean isInterestPrincipalPenaltiesFeesOrderStrategy() {
        return "interest-principal-penalties-fees-order-strategy".equalsIgnoreCase(this.code);
    }
}