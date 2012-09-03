package org.mifosng.platform.staff.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.StaffCommand;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "m_staff", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "display_name" }, name = "display_name")})
public class Staff extends AbstractAuditableCustom<AppUser, Long> {

	@Column(name = "firstname", length = 50)
	private String firstname;

	@Column(name = "lastname", length = 50)
	private String lastname;

	@SuppressWarnings("unused")
	@Column(name = "display_name", length = 100)
	private String displayName;

	public static Staff createNew(final String firstname, final String lastname) {
		return new Staff(firstname, lastname);
	}

	protected Staff() {
		//
	}

	private Staff(final String firstname, final String lastname) {
		this.firstname = StringUtils.defaultIfEmpty(firstname, null);
		this.lastname = StringUtils.defaultIfEmpty(lastname, null);
		deriveDisplayName(firstname);
	}

	public void update(final StaffCommand command) {

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
	}

	private void deriveDisplayName(final String firstname) {
		if (!StringUtils.isBlank(firstname)) {
			this.displayName = this.lastname + ", " + this.firstname;
		} else {
			this.displayName = this.lastname;
		}
	}

}