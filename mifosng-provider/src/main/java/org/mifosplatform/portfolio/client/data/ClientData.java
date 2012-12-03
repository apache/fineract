package org.mifosplatform.portfolio.client.data;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.organisation.office.data.OfficeLookup;

/**
 * Immutable data object representing client data.
 */
final public class ClientData {

    private final Long id;
    private final String firstname;
    private final String lastname;
    private final String clientOrBusinessName;
    private final String displayName;
    private final Long officeId;
    private final String officeName;
    private final String externalId;
    private final LocalDate joinedDate;
    private final String imageKey;
    @SuppressWarnings("unused")
    private final Boolean imagePresent;

    private final List<OfficeLookup> allowedOffices;

    private final ClientData currentChange;
    private final Collection<ClientData> allChanges;

    private static String buildDisplayNameFrom(final String firstname, final String lastname) {
        String displayName = null;
        StringBuilder displayNameBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(firstname)) {
            displayNameBuilder.append(firstname).append(' ');
        }

        if (StringUtils.isNotBlank(lastname)) {
            displayNameBuilder.append(lastname);
            displayName = displayNameBuilder.toString();
        }
        return displayName;
    }

    public static ClientData dataChangeInstance(final Long id, final Long officeId, final String externalId, final String firstname,
            final String lastname, final String clientOrBusinessName, final LocalDate joiningDate) {

        String firstnameValue = firstname;
        String lastnameValue = lastname;
        if (StringUtils.isNotBlank(clientOrBusinessName)) {
            firstnameValue = null;
            lastnameValue = clientOrBusinessName;
        }
        final String displayName = buildDisplayNameFrom(firstnameValue, lastnameValue);

        return new ClientData(officeId, null, id, firstnameValue, lastnameValue, displayName, externalId, joiningDate, null, null, null,
                null);
    }

    public static ClientData integrateChanges(final ClientData clientData, ClientData currentChange, final Collection<ClientData> allChanges) {
        String firstname = clientData.firstname;
        String lastname = clientData.lastname;
        if (StringUtils.isNotBlank(clientData.clientOrBusinessName)) {
            firstname = null;
            lastname = clientData.clientOrBusinessName;
        }
        final String displayName = buildDisplayNameFrom(firstname, lastname);
        return new ClientData(clientData.officeId, clientData.officeName, clientData.id, firstname, lastname, displayName,
                clientData.externalId, clientData.joinedDate, clientData.imageKey, clientData.allowedOffices, currentChange, allChanges);
    }

    public static ClientData template(final Long officeId, final LocalDate joinedDate, final List<OfficeLookup> allowedOffices) {
        return new ClientData(officeId, null, null, null, null, null, null, joinedDate, null, allowedOffices, null, null);
    }

    public static ClientData templateOnTop(final ClientData clientData, final List<OfficeLookup> allowedOffices) {

        String firstname = clientData.firstname;
        String lastname = clientData.lastname;
        if (StringUtils.isNotBlank(clientData.clientOrBusinessName)) {
            firstname = null;
            lastname = clientData.clientOrBusinessName;
        }
        final String displayName = buildDisplayNameFrom(firstname, lastname);
        return new ClientData(clientData.officeId, clientData.officeName, clientData.id, firstname, lastname, displayName,
                clientData.externalId, clientData.joinedDate, clientData.imageKey, allowedOffices, clientData.currentChange,
                clientData.allChanges);
    }

    public static ClientData clientIdentifier(final Long id, final String firstname, final String lastname, final Long officeId,
            final String officeName) {

        final String displayName = buildDisplayNameFrom(firstname, lastname);

        return new ClientData(officeId, officeName, id, firstname, lastname, displayName, null, null, null, null, null, null);
    }

    public ClientData(final Long officeId, final String officeName, final Long id, final String firstname, final String lastname,
            final String displayName, final String externalId, final LocalDate joinedDate, final String imageKey,
            final List<OfficeLookup> allowedOffices, final ClientData currentChange, final Collection<ClientData> allChanges) {
        this.officeId = officeId;
        this.officeName = officeName;
        this.id = id;
        this.firstname = firstname;

        /*** unset last name for business name **/
        if (StringUtils.isBlank(firstname)) {
            this.lastname = null;
            this.clientOrBusinessName = lastname;
        } else {
            this.lastname = lastname;
            this.clientOrBusinessName = null;
        }
        this.displayName = displayName;
        this.externalId = externalId;
        this.joinedDate = joinedDate;
        this.imageKey = imageKey;
        if (imageKey != null) {
            this.imagePresent = Boolean.TRUE;
        } else {
            this.imagePresent = null;
        }
        this.allowedOffices = allowedOffices;
        this.currentChange = currentChange;
        this.allChanges = allChanges;
    }

    public String displayName() {
        return this.displayName;
    }

    public String officeName() {
        return this.officeName;
    }

    private boolean imageKeyExists() {
        return StringUtils.isNotBlank(this.imageKey);
    }

    public boolean imageKeyDoesNotExist() {
        return !imageKeyExists();
    }

    public String imageKey() {
        return this.imageKey;
    }

    public Long id() {
        return this.id;
    }

    public Long officeId() {
        return this.officeId;
    }

    public ClientData currentChange() {
        return this.currentChange;
    }
}