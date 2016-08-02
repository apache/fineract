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

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.data.jpa.domain.AbstractPersistable;

@SuppressWarnings("serial")
@Entity
@Table(name = "m_group_loan_individual_monitoring")
public class GroupLoanIndividualMonitoring extends AbstractPersistable<Long>{
	
	@ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @Column(name = "proposed_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal proposedAmount;
    
    @Column(name = "approved_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal approvedAmount;
    
    @Column(name = "disbursed_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal disbursedAmount;
    
    @ManyToOne
    @JoinColumn(name = "loanpurpose_cv_id", nullable = true)
    private CodeValue loanPurpose;
    
    @Column(name = "is_selected", nullable = true)
    private Boolean isSelected;

	public GroupLoanIndividualMonitoring() {
		super();
	}

	public GroupLoanIndividualMonitoring(final Loan loan, final Client client) {
		this.loan = loan;
		this.client = client;
	}

	public GroupLoanIndividualMonitoring(final Loan loan, final Client client,
			final BigDecimal proposedAmount, final BigDecimal approvedAmount,
			final BigDecimal disbursedAmount, final CodeValue loanPurpose, final Boolean isSelected) {
		this.loan = loan;
		this.client = client;
		this.proposedAmount = proposedAmount;
		this.approvedAmount = approvedAmount;
		this.disbursedAmount = disbursedAmount;
		this.loanPurpose = loanPurpose;
		this.isSelected = isSelected;
	}
    
    public static GroupLoanIndividualMonitoring createDefaultInstance(final Loan loan, final Client client){
		return new GroupLoanIndividualMonitoring(loan, client);    	
    }
    
    public static GroupLoanIndividualMonitoring createInstance(final Loan loan, final Client client,
			final BigDecimal proposedAmount, final BigDecimal approvedAmount,
			final BigDecimal disbursedAmount, final CodeValue loanPurpose, final Boolean isSelected){
		return new GroupLoanIndividualMonitoring(loan, client, proposedAmount, approvedAmount, disbursedAmount, loanPurpose, isSelected);    	
    }

	public Loan getLoan() {
		return this.loan;
	}

	public void setLoan(Loan loan) {
		this.loan = loan;
	}

	public Client getClient() {
		return this.client;
	}

	public BigDecimal getProposedAmount() {
		return this.proposedAmount;
	}

	public void setProposedAmount(BigDecimal proposedAmount) {
		this.proposedAmount = proposedAmount;
	}

	public BigDecimal getApprovedAmount() {
		return this.approvedAmount;
	}

	public void setApprovedAmount(BigDecimal approvedAmount) {
		this.approvedAmount = approvedAmount;
	}

	public BigDecimal getDisbursedAmount() {
		return this.disbursedAmount;
	}

	public void setDisbursedAmount(BigDecimal disbursedAmount) {
		this.disbursedAmount = disbursedAmount;
	}

	public CodeValue getLoanPurpose() {
		return this.loanPurpose;
	} 

	public void setLoanPurpose(CodeValue loanPurpose) {
		this.loanPurpose = loanPurpose;
	}

	public Boolean getIsSelected() {
		return isSelected;
	}

	public void setIsSelected(Boolean isSelected) {
		this.isSelected = isSelected;
	}	
    
}
