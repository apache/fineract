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
package org.apache.fineract.portfolio.account.mapper;

import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.portfolio.account.data.StandingInstructionUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(config = MapstructMapperConfig.class)
public interface StandingInstructionUpdateRequestMapper {

    StandingInstructionUpdateRequestMapper INSTANCE = Mappers.getMapper(StandingInstructionUpdateRequestMapper.class);

    // Copy everything from source to target
    StandingInstructionUpdateRequest copy(StandingInstructionUpdateRequest source);

    // Update existing target object with source values
    void updateFrom(@MappingTarget StandingInstructionUpdateRequest target, StandingInstructionUpdateRequest source);

    // Map everything except @JsonIgnore fields
    @Mapping(target = "commandParam", ignore = true)
    @Mapping(target = "standingInstructionId", ignore = true)
    StandingInstructionUpdateRequest toServiceRequest(StandingInstructionUpdateRequest source);
}
