/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.data;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Immutable data object representing client data.
 */
final public class ClientData implements Comparable<ClientData> {

    private final static Logger logger = LoggerFactory.getLogger(ClientData.class);


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

//    private String imageData;
    private ImageData imageData;
    @SuppressWarnings("unused")
    private final Boolean imagePresent;

    // associations
    private final Collection<GroupGeneralData> groups;

    // template
    private final Collection<OfficeData> officeOptions;

    public static ClientData template(final Long officeId, final LocalDate joinedDate, final Collection<OfficeData> officeOptions) {
        return new ClientData(null, null, officeId, null, null, null, null, null, null, null, null, joinedDate, null, officeOptions, null);
    }

    public static ClientData templateOnTop(final ClientData clientData, final List<OfficeData> allowedOffices) {

        return new ClientData(clientData.accountNo, clientData.status, clientData.officeId, clientData.officeName, clientData.id,
                clientData.firstname, clientData.middlename, clientData.lastname, clientData.fullname, clientData.displayName,
                clientData.externalId, clientData.activationDate, clientData.imageData, allowedOffices, clientData.groups);
    }

    public static ClientData setParentGroups(final ClientData clientData, final Collection<GroupGeneralData> parentGroups) {
        return new ClientData(clientData.accountNo, clientData.status, clientData.officeId, clientData.officeName, clientData.id,
                clientData.firstname, clientData.middlename, clientData.lastname, clientData.fullname, clientData.displayName,
                clientData.externalId, clientData.activationDate, clientData.imageData, clientData.officeOptions, parentGroups);
    }

    public static ClientData clientIdentifier(final Long id, final String accountNo, final EnumOptionData status, final String firstname,
            final String middlename, final String lastname, final String fullname, final String displayName, final Long officeId,
            final String officeName) {

        return new ClientData(accountNo, status, officeId, officeName, id, firstname, middlename, lastname, fullname, displayName, null,
                null, null, null, null);
    }

    public static ClientData lookup(final Long id, final String displayName, final Long officeId, final String officeName) {
        return new ClientData(null, null, officeId, officeName, id, null, null, null, null, displayName, null, null, null, null, null);
    }

    public static ClientData instance(final String accountNo, final EnumOptionData status, final Long officeId, final String officeName,
            final Long id, final String firstname, final String middlename, final String lastname, final String fullname,
            final String displayName, final String externalId, final LocalDate activationDate, final ImageData imageData) {
        return new ClientData(accountNo, status, officeId, officeName, id, firstname, middlename, lastname, fullname, displayName,
                externalId, activationDate, imageData, null, null);
    }

    private ClientData(final String accountNo, final EnumOptionData status, final Long officeId, final String officeName, final Long id,
            final String firstname, final String middlename, final String lastname, final String fullname, final String displayName,
            final String externalId, final LocalDate activationDate, final ImageData imageKey, final Collection<OfficeData> allowedOffices,
            final Collection<GroupGeneralData> groups) {
        this.accountNo = accountNo;
        this.status = status;
        if (status != null) {
            active = status.getId().equals(300L);
        } else {
            active = null;
        }
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
        this.imageData = imageKey;
        if (imageKey != null) {
            this.imagePresent = Boolean.TRUE;
        } else {
            this.imagePresent = null;
        }

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
        return this.imageData.imageKey();
    }

    public boolean imageKeyDoesNotExist() {
        return imageData.equals(null) && !imageKeyExists();
    }

    private boolean imageKeyExists() {
        return StringUtils.isNotBlank(this.imageData.imageKey());
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

    public void setImageData(ImageData imageData){
        this.imageData = imageData;
    }

    public String imageName() {
        return imageData.name();
    }

    public String imageContentType() {
        return imageData.contentType();
    }

    public byte[] image(){
        try{
            return imageData.getContent();
        }catch (IOException ioe){
            logger.error("Error in fetching client image");
            return null;
        }
    }
}