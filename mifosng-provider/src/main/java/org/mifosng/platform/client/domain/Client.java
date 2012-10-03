package org.mifosng.platform.client.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "m_client")
public class Client extends AbstractAuditableCustom<AppUser, Long> {

	@ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office       office;

	@Column(name = "firstname", length=50)
    private String       firstName;
    
	@Column(name = "lastname", length=50)
    private String       lastName;
    
    @SuppressWarnings("unused")
   	@Column(name = "display_name", length=50)
    private String       displayName;

    @SuppressWarnings("unused")
	@Column(name = "joining_date")
    @Temporal(TemporalType.DATE)
    private Date         joiningDate;

    @Column(name = "external_id", length=100, unique=true)
    private String externalId;

    @Column(name = "is_deleted", nullable=false)
	private boolean deleted = false;

	public static Client newClient(Office clientOffice, String firstname, String lastname, LocalDate joiningDate, String externalId) {
		return new Client(clientOffice, firstname, lastname, joiningDate, externalId);
	}

    protected Client() {
        this.office = null;
        this.joiningDate = null;
        this.firstName = null;
        this.lastName = null;
        this.externalId = null;
    }

    private Client(final Office office, final String firstName, final String lastName, final LocalDate openingDate, final String externalId) {
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
        //populate display name
        if(this.firstName!=null){
    		this.displayName = this.firstName + " " + this.lastName;
        }else{
        	this.displayName = this.lastName;
        }
    }

	public boolean identifiedBy(final String identifier) {
		return identifier.equalsIgnoreCase(this.externalId);
	}
	
	public boolean identifiedBy(Long clientId) {
		return getId().equals(clientId);
	}

	public void update(ClientCommand command) {
		this.joiningDate = command.getJoiningDate().toDate();
		this.firstName = command.getFirstname();
		this.lastName = command.getLastname();
		
		if (StringUtils.isNotBlank(command.getClientOrBusinessName())) {
			this.firstName = null;
			this.lastName = command.getClientOrBusinessName().trim();
			this.displayName = this.lastName;
		}else{
			deriveDisplayName();
		}
		
		if (StringUtils.isNotBlank(command.getExternalId())) {
            this.externalId = command.getExternalId().trim();
        } else {
            this.externalId = null;
        }
	}

	public void update(final Office clientOffice, final ClientCommand command) {
		if (command.isOfficeChanged()) {
			this.office = clientOffice;
		}
		if (command.isJoiningDateChanged()) {
			this.joiningDate = command.getJoiningDate().toDate();
		}
		if (command.isFirstnameChanged()) {
			this.firstName = command.getFirstname();
		}
		if (command.isLastnameChanged()) {
			this.lastName = command.getLastname();
		}
		
		if (command.isClientOrBusinessNameChanged() && StringUtils.isNotBlank(command.getClientOrBusinessName())) {
			this.lastName = command.getClientOrBusinessName();
			this.firstName = null;
			this.displayName = this.lastName;
		}else if(command.isFirstnameChanged() || command.isLastnameChanged()) {
			//do not want to enter this condition for business names
			deriveDisplayName();
		}
		
		if (command.isExternalIdChanged()) {
			 this.externalId = StringUtils.defaultIfEmpty(command.getExternalId().trim(), null);
		}
	}

	private void deriveDisplayName() {
		this.displayName = this.firstName + " " + this.lastName;
	}

    /**
	 * Delete is a <i>soft delete</i>. Updates flag on client so it wont appear in query/report results.
	 * 
	 * Any fields with unique constraints and prepended with id of record.
	 */
	public void delete() {
		this.deleted = true;
		this.externalId = this.getId() + "_" + this.externalId;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isOfficeIdentifiedBy(final Long officeId) {
		return this.office.identifiedBy(officeId);
	}
}