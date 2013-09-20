/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "stretchy_report_parameter")
public final class ReportParameterUsage extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(optional = false)
    @JoinColumn(name = "parameter_id", nullable = false)
    private ReportParameter parameter;

    @Column(name = "report_parameter_name")
    private String reportParameterName;

    protected ReportParameterUsage() {
        //
    }

    public ReportParameterUsage(final Report report, final ReportParameter parameter, final String reportParameterName) {
        this.report = report;
        this.parameter = parameter;
        this.reportParameterName = reportParameterName;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        final ReportParameterUsage rhs = (ReportParameterUsage) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)) //
                .append(getId(), rhs.getId()) //
                .append(this.report.getId(), rhs.report.getId()) //
                .append(this.parameter.getId(), rhs.parameter.getId()) //
                .append(this.reportParameterName, rhs.reportParameterName) //
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 5) //
                .append(getId()) //
                .append(this.report.getId()) //
                .append(this.parameter.getId()) //
                .append(this.reportParameterName) //
                .toHashCode();
    }

    public boolean hasIdOf(final Long id) {
        return getId().equals(id);
    }

    public boolean hasParameterIdOf(final Long parameterId) {
        return this.parameter != null && this.parameter.hasIdOf(parameterId);
    }

    public void updateParameterName(final String parameterName) {
        this.reportParameterName = parameterName;
    }
}