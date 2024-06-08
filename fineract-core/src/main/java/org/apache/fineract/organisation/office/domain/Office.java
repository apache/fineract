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
package org.apache.fineract.organisation.office.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.office.exception.CannotUpdateOfficeWithParentOfficeSameAsSelf;
import org.apache.fineract.organisation.office.exception.RootOfficeParentCannotBeUpdated;

@Entity
@Table(name = "m_office", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "name_org"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "externalid_org") })
@Getter
@Setter
public class Office extends AbstractPersistableCustom<Long> implements Serializable {

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private List<Office> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Office parent;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "hierarchy", length = 50)
    private String hierarchy;

    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    @Column(name = "external_id", length = 100, unique = true)
    private ExternalId externalId;

    public static Office headOffice(final String name, final LocalDate openingDate, final ExternalId externalId) {
        return new Office(null, name, openingDate, externalId);
    }

    public static Office fromJson(final Office parentOffice, final JsonCommand command) {

        final String name = command.stringValueOfParameterNamed("name");
        final LocalDate openingDate = command.localDateValueOfParameterNamed("openingDate");
        final String externalId = command.stringValueOfParameterNamed("externalId");
        return new Office(parentOffice, name, openingDate, ExternalIdFactory.produce(externalId));
    }

    protected Office() {
        this.openingDate = null;
        this.parent = null;
        this.name = null;
        this.externalId = null;
    }

    private Office(final Office parent, final String name, final LocalDate openingDate, final ExternalId externalId) {
        this.parent = parent;
        this.openingDate = openingDate;
        if (parent != null) {
            this.parent.addChild(this);
        }

        if (StringUtils.isNotBlank(name)) {
            this.name = name.trim();
        } else {
            this.name = null;
        }
        this.externalId = externalId;
    }

    private void addChild(final Office office) {
        this.children.add(office);
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String parentIdParamName = "parentId";

        if (command.parameterExists(parentIdParamName) && this.parent == null) {
            throw new RootOfficeParentCannotBeUpdated();
        }

        if (this.parent != null && command.isChangeInLongParameterNamed(parentIdParamName, this.parent.getId())) {
            final Long newValue = command.longValueOfParameterNamed(parentIdParamName);
            actualChanges.put(parentIdParamName, newValue);
        }

        final String openingDateParamName = "openingDate";
        if (command.isChangeInLocalDateParameterNamed(openingDateParamName, getOpeningLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(openingDateParamName);
            actualChanges.put(openingDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            this.openingDate = command.localDateValueOfParameterNamed(openingDateParamName);
        }

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }

        final String externalIdParamName = "externalId";
        if (command.isChangeInStringParameterNamed(externalIdParamName,
                Optional.ofNullable(this.externalId).map(ExternalId::getValue).orElse(null))) {
            final String newValue = command.stringValueOfParameterNamed(externalIdParamName);
            actualChanges.put(externalIdParamName, newValue);
            this.externalId = ExternalIdFactory.produce(StringUtils.defaultIfEmpty(newValue, null));
        }

        return actualChanges;
    }

    public boolean isOpeningDateBefore(final LocalDate baseDate) {
        return DateUtils.isBefore(getOpeningLocalDate(), baseDate);
    }

    public boolean isOpeningDateAfter(final LocalDate baseDate) {
        return DateUtils.isAfter(getOpeningLocalDate(), baseDate);
    }

    public LocalDate getOpeningLocalDate() {
        return this.openingDate;
    }

    public void update(final Office newParent) {

        if (this.parent == null) {
            throw new RootOfficeParentCannotBeUpdated();
        }

        if (identifiedBy(newParent.getId())) {
            throw new CannotUpdateOfficeWithParentOfficeSameAsSelf(getId(), newParent.getId());
        }

        this.parent = newParent;
        generateHierarchy();
    }

    public boolean identifiedBy(final Long id) {
        return getId().equals(id);
    }

    public void generateHierarchy() {

        if (this.parent != null) {
            this.hierarchy = this.parent.hierarchyOf(getId());
        } else {
            this.hierarchy = ".";
        }
    }

    private String hierarchyOf(final Long id) {
        return this.hierarchy + id.toString() + ".";
    }

    public boolean hasParentOf(final Office office) {
        if (this.parent != null) {
            return this.parent.equals(office);
        }
        return false;
    }

    public boolean doesNotHaveAnOfficeInHierarchyWithId(final Long officeId) {
        return !hasAnOfficeInHierarchyWithId(officeId);
    }

    private boolean hasAnOfficeInHierarchyWithId(final Long officeId) {

        boolean match = false;

        if (identifiedBy(officeId)) {
            match = true;
        }

        if (!match) {
            for (final Office child : this.children) {
                final boolean result = child.hasAnOfficeInHierarchyWithId(officeId);

                if (result) {
                    match = true;
                    break;
                }
            }
        }

        return match;
    }

    public void loadLazyCollections() {
        this.children.size();
    }
}
