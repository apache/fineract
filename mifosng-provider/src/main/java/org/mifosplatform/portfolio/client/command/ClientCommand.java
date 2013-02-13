/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.command;

import org.joda.time.LocalDate;

public class ClientCommand {

    @SuppressWarnings("unused")
    private final String accountNo;
    private final String externalId;
    private final String firstname;
    private final String middlename;
    private final String lastname;
    private final String fullname;
    private final Long officeId;
    private final LocalDate joinedDate;

    public ClientCommand(final String accountNo, final String externalId, final String firstname, final String middlename,
            final String lastname, final String fullname, final Long officeId, final LocalDate joinedDate) {
        this.accountNo = accountNo;
        this.externalId = externalId;
        this.firstname = firstname;
        this.middlename = middlename;
        this.lastname = lastname;
        this.fullname = fullname;
        this.officeId = officeId;
        this.joinedDate = joinedDate;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getMiddlename() {
        return this.middlename;
    }

    public String getLastname() {
        return this.lastname;
    }

    public String getFullname() {
        return this.fullname;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public LocalDate getJoiningDate() {
        return this.joinedDate;
    }
}