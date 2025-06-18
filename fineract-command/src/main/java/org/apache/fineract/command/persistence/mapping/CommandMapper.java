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
package org.apache.fineract.command.persistence.mapping;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.persistence.domain.CommandEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR, uses = { CommandJsonMapper.class })
public interface CommandMapper {

    @Mapping(ignore = true, target = "id")
    @Mapping(source = "id", target = "commandId")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "tenantId", target = "tenantId")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "payload", target = "payload")
    CommandEntity map(Command source);

    @InheritInverseConfiguration
    Command map(CommandEntity source);
}
