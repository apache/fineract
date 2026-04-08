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
package org.apache.fineract.command.jdbc.store.mapping;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.jdbc.store.domain.CommandEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR, uses = { CommandJsonMapper.class })
@SuppressWarnings("rawtypes")
public interface CommandMapper {

    @Mapping(source = "commandId", target = "id")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "executedAt", target = "executedAt")
    @Mapping(source = "approvedAt", target = "approvedAt")
    @Mapping(source = "rejectedAt", target = "rejectedAt")
    @Mapping(source = "initiatedByUsername", target = "initiatedByUsername")
    @Mapping(source = "executedByUsername", target = "executedByUsername")
    @Mapping(source = "approvedByUsername", target = "approvedByUsername")
    @Mapping(source = "rejectedByUsername", target = "rejectedByUsername")
    @Mapping(source = "payload", target = "request")
    @Mapping(source = "ipAddress", target = "ipAddress")
    @Mapping(source = "idempotencyKey", target = "idempotencyKey")
    @Mapping(source = "error", target = "error")
    @Mapping(ignore = true, target = "state")
    @Mapping(ignore = true, target = "response")
    CommandEntity map(Command source);

    @Mapping(source = "source.commandId", target = "id")
    @Mapping(source = "source.createdAt", target = "createdAt")
    @Mapping(source = "source.updatedAt", target = "updatedAt")
    @Mapping(source = "source.executedAt", target = "executedAt")
    @Mapping(source = "source.approvedAt", target = "approvedAt")
    @Mapping(source = "source.rejectedAt", target = "rejectedAt")
    @Mapping(source = "source.initiatedByUsername", target = "initiatedByUsername")
    @Mapping(source = "source.executedByUsername", target = "executedByUsername")
    @Mapping(source = "source.approvedByUsername", target = "approvedByUsername")
    @Mapping(source = "source.rejectedByUsername", target = "rejectedByUsername")
    @Mapping(source = "source.payload", target = "request")
    @Mapping(source = "source.ipAddress", target = "ipAddress")
    @Mapping(source = "source.idempotencyKey", target = "idempotencyKey")
    @Mapping(source = "source.error", target = "error")
    @Mapping(source = "response", target = "response")
    @Mapping(ignore = true, target = "state")
    CommandEntity map(Command source, Object response);

    @InheritInverseConfiguration
    Command map(CommandEntity source);
}
