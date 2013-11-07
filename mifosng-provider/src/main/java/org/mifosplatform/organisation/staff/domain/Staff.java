/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.staff.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.office.domain.Office;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_staff", uniqueConstraints = { @UniqueConstraint(columnNames = { "display_name" }, name = "display_name"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "external_id_UNIQUE"),
        @UniqueConstraint(columnNames = { "mobile_no" }, name = "mobile_no_UNIQUE") })
public class Staff extends AbstractPersistable<Long> {

    @Column(name = "firstname", length = 50)
    private String firstname;

    @Column(name = "lastname", length = 50)
    private String lastname;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "mobile_no", length = 50, nullable = false, unique = true)
    private String mobileNo;

    @Column(name = "external_id", length = 100, nullable = true, unique = true)
    private String externalId;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "is_loan_officer ", nullable = false)
    private boolean loanOfficer;

    @Column(name = "organisational_role_enum", nullable = true)
    private Integer organisationalRoleType;

    @ManyToOne
    @JoinColumn(name = "organisational_role_parent_staff_id", nullable = true)
    private Staff organisationalRoleParentStaff;

    public static Staff fromJson(final Office staffOffice, final JsonCommand command) {

        final String firstnameParamName = "firstname";
        final String firstname = command.stringValueOfParameterNamed(firstnameParamName);

        final String lastnameParamName = "lastname";
        final String lastname = command.stringValueOfParameterNamed(lastnameParamName);

        final String externalIdParamName = "externalId";
        final String externalId = command.stringValueOfParameterNamedAllowingNull(externalIdParamName);

        final String mobileNoParamName = "mobileNo";
        final String mobileNo = command.stringValueOfParameterNamedAllowingNull(mobileNoParamName);

        final String isLoanOfficerParamName = "isLoanOfficer";
        final boolean isLoanOfficer = command.booleanPrimitiveValueOfParameterNamed(isLoanOfficerParamName);

        return new Staff(staffOffice, firstname, lastname, externalId, mobileNo, isLoanOfficer);
    }

    protected Staff() {
        //
    }

    private Staff(final Office staffOffice, final String firstname, final String lastname, final String externalId, final String mobileNo,
            final boolean isLoanOfficer) {
        this.office = staffOffice;
        this.firstname = StringUtils.defaultIfEmpty(firstname, null);
        this.lastname = StringUtils.defaultIfEmpty(lastname, null);
        this.externalId = StringUtils.defaultIfEmpty(externalId, null);
        this.mobileNo = StringUtils.defaultIfEmpty(mobileNo, null);
        this.loanOfficer = isLoanOfficer;
        deriveDisplayName(firstname);
    }

    public EnumOptionData organisationalRoleData() {
        EnumOptionData organisationalRole = null;
        if (this.organisationalRoleType != null) {
            organisationalRole = StaffEnumerations.organisationalRole(this.organisationalRoleType);
        }
        return organisationalRole;
    }

    public void changeOffice(final Office newOffice) {
        this.office = newOffice;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(7);

        final String officeIdParamName = "officeId";
        if (command.isChangeInLongParameterNamed(officeIdParamName, this.office.getId())) {
            final Long newValue = command.longValueOfParameterNamed(officeIdParamName);
            actualChanges.put(officeIdParamName, newValue);
        }

        boolean firstnameChanged = false;
        final String firstnameParamName = "firstname";
        if (command.isChangeInStringParameterNamed(firstnameParamName, this.firstname)) {
            final String newValue = command.stringValueOfParameterNamed(firstnameParamName);
            actualChanges.put(firstnameParamName, newValue);
            this.firstname = newValue;
            firstnameChanged = true;
        }

        boolean lastnameChanged = false;
        final String lastnameParamName = "lastname";
        if (command.isChangeInStringParameterNamed(lastnameParamName, this.lastname)) {
            final String newValue = command.stringValueOfParameterNamed(lastnameParamName);
            actualChanges.put(lastnameParamName, newValue);
            this.lastname = newValue;
            lastnameChanged = true;
        }

        if (firstnameChanged || lastnameChanged) {
            deriveDisplayName(this.firstname);
        }

        final String externalIdParamName = "externalId";
        if (command.isChangeInStringParameterNamed(externalIdParamName, this.externalId)) {
            final String newValue = command.stringValueOfParameterNamed(externalIdParamName);
            actualChanges.put(externalIdParamName, newValue);
            this.externalId = newValue;
        }

        final String mobileNoParamName = "mobileNo";
        if (command.isChangeInStringParameterNamed(mobileNoParamName, this.mobileNo)) {
            final String newValue = command.stringValueOfParameterNamed(mobileNoParamName);
            actualChanges.put(mobileNoParamName, newValue);
            this.mobileNo = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String isLoanOfficerParamName = "isLoanOfficer";
        if (command.isChangeInBooleanParameterNamed(isLoanOfficerParamName, this.loanOfficer)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(isLoanOfficerParamName);
            actualChanges.put(isLoanOfficerParamName, newValue);
            this.loanOfficer = newValue;
        }

        return actualChanges;
    }

    public boolean isNotLoanOfficer() {
        return !isLoanOfficer();
    }

    public boolean isLoanOfficer() {
        return this.loanOfficer;
    }

    private void deriveDisplayName(final String firstname) {
        if (!StringUtils.isBlank(firstname)) {
            this.displayName = this.lastname + ", " + this.firstname;
        } else {
            this.displayName = this.lastname;
        }
    }

    public boolean identifiedBy(final Staff staff) {
        return getId().equals(staff.getId());
    }

    public Long officeId() {
        return this.office.getId();
    }

    public String displayName() {
        return this.displayName;
    }

    public String mobileNo() {
        return this.mobileNo;
    }

    public Office office() {
        return this.office;
    }
}