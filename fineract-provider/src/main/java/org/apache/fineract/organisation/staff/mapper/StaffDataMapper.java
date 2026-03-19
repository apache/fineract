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
import org.apache.fineract.organisation.staff.data.StaffData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class, uses = StaffDateMapper.class)
public abstract class StaffDataMapper {

    protected final StaffDateMapper dateMapper = new StaffDateMapper();

    @Mapping(ignore = true, target = "emailAddress")
    @Mapping(ignore = true, target = "forceStatus")
    @Mapping(expression = "java( dateMapper.map(source.getJoiningDate(), source.getDateFormat()) )", target = "joiningDate")
    public abstract StaffCreateRequest map(StaffData source);
}
