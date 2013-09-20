/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.data;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;

/**
 * Immutable data object representing client data.
 */
final public class ClientData implements Comparable<ClientData> {

    private final Long id;
    private final String accountNo;
    private final String externalId;

    private final EnumOptionData status;
    @SuppressWarnings("unused")
    private final Boolean active;
    private final LocalDate activationDate;

    private final String firstname;
    private final String middlename;
    private final String lastname;
    private final String fullname;
    private final String displayName;

    private final Long officeId;
    private final String officeName;
    private final Long transferToOfficeId;
    private final String transferToOfficeName;

    private final Long imageId;
    private final Boolean imagePresent;
    private final Long staffId;
    private final String staffName;

    // associations
    private final Collection<GroupGeneralData> groups;

    // template
    private final Collection<OfficeData> officeOptions;
    private final Collection<StaffData> staffOptions;
    @SuppressWarnings("unused")
    private final Collection<CodeValueData> closureReasons;

    public static ClientData template(final Long officeId, final LocalDate joinedDate, final Collection<OfficeData> officeOptions,
            final Collection<StaffData> staffOptions, final Collection<CodeValueData> closureReasons) {
        return new ClientData(null, null, officeId, null, null, null, null, null, null, null, null, null, null, joinedDate, null, null,
                null, officeOptions, null, staffOptions, closureReasons);
    }

    public static ClientData templateOnTop(final ClientData clientData, final ClientData templateData) {

        return new ClientData(clientData.accountNo, clientData.status, clientData.officeId, clientData.officeName,
                clientData.transferToOfficeId, clientData.transferToOfficeName, clientData.id, clientData.firstname, clientData.middlename,
                clientData.lastname, clientData.fullname, clientData.displayName, clientData.externalId, clientData.activationDate,
                clientData.imageId, clientData.staffId, clientData.staffName, templateData.officeOptions, clientData.groups,
                templateData.staffOptions, null);
    }

    public static ClientData setParentGroups(final ClientData clientData, final Collection<GroupGeneralData> parentGroups) {
        return new ClientData(clientData.accountNo, clientData.status, clientData.officeId, clientData.officeName,
                clientData.transferToOfficeId, clientData.transferToOfficeName, clientData.id, clientData.firstname, clientData.middlename,
                clientData.lastname, clientData.fullname, clientData.displayName, clientData.externalId, clientData.activationDate,
                clientData.imageId, clientData.staffId, clientData.staffName, clientData.officeOptions, parentGroups,
                clientData.staffOptions, null);
    }

    public static ClientData clientIdentifier(final Long id, final String accountNo, final EnumOptionData status, final String firstname,
            final String middlename, final String lastname, final String fullname, final String displayName, final Long officeId,
            final String officeName) {

        return new ClientData(accountNo, status, officeId, officeName, null, null, id, firstname, middlename, lastname, fullname,
                displayName, null, null, null, null, null, null, null, null, null);
    }

    public static ClientData lookup(final Long id, final String displayName, final Long officeId, final String officeName) {
        return new ClientData(null, null, officeId, officeName, null, null, id, null, null, null, null, displayName, null, null, null,
                null, null, null, null, null, null);
    }

    public static ClientData instance(final String accountNo, final EnumOptionData status, final Long officeId, final String officeName,
            final Long transferToOfficeId, final String transferToOfficeName, final Long id, final String firstname,
            final String middlename, final String lastname, final String fullname, final String displayName, final String externalId,
            final LocalDate activationDate, final Long imageId, final Long staffId, final String staffName) {
        return new ClientData(accountNo, status, officeId, officeName, transferToOfficeId, transferToOfficeName, id, firstname, middlename,
                lastname, fullname, displayName, externalId, activationDate, imageId, staffId, staffName, null, null, null, null);
    }

    private ClientData(final String accountNo, final EnumOptionData status, final Long officeId, final String officeName,
            final Long transferToOfficeId, final String transferToOfficeName, final Long id, final String firstname,
            final String middlename, final String lastname, final String fullname, final String displayName, final String externalId,
            final LocalDate activationDate, final Long imageId, final Long staffId, final String staffName,
            final Collection<OfficeData> allowedOffices, final Collection<GroupGeneralData> groups,
            final Collection<StaffData> staffOptions, final Collection<CodeValueData> closureReasons) {
        this.accountNo = accountNo;
        this.status = status;
        if (status != null) {
            this.active = status.getId().equals(300L);
        } else {
            this.active = null;
        }
        this.officeId = officeId;
        this.officeName = officeName;
        this.transferToOfficeId = transferToOfficeId;
        this.transferToOfficeName = transferToOfficeName;
        this.id = id;
        this.firstname = StringUtils.defaultIfEmpty(firstname, null);
        this.middlename = StringUtils.defaultIfEmpty(middlename, null);
        this.lastname = StringUtils.defaultIfEmpty(lastname, null);
        this.fullname = StringUtils.defaultIfEmpty(fullname, null);
        this.displayName = StringUtils.defaultIfEmpty(displayName, null);
        this.externalId = StringUtils.defaultIfEmpty(externalId, null);
        this.activationDate = activationDate;
        this.imageId = imageId;
        if (imageId != null) {
            this.imagePresent = Boolean.TRUE;
        } else {
            this.imagePresent = null;
        }
        this.staffId = staffId;
        this.staffName = staffName;

        // associations
        this.groups = groups;

        // template
        this.officeOptions = allowedOffices;
        this.staffOptions = staffOptions;
        this.closureReasons = closureReasons;
    }

    public Long id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    public Long officeId() {
        return this.officeId;
    }

    public String officeName() {
        return this.officeName;
    }

    public Long getImageId() {
        return this.imageId;
    }

    public Boolean getImagePresent() {
        return this.imagePresent;
    }

    @Override
    public int compareTo(final ClientData obj) {
        if (obj == null) { return -1; }
        return new CompareToBuilder() //
                .append(this.id, obj.id) //
                .append(this.displayName, obj.displayName) //
                .toComparison();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        final ClientData rhs = (ClientData) obj;
        return new EqualsBuilder() //
                .append(this.id, rhs.id) //
                .append(this.displayName, rhs.displayName) //
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37) //
                .append(this.id) //
                .append(this.displayName) //
                .toHashCode();
    }

    public String getExternalId() {
        return this.externalId;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public LocalDate getActivationDate() {
        return this.activationDate;
    }
}