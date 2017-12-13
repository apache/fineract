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
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.joda.time.LocalDate;

/**
 * Immutable data object for office data.
 */
public class OfficeData implements Serializable {

    private final Long id;
    private final String name;
    private final String nameDecorated;
    private final String externalId;
    private final LocalDate openingDate;
    private final String hierarchy;
    private final Long parentId;
    private final String parentName;
    @SuppressWarnings("unused")
    private final Collection<OfficeData> allowedParents;

    //import fields
    private transient Integer rowIndex;
    private String locale;
    private String dateFormat;

    public static OfficeData importInstance(final String name, final Long parentId, final LocalDate openingDate,final String externalId) {
        return new OfficeData(null, name, null, externalId, openingDate, null, parentId, null, null);
    }
    public void setImportFields(final Integer rowIndex, final String locale, final String dateFormat) {
        this.rowIndex = rowIndex;
        this.locale = locale;
        this.dateFormat = dateFormat;
    }
    public static OfficeData testInstance(final Long id,final String name){
        return new OfficeData(id,name,null,null,
                null,null,null,null,
                null);
    }
    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public Long getId() {
        return id;
    }

    public static OfficeData dropdown(final Long id, final String name, final String nameDecorated) {
        return new OfficeData(id, name, nameDecorated, null, null, null, null, null, null);
    }

    public static OfficeData template(final List<OfficeData> parentLookups, final LocalDate defaultOpeningDate) {
        return new OfficeData(null, null, null, null, defaultOpeningDate, null, null, null, parentLookups);
    }

    public static OfficeData appendedTemplate(final OfficeData office, final Collection<OfficeData> allowedParents) {
        return new OfficeData(office.id, office.name, office.nameDecorated, office.externalId, office.openingDate, office.hierarchy,
                office.parentId, office.parentName, allowedParents);
    }

    public OfficeData(final Long id, final String name, final String nameDecorated, final String externalId, final LocalDate openingDate,
            final String hierarchy, final Long parentId, final String parentName, final Collection<OfficeData> allowedParents) {
        this.id = id;
        this.name = name;
        this.nameDecorated = nameDecorated;
        this.externalId = externalId;
        this.openingDate = openingDate;
        this.hierarchy = hierarchy;
        this.parentName = parentName;
        this.parentId = parentId;
        this.allowedParents = allowedParents;
    }

    public boolean hasIdentifyOf(final Long officeId) {
        return this.id.equals(officeId);
    }

    public String name() {
        return this.name;
    }

    public String getHierarchy() {
        return this.hierarchy;
    }
}