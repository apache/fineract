package org.mifosng.platform.fund.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.mifosng.platform.api.commands.FundCommand;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.organisation.domain.Organisation;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "org_fund")
public class Fund extends AbstractAuditableCustom<AppUser, Long> {

	@ManyToOne
	@JoinColumn(name = "org_id", nullable = false)
	private final Organisation organisation;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "external_id", length=100)
	private String externalId;

	@Temporal(TemporalType.DATE)
	@Column(name = "activatedon_date")
	private Date activatedOnDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "deactivatedon_date")
	private Date deactivatedOnDate;

	@Column(name = "initial_balance", scale = 6, precision = 19)
	private BigDecimal initialBalance;

	public static Fund createNew(final Organisation organisation, final String fundName) {
		return new Fund(organisation, fundName);
	}
	
	protected Fund() {
		this.organisation = null;
	}

	private Fund(final Organisation organisation, final String fundName) {
		this.organisation = organisation;
		this.name = fundName;
		this.initialBalance = BigDecimal.ZERO;
	}

	public void update(FundCommand command) {
		if (command.getName() != null) {
			this.name = command.getName();
		}
	}
}