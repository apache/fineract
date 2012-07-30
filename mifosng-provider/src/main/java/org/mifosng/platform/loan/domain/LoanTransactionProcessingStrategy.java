package org.mifosng.platform.loan.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.mifosng.platform.api.data.TransactionProcessingStrategyData;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

/**
 *
 */
@Entity
@Table(name = "ref_loan_transaction_processing_strategy")
public class LoanTransactionProcessingStrategy extends AbstractAuditableCustom<AppUser, Long> {

	@Column(name = "code", unique=true)
	private String code;
	
	@Column(name = "name")
	private String name;
	
	protected LoanTransactionProcessingStrategy() {
		//
	}

	public String code() {
		return this.code;
	}

	public TransactionProcessingStrategyData toData() {
		return new TransactionProcessingStrategyData(this.getId(), this.code, this.name);
	}
}