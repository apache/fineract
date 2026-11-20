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
package org.apache.fineract.accounting.closure.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.organisation.office.domain.Office;

@Entity
@Table(name = "acc_gl_closure", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "office_id", "closing_date" }, name = "office_id_closing_date") })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GLClosure extends AbstractAuditableCustom {

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = true;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Column(name = "comments", nullable = true, length = 500)
    private String comments;

    public GLClosure(final Office office, final LocalDate closingDate, final String comments) {
        this.office = office;
        this.deleted = false;
        this.closingDate = closingDate;
        this.comments = StringUtils.defaultIfEmpty(comments, null);
        if (this.comments != null) {
            this.comments = this.comments.trim();
        }
    }

    public void setComments(final String comments) {
        this.comments = StringUtils.defaultIfEmpty(comments, null);
        if (this.comments != null) {
            this.comments = this.comments.trim();
        }
    }
}
