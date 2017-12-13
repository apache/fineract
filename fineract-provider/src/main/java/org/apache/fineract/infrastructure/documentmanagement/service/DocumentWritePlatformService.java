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
package org.apache.fineract.infrastructure.documentmanagement.service;

import java.io.InputStream;

import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DocumentWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_DOCUMENT')")
    Long createDocument(DocumentCommand documentCommand, InputStream inputStream);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'UPDATE_DOCUMENT')")
    CommandProcessingResult updateDocument(DocumentCommand documentCommand, InputStream inputStream);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DELETE_DOCUMENT')")
    CommandProcessingResult deleteDocument(DocumentCommand documentCommand);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_DOCUMENT')")
    Long createInternalDocument(String entityType, Long entityId,
            Long fileSize, InputStream inputStream, String mimeType,
            String name, String description, String fileName);

}