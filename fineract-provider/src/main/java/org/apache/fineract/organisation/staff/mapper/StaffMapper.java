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
package org.apache.fineract.organisation.staff.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.data.CreateStaffRequest;
import org.apache.fineract.organisation.staff.data.UpdateStaffRequest;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = MapstructMapperConfig.class)
public abstract class StaffMapper {

    @Autowired
    protected OfficeRepositoryWrapper officeRepositoryWrapper;

    @Mappings({ @Mapping(target = "id", ignore = true), @Mapping(target = "office", ignore = true),
            @Mapping(target = "organisationalRoleType", ignore = true), @Mapping(target = "organisationalRoleParentStaff", ignore = true),
            @Mapping(target = "image", ignore = true), @Mapping(target = "joiningDate", ignore = true),
            @Mapping(target = "displayName", ignore = true) })
    public abstract void updateStaffFromRequest(UpdateStaffRequest request, @MappingTarget Staff staff);

    @Mappings({ @Mapping(target = "id", ignore = true), @Mapping(target = "office", ignore = true),
            @Mapping(target = "organisationalRoleType", ignore = true), @Mapping(target = "organisationalRoleParentStaff", ignore = true),
            @Mapping(target = "image", ignore = true), @Mapping(target = "joiningDate", ignore = true),
            @Mapping(target = "displayName", ignore = true) })
    public abstract Staff map(CreateStaffRequest request);

    @AfterMapping
    protected void mapCreateRequest(@MappingTarget Staff staff, CreateStaffRequest source) {
        if (source == null || staff == null) {
            return;
        }
        addOfficeToStaff(staff, source.getOfficeId());
        addDisplayNameToStaff(staff, source.getFirstname(), source.getLastname());
        addJoiningDateToStaff(staff, source.getJoiningDate(), source.getDateFormat(), source.getLocale());
    }

    @AfterMapping
    protected void afterUpdateStaffFromRequest(UpdateStaffRequest request, @MappingTarget Staff staff) {
        if (request == null || staff == null) {
            return;
        }

        addOfficeToStaff(staff, request.getOfficeId());
        addDisplayNameToStaff(staff, request.getFirstname(), request.getLastname());
        addJoiningDateToStaff(staff, request.getJoiningDate(), request.getDateFormat(), request.getLocale());

    }

    private void addJoiningDateToStaff(@MappingTarget Staff staff, String joiningDate2, String dateFormat2, String locale) {
        if (StringUtils.isNotBlank(joiningDate2)) {
            try {
                String dateFormat = StringUtils.isBlank(dateFormat2) ? DateUtils.DATE_FORMAT : dateFormat2;
                LocalDate joiningDate = LocalDate.parse(joiningDate2,
                        DateTimeFormatter.ofPattern(dateFormat).withLocale(Locale.forLanguageTag(locale)));
                staff.setJoiningDate(joiningDate);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid joining date format. Expected format: " + dateFormat2, e);
            }
        }
    }

    private void addDisplayNameToStaff(@MappingTarget Staff staff, String firstname, String lastname) {
        if (!StringUtils.isBlank(firstname)) {
            staff.setDisplayName(lastname + ", " + firstname);
        } else {
            staff.setDisplayName(lastname);
        }
    }

    private void addOfficeToStaff(@MappingTarget Staff staff, Long officeId) {
        if (officeId != null) {
            Office office = officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
            staff.changeOffice(office);
        }
    }
}
