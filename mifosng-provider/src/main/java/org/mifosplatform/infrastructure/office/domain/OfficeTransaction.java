package org.mifosplatform.infrastructure.office.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.configuration.domain.MonetaryCurrency;
import org.mifosplatform.infrastructure.configuration.domain.Money;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.infrastructure.user.domain.AppUser;

@Entity
@Table(name = "m_office_transaction")
public class OfficeTransaction extends AbstractAuditableCustom<AppUser, Long> {

	@SuppressWarnings("unused")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "from_office_id")
	private Office from;

	@SuppressWarnings("unused")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "to_office_id")
	private Office to;

	@SuppressWarnings("unused")
	@Column(name = "transaction_date", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date transactionDate;

	@SuppressWarnings("unused")
	@Embedded
	private MonetaryCurrency currency;

	@SuppressWarnings("unused")
	@Column(name = "transaction_amount", scale = 6, precision = 19, nullable = false)
	private BigDecimal transactionAmount;
	
	@SuppressWarnings("unused")
	@Column(name = "description", nullable=true, length=100)
	private String description;

	protected OfficeTransaction() {
		this.transactionDate = null;
	}

	public static OfficeTransaction create(Office fromOffice, Office toOffice, LocalDate transactionLocalDate, Money amount, String description) {

		Date transactionDate = null;
		if (transactionLocalDate != null) {
			transactionDate = transactionLocalDate.toDate();
		}

		return new OfficeTransaction(fromOffice, toOffice, transactionDate, amount, description);
	}

	private OfficeTransaction(final Office fromOffice,final Office toOffice, final Date transactionDate, final Money amount, String description) {
		this.from = fromOffice;
		this.to = toOffice;
		this.transactionDate = transactionDate;
		this.currency = amount.getCurrency();
		this.transactionAmount = amount.getAmount();
		this.description = description;
	}
}