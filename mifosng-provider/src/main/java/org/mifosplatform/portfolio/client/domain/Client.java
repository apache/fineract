package org.mifosplatform.portfolio.client.domain;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.security.service.RandomPasswordGenerator;
import org.mifosplatform.organisation.office.domain.Office;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_client")
public class Client extends AbstractPersistable<Long> {

    @SuppressWarnings("unused")
    @Column(name = "account_no", length=40, unique=true, nullable=false)
    private String accountNumber;
    
    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "firstname", length = 50)
    private String firstName;

    @Column(name = "lastname", length = 50)
    private String lastName;

    @Column(name = "display_name", length = 50)
    private String displayName;

    @Column(name = "joined_date")
    @Temporal(TemporalType.DATE)
    private Date joinedDate;

    @Column(name = "external_id", length = 100, unique = true)
    private String externalId;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "image_key", length = 500)
    private String imageKey;

    public static Client fromJson(final Office clientOffice, final JsonCommand command) {
        
        final String joiningDateParamName = "joinedDate";
        final LocalDate joiningDate = command.localDateValueOfParameterNamed(joiningDateParamName);
        
        final String firstnameParamName = "firstname";
        String firstname = command.stringValueOfParameterNamed(firstnameParamName);
        
        final String lastnameParamName = "lastname";
        String lastname = command.stringValueOfParameterNamed(lastnameParamName);
        
        final String clientOrBusinessNameParamName = "clientOrBusinessName";
        final String clientOrBusinessName = command.stringValueOfParameterNamed(clientOrBusinessNameParamName);
        if (StringUtils.isNotBlank(clientOrBusinessName)) {
            lastname = clientOrBusinessName;
            firstname = null;
        }
        
        final String externalIdParamName = "externalId";
        final String externalId = command.stringValueOfParameterNamed(externalIdParamName);
        
        return new Client(clientOffice, firstname, lastname, joiningDate, externalId);
    }
    
    protected Client() {
        //
    }

    private Client(final Office office, final String firstName, final String lastName, final LocalDate openingDate, final String externalId) {
        this.accountNumber = new RandomPasswordGenerator(19).generate();
        this.office = office;
        if (StringUtils.isNotBlank(externalId)) {
            this.externalId = externalId.trim();
        } else {
            this.externalId = null;
        }
        this.joinedDate = openingDate.toDateMidnight().toDate();
        if (StringUtils.isNotBlank(firstName)) {
            this.firstName = firstName.trim();
        } else {
            this.firstName = null;
        }
        this.lastName = lastName.trim();
        // populate display name
        if (this.firstName != null) {
            this.displayName = this.firstName + " " + this.lastName;
        } else {
            this.displayName = this.lastName;
        }
    }

    public boolean identifiedBy(final String identifier) {
        return identifier.equalsIgnoreCase(this.externalId);
    }

    public boolean identifiedBy(Long clientId) {
        return getId().equals(clientId);
    }
    
    public void changeOffice(final Office newOffice) {
        this.office = newOffice;
    }
    
    public void updateAccountIdentifier(final String accountIdentifier) {
        this.accountNumber = accountIdentifier;
    }

    public Map<String, Object> update(final JsonCommand command) {
        
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(7);
        
        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();
        
        final String officeIdParamName = "officeId";
        if (command.isChangeInLongParameterNamed(officeIdParamName, this.office.getId())) {
            final Long newValue = command.longValueOfParameterNamed(officeIdParamName);
            actualChanges.put(officeIdParamName, newValue);
        }
        
        final String joiningDateParamName = "joinedDate";
        if (command.isChangeInLocalDateParameterNamed(joiningDateParamName, getJoiningLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(joiningDateParamName);
            actualChanges.put(joiningDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);
            
            final LocalDate newValue = command.localDateValueOfParameterNamed(joiningDateParamName);
            this.joinedDate = newValue.toDate();
        }

        final String clientOrBusinessNameParamName = "clientOrBusinessName";
        final String lastnameParamName = "lastname";
        if (command.isChangeInStringParameterNamed(lastnameParamName, getLastnameIfNotClientOrBusinessName())) {
            final String newValue = command.stringValueOfParameterNamed(lastnameParamName);
            actualChanges.put(lastnameParamName, newValue);
            if (StringUtils.isNotBlank(getClientOrBusinessName())) {
                actualChanges.put(clientOrBusinessNameParamName, "");
            }
            this.lastName = newValue;
        }
        
        final String firstnameParamName = "firstname";
        if (command.isChangeInStringParameterNamed(firstnameParamName, this.firstName)) {
            final String newValue = command.stringValueOfParameterNamed(firstnameParamName);
            actualChanges.put(firstnameParamName, newValue);
            this.firstName = newValue;
        }
        
        
        if (command.isChangeInStringParameterNamed(clientOrBusinessNameParamName, getClientOrBusinessName())) {
            final String newValue = command.stringValueOfParameterNamed(clientOrBusinessNameParamName);
            actualChanges.put(clientOrBusinessNameParamName, newValue);
            if (StringUtils.isNotBlank(newValue)) {
                this.lastName = newValue;
                this.firstName = null;
                this.displayName = this.lastName;
            } else {
                deriveDisplayName();
            }
        }
        
        final String externalIdParamName = "externalId";
        if (command.isChangeInStringParameterNamed(externalIdParamName, this.externalId)) {
            final String newValue = command.stringValueOfParameterNamed(externalIdParamName);
            actualChanges.put(externalIdParamName, newValue);
            this.externalId = StringUtils.defaultIfEmpty(newValue, null);
        }
        
        return actualChanges;
    }

    private void deriveDisplayName() {
        this.displayName = this.firstName + " " + this.lastName;
    }
    
    private LocalDate getJoiningLocalDate() {
        LocalDate joiningLocalDate = null;
        if (this.joinedDate != null) {
            joiningLocalDate = LocalDate.fromDateFields(this.joinedDate);
        }
        return joiningLocalDate;
    }

    /*
     * 
     */
    private String getLastnameIfNotClientOrBusinessName() {
        String lastname = null;
        if (StringUtils.isNotBlank(this.firstName) && StringUtils.isNotBlank(this.lastName)) {
            lastname = this.lastName;
        }
        return lastname;
    }

    
    private String getClientOrBusinessName() {
        String clientOrBusinessName = null;
        if (StringUtils.isBlank(this.firstName) && StringUtils.isNotBlank(this.lastName)) {
            clientOrBusinessName = this.lastName;
        }
        return clientOrBusinessName;
    }

    /**
     * Delete is a <i>soft delete</i>. Updates flag on client so it wont appear
     * in query/report results.
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

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Date getJoiningDate() {
        return joinedDate;
    }

    public void setJoiningDate(Date joiningDate) {
        this.joinedDate = joiningDate;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }
}