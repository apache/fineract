/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.data;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;

/**
 * Immutable data object representing client data.
 */
final public class ClientData implements Comparable<ClientData> {

    private final Long id;
    private final String accountNo;
    private final String externalId;

    private final EnumOptionData status;
    private final LocalDate activationDate;

    private final String firstname;
    private final String middlename;
    private final String lastname;
    private final String fullname;
    private final String displayName;

    private final Long officeId;
    private final String officeName;

    private final String imageKey;
    @SuppressWarnings("unused")
    private final Boolean imagePresent;

    private final Boolean clientPendingApprovalAllowed;

    // associations
    private final Collection<GroupGeneralData> groups;

    // template
    private final Collection<OfficeData> officeOptions;

    public static ClientData template(final Long officeId, final LocalDate joinedDate, final Boolean clientPendingApprovalAllowed,
            final Collection<OfficeData> allowedOffices) {
        return new ClientData(null, null, officeId, null, null, null, null, null, null, null, null, joinedDate, null,
                clientPendingApprovalAllowed, allowedOffices, null);
    }

    public static ClientData templateOnTop(final ClientData clientData, final List<OfficeData> allowedOffices) {

        return new ClientData(clientData.accountNo, clientData.status, clientData.officeId, clientData.officeName, clientData.id,
                clientData.firstname, clientData.middlename, clientData.lastname, clientData.fullname, clientData.displayName,
                clientData.externalId, clientData.activationDate, clientData.imageKey, clientData.clientPendingApprovalAllowed,
                allowedOffices, clientData.groups);
    }

    public static ClientData setParentGroups(final ClientData clientData, final Collection<GroupGeneralData> parentGroups) {
        return new ClientData(clientData.accountNo, clientData.status, clientData.officeId, clientData.officeName, clientData.id,
                clientData.firstname, clientData.middlename, clientData.lastname, clientData.fullname, clientData.displayName,
                clientData.externalId, clientData.activationDate, clientData.imageKey, clientData.clientPendingApprovalAllowed,
                clientData.officeOptions, parentGroups);
    }

    public static ClientData clientIdentifier(final Long id, final String accountNo, final EnumOptionData status, final String firstname,
            final String middlename, final String lastname, final String fullname, final String displayName, final Long officeId,
            final String officeName) {

        return new ClientData(accountNo, status, officeId, officeName, id, firstname, middlename, lastname, fullname, displayName, null,
                null, null, null, null, null);
    }

    public static ClientData lookup(final Long id, final String displayName, final Long officeId, final String officeName) {
        return new ClientData(null, null, officeId, officeName, id, null, null, null, null, displayName, null, null, null, null, null, null);
    }

    public ClientData(final String accountNo, final EnumOptionData status, final Long officeId, final String officeName, final Long id,
            final String firstname, final String middlename, final String lastname, final String fullname, final String displayName,
            final String externalId, final LocalDate activationDate, final String imageKey, final Boolean clientPendingApprovalAllowed,
            final Collection<OfficeData> allowedOffices, final Collection<GroupGeneralData> groups) {
        this.accountNo = accountNo;
        this.status = status;
        this.officeId = officeId;
        this.officeName = officeName;
        this.id = id;
        this.firstname = StringUtils.defaultIfEmpty(firstname, null);
        this.middlename = StringUtils.defaultIfEmpty(middlename, null);
        this.lastname = StringUtils.defaultIfEmpty(lastname, null);
        this.fullname = StringUtils.defaultIfEmpty(fullname, null);
        this.displayName = StringUtils.defaultIfEmpty(displayName, null);
        this.externalId = StringUtils.defaultIfEmpty(externalId, null);
        this.activationDate = activationDate;
        this.imageKey = imageKey;
        if (imageKey != null) {
            this.imagePresent = Boolean.TRUE;
        } else {
            this.imagePresent = null;
        }

        this.clientPendingApprovalAllowed = clientPendingApprovalAllowed;

        // associations
        this.groups = groups;

        // template
        this.officeOptions = allowedOffices;
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

    public String imageKey() {
        return this.imageKey;
    }

    public boolean imageKeyDoesNotExist() {
        return !imageKeyExists();
    }

    private boolean imageKeyExists() {
        return StringUtils.isNotBlank(this.imageKey);
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
        ClientData rhs = (ClientData) obj;
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

    // TODO - kw - look into removing usage of the getters below
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