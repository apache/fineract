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
package org.apache.fineract.organisation.office.data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Immutable data object for office data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class OfficeData implements Serializable {

    private Long id;
    private String name;
    private String nameDecorated;
    private String externalId;
    private LocalDate openingDate;
    private String hierarchy;
    private Long parentId;
    private String parentName;
    private Collection<OfficeData> allowedParents;

    // import fields
    private transient Integer rowIndex;
    private String locale;
    private String dateFormat;

    public static OfficeData importInstance(final String name, final Long parentId, final LocalDate openingDate, final String externalId) {
        return new OfficeData()
                .setName(name)
                .setParentId(parentId)
                .setOpeningDate(openingDate)
                .setExternalId(externalId);
    }

    public void setImportFields(final Integer rowIndex, final String locale, final String dateFormat) {
        this.rowIndex = rowIndex;
        this.locale = locale;
        this.dateFormat = dateFormat;
    }

    public static OfficeData testInstance(final Long id, final String name) {
        return new OfficeData()
                .setId(id)
                .setName(name);
    }

    public static OfficeData dropdown(final Long id, final String name, final String nameDecorated) {
        return new OfficeData()
                .setId(id)
                .setName(name)
                .setNameDecorated(nameDecorated);
    }

    public static OfficeData template(final List<OfficeData> parentLookups, final LocalDate defaultOpeningDate) {
        return new OfficeData()
                .setAllowedParents(parentLookups)
                .setOpeningDate(defaultOpeningDate);
    }

    public static OfficeData appendedTemplate(final OfficeData office, final Collection<OfficeData> allowedParents) {
        return new OfficeData()
                .setId(office.id)
                .setName(office.name)
                .setNameDecorated(office.nameDecorated)
                .setExternalId(office.externalId)
                .setOpeningDate(office.openingDate)
                .setHierarchy(office.hierarchy)
                .setParentId(office.parentId)
                .setParentName(office.parentName)
                .setAllowedParents(allowedParents);
    }

    public boolean hasIdentifyOf(final Long officeId) {
        return this.id.equals(officeId);
    }
}
