package org.mifosng.platform.organisation.domain;

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
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "portfolio_office_monetary_transfers")
public class OfficeMonetaryTransfer extends AbstractAuditableCustom<AppUser, Long> {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "from_office_id")
	private Office from;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "to_office_id")
	private Office to;

	@Column(name = "transaction_date", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date transactionDate;

	@Embedded
	private MonetaryCurrency currency;

	@Column(name = "transaction_amount", scale = 6, precision = 19, nullable = false)
	private BigDecimal transactionAmount;

	protected OfficeMonetaryTransfer() {
		this.transactionDate = null;
	}

	public static OfficeMonetaryTransfer create(Office fromOffice, Office toOffice, LocalDate transactionLocalDate, Money amount) {

		Date transactionDate = null;
		if (transactionLocalDate != null) {
			transactionDate = transactionLocalDate.toDate();
		}

		return new OfficeMonetaryTransfer(fromOffice, toOffice, transactionDate, amount);
	}

	private OfficeMonetaryTransfer(final Office fromOffice,final Office toOffice, final Date transactionDate, final Money amount) {
		this.from = fromOffice;
		this.to = toOffice;
		this.transactionDate = transactionDate;
		this.currency = amount.getCurrency();
		this.transactionAmount = amount.getAmount();
	}
}