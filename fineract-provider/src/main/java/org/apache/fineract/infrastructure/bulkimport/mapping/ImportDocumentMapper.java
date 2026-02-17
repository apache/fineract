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
package org.apache.fineract.infrastructure.bulkimport.mapping;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.apache.fineract.infrastructure.bulkimport.data.ImportData;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface ImportDocumentMapper {

    @Mapping(source = "id", target = "importId")
    @Mapping(source = "documentId", target = "documentId")
    @Mapping(ignore = true, target = "name")
    @Mapping(source = "entityType", target = "entityType")
    @Mapping(source = "importTime", target = "importTime")
    @Mapping(source = "completed", target = "completed")
    @Mapping(source = "endTime", target = "endTime")
    @Mapping(source = "createdBy.id", target = "createdBy")
    @Mapping(source = "failureCount", target = "failureCount")
    @Mapping(source = "successCount", target = "successCount")
    @Mapping(source = "totalRecords", target = "totalRecords")
    ImportData map(ImportDocument source);
}
