package org.mifosng.platform.client.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.organisation.domain.Organisation;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "portfolio_client", uniqueConstraints = @UniqueConstraint(columnNames = {"org_id", "external_id" }))
public class Client extends AbstractAuditableCustom<AppUser, Long> {

    @SuppressWarnings("unused")
	@ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private final Organisation organisation;

    @SuppressWarnings("unused")
	@ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office       office;

    @SuppressWarnings("unused")
	@Column(name = "firstname", length=50)
    private String       firstName;
    
    @SuppressWarnings("unused")
	@Column(name = "lastname", length=50)
    private String       lastName;

    @SuppressWarnings("unused")
	@Column(name = "joining_date")
    @Temporal(TemporalType.DATE)
    private Date         joiningDate;

    @Column(name = "external_id", length=100)
    private String externalId;

	public static Client newClient(Organisation organisation,
			Office clientOffice, String firstname, String lastname,
			LocalDate joiningDate, String externalId) {
		return new Client(organisation, clientOffice, firstname, lastname,
				joiningDate, externalId);
	}

    public Client() {
        this.organisation = null;
        this.office = null;
        this.joiningDate = null;
        this.firstName = null;
        this.lastName = null;
        this.externalId = null;
    }

    public Client(final Organisation organisation, final Office office, final String firstName,
            final String lastName, final LocalDate openingDate, final String externalId) {
        this.organisation = organisation;
        this.office = office;
        if (StringUtils.isNotBlank(externalId)) {
            this.externalId = externalId.trim();
        } else {
            this.externalId = null;
        }
        this.joiningDate = openingDate.toDateMidnight().toDate();
        if (StringUtils.isNotBlank(firstName)) {
        	this.firstName = firstName.trim();
        } else {
        	this.firstName = null;
        }
        this.lastName = lastName.trim();
    }

	public boolean identifiedBy(final String identifier) {
		return identifier.equalsIgnoreCase(this.externalId);
	}
	
	public boolean identifiedBy(Long clientId) {
		return getId().equals(clientId);
	}

	public void update(ClientCommand command) {
		this.joiningDate = command.getJoiningLocalDate().toDate();
		this.firstName = command.getFirstname();
		this.lastName = command.getLastname();
		
		if (StringUtils.isNotBlank(command.getClientOrBusinessName())) {
			this.firstName = null;
			this.lastName = command.getClientOrBusinessName().trim();
		}
		
		if (StringUtils.isNotBlank(command.getExternalId())) {
            this.externalId = command.getExternalId().trim();
        } else {
            this.externalId = null;
        }
	}

	public void update(Office clientOffice, String firstname, String lastname, String externalId, LocalDate joiningDate) {
		if (clientOffice != null) {
			this.office = clientOffice;
		}
		if (joiningDate != null) {
			this.joiningDate = joiningDate.toDate();
		}
		if (StringUtils.isNotBlank(firstname)) {
			this.firstName = firstname;
		}
		if (StringUtils.isNotBlank(lastname)) {
			this.lastName = lastname;
		}
		if (externalId != null) {
			 this.externalId = externalId.trim();
		}
	}
}