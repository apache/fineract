package org.mifosng.platform.saving.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.deposit.domain.DepositProduct;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "portfolio_deposit_account", uniqueConstraints = @UniqueConstraint(name="deposit_acc_external_id", columnNames = { "external_id" }))
public class DepositAccount extends AbstractAuditableCustom<AppUser, Long> {
	
	@ManyToOne
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

	@ManyToOne
	@JoinColumn(name = "product_id")
	private final DepositProduct product;
	
	@Column(name = "external_id")
	private String externalId;
	
	@Embedded
	private MonetaryCurrency currency;
	
	@Column(name = "deposit_amount", scale = 6, precision = 19, nullable = false)
	private BigDecimal depositAmount;
	
	@Column(name = "maturity_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal interestRate;
	
	@Column(name = "tenure_months", nullable = false)
	private Integer tenureInMonths;
	
	@Column(name = "pre_closure_interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal preClosureInterestRate;
	
	@Column(name = "can_renew", nullable = false)
	private boolean renewalAllowed;
	
	@Column(name = "can_pre_close", nullable = false)
	private boolean preClosureAllowed;
	
    @Column(name = "is_deleted", nullable=false)
	private boolean deleted = false;
	
	protected DepositAccount() {
		this.product = null;
	}

	public DepositAccount(
			final Client client, final DepositProduct product,
			final String externalId, final Money deposit, final BigDecimal interestRate, final Integer termInMonths) {
		this.client = client;
		this.product = product;
		this.externalId = externalId;
		this.currency = deposit.getCurrency();
		this.depositAmount = deposit.getAmount();
		this.interestRate = interestRate;
		product.validateInterestRateInRange(interestRate);
		this.tenureInMonths = termInMonths;
		this.preClosureInterestRate = BigDecimal.ZERO;
	}
	
	public boolean isDeleted() {
		return deleted;
	}
	
	public void delete() {
		this.deleted = true;
	}	

	public void update(final DepositAccountCommand command) {

	}
}