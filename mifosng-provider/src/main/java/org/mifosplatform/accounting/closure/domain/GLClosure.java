/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.closure.domain;

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
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.accounting.closure.api.GLClosureJsonInputParams;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "acc_gl_closure", uniqueConstraints = { @UniqueConstraint(columnNames = { "office_id", "closing_date" }, name = "office_id_closing_date") })
public class GLClosure extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = true;

    @Column(name = "closing_date")
    @Temporal(TemporalType.DATE)
    private Date closingDate;

    @Column(name = "comments", nullable = true, length = 500)
    private String comments;

    protected GLClosure() {
        //
    }

    public GLClosure(final Office office, final Date closingDate, final String comments) {
        this.office = office;
        this.deleted = false;
        this.closingDate = closingDate;
        this.comments = StringUtils.defaultIfEmpty(comments, null);
        if (this.comments != null) {
            this.comments = this.comments.trim();
        }
    }

    public static GLClosure fromJson(final Office office, final JsonCommand command) {
        final Date closingDate = command.DateValueOfParameterNamed(GLClosureJsonInputParams.CLOSING_DATE.getValue());
        final String comments = command.stringValueOfParameterNamed(GLClosureJsonInputParams.COMMENTS.getValue());
        return new GLClosure(office, closingDate, comments);
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(5);
        handlePropertyUpdate(command, actualChanges, GLClosureJsonInputParams.COMMENTS.getValue(), this.comments);
        return actualChanges;
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            final String propertyToBeUpdated) {
        if (command.isChangeInStringParameterNamed(paramName, propertyToBeUpdated)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            // now update actual property
            if (paramName.equals(GLClosureJsonInputParams.COMMENTS.getValue())) {
                this.comments = newValue;
            }
        }
    }

    public Date getClosingDate() {
        return this.closingDate;
    }

    public Office getOffice() {
        return this.office;
    }

}