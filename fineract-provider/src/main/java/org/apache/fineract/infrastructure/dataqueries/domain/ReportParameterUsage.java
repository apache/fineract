/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.dataqueries.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "stretchy_report_parameter")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public final class ReportParameterUsage extends AbstractPersistableCustom {

    @ManyToOne(optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(optional = false)
    @JoinColumn(name = "parameter_id", nullable = false)
    private ReportParameter parameter;

    @Column(name = "report_parameter_name")
    private String reportParameterName;
    /*
     * @Override public boolean equals(final Object obj) { if (obj == null) { return false; } if (obj == this) { return
     * true; } if (obj.getClass() != getClass()) { return false; } final ReportParameterUsage rhs =
     * (ReportParameterUsage) obj; return new EqualsBuilder().appendSuper(super.equals(obj)) // .append(getId(),
     * rhs.getId()) // .append(this.report.getId(), rhs.report.getId()) // .append(this.parameter.getId(),
     * rhs.parameter.getId()) // .append(this.reportParameterName, rhs.reportParameterName) // .isEquals(); }
     *
     * @Override public int hashCode() { return new HashCodeBuilder(3, 5) // .append(getId()) //
     * .append(this.report.getId()) // .append(this.parameter.getId()) // .append(this.reportParameterName) //
     * .toHashCode(); }
     */

    public boolean hasParameterIdOf(final Long parameterId) {
        return this.parameter != null && this.parameter.getId().equals(parameterId);
    }
}
