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
package org.apache.fineract.infrastructure.documentmanagement.mapping;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.io.InputStream;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentContent;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.domain.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface DocumentMapper {

    DocumentData map(Document source);

    @Mapping(source = "source.type", target = "contentType")
    @Mapping(source = "source.name", target = "displayName")
    @Mapping(source = "source.fileName", target = "fileName")
    @Mapping(source = "source.size", target = "size")
    @Mapping(ignore = true, target = "format")
    @Mapping(source = "stream", target = "stream")
    DocumentContent map(Document source, InputStream stream);
}
