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

import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.organisation.staff.data.StaffCreateRequest;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class)
public abstract class StaffCreateRequestMapper {

    protected final StaffDateMapper dateMapper = new StaffDateMapper();

    @Mapping(source = "isActive", target = "active")
    @Mapping(source = "isLoanOfficer", target = "loanOfficer")
    @Mapping(expression = "java( dateMapper.map(source.getJoiningDate(), source.getDateFormat()) )", target = "joiningDate")
    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "imageId")
    @Mapping(ignore = true, target = "organisationalRoleParentStaff")
    @Mapping(ignore = true, target = "organisationalRoleType")
    @Mapping(ignore = true, target = "displayName")
    @Mapping(ignore = true, target = "office")
    public abstract Staff map(StaffCreateRequest source);
}
