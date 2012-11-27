package org.mifosng.platform.staff.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.StaffCommand;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosplatform.infrastructure.user.domain.AppUser;

@Entity
@Table(name = "m_staff", uniqueConstraints = { @UniqueConstraint(columnNames = { "display_name" }, name = "display_name") })
public class Staff extends AbstractAuditableCustom<AppUser, Long> {

	@Column(name = "firstname", length = 50)
	private String firstname;

	@Column(name = "lastname", length = 50)
	private String lastname;

	@SuppressWarnings("unused")
	@Column(name = "display_name", length = 100)
	private String displayName;

	// Office to which this employee belongs
	@SuppressWarnings("unused")
	@ManyToOne
	@JoinColumn(name = "office_id", nullable = false)
	private Office office;

	// Flag determines if employee is a loan Officer
	@Column(name = "is_loan_officer ", nullable = false)
	private Boolean loanOfficerFlag;

	public static Staff createNew(Office staffOffice, final String firstname,
			final String lastname, boolean isLoanOfficer) {
		return new Staff(staffOffice, firstname, lastname, isLoanOfficer);
	}

	protected Staff() {
		//
	}

	private Staff(final Office staffOffice, final String firstname,
			final String lastname, final boolean isLoanOfficer) {
		this.office = staffOffice;
		this.firstname = StringUtils.defaultIfEmpty(firstname, null);
		this.lastname = StringUtils.defaultIfEmpty(lastname, null);
		this.loanOfficerFlag = isLoanOfficer;
		deriveDisplayName(firstname);
	}

	public void update(final StaffCommand command, final Office staffOffice) {
		if (command.isOfficeChanged()) {
			this.office = staffOffice;
		}

		if (command.isFirstNameChanged()) {
			this.firstname = StringUtils.defaultIfEmpty(command.getFirstName(),
					null);
		}

		if (command.isLastNameChanged()) {
			this.lastname = StringUtils.defaultIfEmpty(command.getLastName(),
					null);
		}

		if (command.isLastNameChanged() || command.isFirstNameChanged()) {
			deriveDisplayName(firstname);
		}

		if (command.isLoanOfficerFlagChanged()) {
			this.loanOfficerFlag = command.isLoanOfficerFlag();
		}
	}

	private void deriveDisplayName(final String firstname) {
		if (!StringUtils.isBlank(firstname)) {
			this.displayName = this.lastname + ", " + this.firstname;
		} else {
			this.displayName = this.lastname;
		}
	}

	public Boolean getLoanOfficerFlag() {
		return loanOfficerFlag;
	}

	public boolean identifiedBy(final Staff staff) {
		return getId().equals(staff.getId());
	}
}